package app.capgo.disqo

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.getcapacitor.JSObject
import com.getcapacitor.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

data class SdkStatus(
    val available: Boolean,
    val missingClasses: List<String>,
    val message: String,
)

data class ServiceStateSnapshot(
    val enabled: Boolean,
    val state: String,
    val raw: String?,
)

private data class PendingAuthRequest(
    val kind: AuthRequestKind,
    val deferred: CompletableDeferred<AuthRequestResolution>,
)

private enum class AuthRequestKind(
    val eventName: String,
    val label: String,
) {
    ACCESS_TOKEN("accessTokenRequested", "access token"),
    REFRESH_TOKEN("refreshTokenRequested", "refresh token"),
}

private sealed interface AuthRequestResolution {
    data class Success(val accessToken: String?) : AuthRequestResolution
    data class Failure(val message: String) : AuthRequestResolution
}

class Disqo(
    private val plugin: DisqoPlugin,
    private val pluginScope: CoroutineScope,
) {
    private var sdk: Any? = null
    private var cachedAccessToken: String? = null
    private val pendingRequests = ConcurrentHashMap<String, PendingAuthRequest>()

    fun getSdkStatus(): SdkStatus {
        val missing = REQUIRED_CLASS_NAMES.filterNot(::isClassAvailable)
        if (missing.isEmpty()) {
            return SdkStatus(
                available = true,
                missingClasses = emptyList(),
                message = "Disqo Pulse SDK classes are available.",
            )
        }

        return SdkStatus(
            available = false,
            missingClasses = missing,
            message = "Disqo Pulse SDK classes were not found in the current Android build.",
        )
    }

    fun initialize(context: Context, apiUrl: String) {
        val sdkStatus = getSdkStatus()
        if (!sdkStatus.available) {
            throw IllegalStateException(sdkStatus.message)
        }

        cancelPendingRequests("The SDK was reinitialized before the previous auth request completed.")

        val pulseInstance = resolvePulseInstance(context.applicationContext, apiUrl)
        val resolvedSdk = invokeCompatibleMethod(pulseInstance, "getSdk")
            ?: throw IllegalStateException("Pulse.getSdk() returned null.")

        val providerInterface = loadRequiredClass(PULSE_ACCESS_TOKEN_PROVIDER_CLASS)
        val proxy = Proxy.newProxyInstance(
            providerInterface.classLoader,
            arrayOf(providerInterface),
            PulseAccessTokenProviderHandler(),
        )

        invokeCompatibleMethod(resolvedSdk, "setup", proxy)
        sdk = resolvedSdk
    }

    fun start(userId: String) {
        invokeCompatibleMethod(requireSdk(), "start", userId)
    }

    fun stop() {
        invokeCompatibleMethod(requireSdk(), "stop")
    }

    fun updateAccessToken(accessToken: String?) {
        cachedAccessToken = accessToken?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun clearAccessToken() {
        cachedAccessToken = null
    }

    fun getServiceStateInfo(): ServiceStateSnapshot {
        val state = invokeCompatibleMethod(requireSdk(), "getServiceStateInfo")
        return ServiceStateSnapshot(
            enabled = isServiceEnabled(),
            state = normalizeStateName(state),
            raw = state?.toString(),
        )
    }

    fun isServiceEnabled(): Boolean {
        val value = invokeCompatibleMethod(requireSdk(), "isServiceEnabled")
        return value as? Boolean ?: false
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun refreshConfigs() {
        invokeCompatibleMethod(requireSdk(), "refreshConfigs")
    }

    fun send() {
        invokeCompatibleMethod(requireSdk(), "send")
    }

    fun resolveAccessTokenRequest(requestId: String, accessToken: String) {
        cachedAccessToken = accessToken
        completePendingRequest(
            requestId = requestId,
            expectedKind = AuthRequestKind.ACCESS_TOKEN,
            resolution = AuthRequestResolution.Success(accessToken),
        )
    }

    fun rejectAccessTokenRequest(requestId: String, message: String?) {
        completePendingRequest(
            requestId = requestId,
            expectedKind = AuthRequestKind.ACCESS_TOKEN,
            resolution = AuthRequestResolution.Failure(message ?: "The host app rejected the access token request."),
        )
    }

    fun resolveRefreshTokenRequest(requestId: String, accessToken: String?) {
        accessToken?.trim()?.takeIf { it.isNotEmpty() }?.let {
            cachedAccessToken = it
        }

        completePendingRequest(
            requestId = requestId,
            expectedKind = AuthRequestKind.REFRESH_TOKEN,
            resolution = AuthRequestResolution.Success(accessToken),
        )
    }

    fun rejectRefreshTokenRequest(requestId: String, message: String?) {
        completePendingRequest(
            requestId = requestId,
            expectedKind = AuthRequestKind.REFRESH_TOKEN,
            resolution = AuthRequestResolution.Failure(message ?: "The host app rejected the refresh token request."),
        )
    }

    fun dispose() {
        sdk = null
        cachedAccessToken = null
        cancelPendingRequests("The plugin was destroyed before the auth request completed.")
    }

    private fun requireSdk(): Any {
        return sdk ?: throw IllegalStateException("Disqo is not initialized. Call initialize() first.")
    }

    private fun completePendingRequest(
        requestId: String,
        expectedKind: AuthRequestKind,
        resolution: AuthRequestResolution,
    ) {
        val pending = pendingRequests.remove(requestId)
            ?: throw IllegalStateException("No pending ${expectedKind.label} request found for id $requestId.")

        if (pending.kind != expectedKind) {
            pendingRequests[requestId] = pending
            throw IllegalStateException(
                "Request $requestId is a ${pending.kind.label} request, not a ${expectedKind.label} request.",
            )
        }

        pending.deferred.complete(resolution)
    }

    private fun cancelPendingRequests(message: String) {
        pendingRequests.forEach { (_, pendingRequest) ->
            pendingRequest.deferred.complete(AuthRequestResolution.Failure(message))
        }
        pendingRequests.clear()
    }

    private suspend fun requestAccessToken(): String {
        cachedAccessToken?.let { return it }

        return when (val resolution = requestAuthFromJs(AuthRequestKind.ACCESS_TOKEN)) {
            is AuthRequestResolution.Success -> {
                val token = resolution.accessToken?.trim()?.takeIf { it.isNotEmpty() }
                    ?: throw IllegalStateException("The access token request resolved without an access token.")
                cachedAccessToken = token
                token
            }
            is AuthRequestResolution.Failure -> throw IllegalStateException(resolution.message)
        }
    }

    private suspend fun requestRefreshToken() {
        when (val resolution = requestAuthFromJs(AuthRequestKind.REFRESH_TOKEN)) {
            is AuthRequestResolution.Success -> {
                resolution.accessToken?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    cachedAccessToken = it
                }
            }
            is AuthRequestResolution.Failure -> throw IllegalStateException(resolution.message)
        }
    }

    private suspend fun requestAuthFromJs(kind: AuthRequestKind): AuthRequestResolution {
        val requestId = UUID.randomUUID().toString()
        val deferred = CompletableDeferred<AuthRequestResolution>()
        pendingRequests[requestId] = PendingAuthRequest(kind, deferred)

        val payload = JSObject().apply {
            put("requestId", requestId)
        }

        withContext(Dispatchers.Main.immediate) {
            plugin.emitBridgeEvent(kind.eventName, payload)
        }

        return try {
            withTimeout(AUTH_REQUEST_TIMEOUT_MS) {
                deferred.await()
            }
        } catch (_: TimeoutCancellationException) {
            AuthRequestResolution.Failure("Timed out waiting for the ${kind.label} listener to reply.")
        } finally {
            pendingRequests.remove(requestId)
        }
    }

    private fun resolvePulseInstance(context: Context, apiUrl: String): Any {
        val pulseClass = loadRequiredClass(PULSE_CLASS_NAME)
        val args = arrayOf(context, apiUrl)

        findCompatibleMethod(pulseClass.methods.toList(), "getInstance", args)?.let { method ->
            return method.invoke(null, *args)
                ?: throw IllegalStateException("Pulse.getInstance(Context, String) returned null.")
        }

        val companion = runCatching {
            pulseClass.getDeclaredField("Companion").apply { isAccessible = true }.get(null)
        }.getOrNull()

        if (companion != null) {
            findCompatibleMethod(companion.javaClass.methods.toList(), "getInstance", args)?.let { method ->
                return method.invoke(companion, *args)
                    ?: throw IllegalStateException("Pulse.Companion.getInstance(Context, String) returned null.")
            }
        }

        throw IllegalStateException("Could not find Pulse.getInstance(Context, String).")
    }

    private fun loadRequiredClass(className: String): Class<*> {
        return try {
            Class.forName(className)
        } catch (error: ClassNotFoundException) {
            throw IllegalStateException(getSdkStatus().message, error)
        }
    }

    private fun isClassAvailable(className: String): Boolean {
        return runCatching { Class.forName(className) }.isSuccess
    }

    private fun invokeCompatibleMethod(target: Any, name: String, vararg args: Any?): Any? {
        val methods = target.javaClass.methods.toList() + target.javaClass.declaredMethods.toList()
        val method = findCompatibleMethod(methods, name, args)
            ?: throw IllegalStateException("Could not find ${target.javaClass.simpleName}.$name with ${args.size} argument(s).")

        method.isAccessible = true
        return method.invoke(target, *args)
    }

    private fun findCompatibleMethod(
        methods: List<Method>,
        name: String,
        args: Array<out Any?>,
    ): Method? {
        return methods.firstOrNull { method ->
            method.name == name &&
                method.parameterTypes.size == args.size &&
                method.parameterTypes.withIndex().all { (index, parameterType) ->
                    val arg = args[index]
                    arg == null || wrapPrimitive(parameterType).isAssignableFrom(arg.javaClass)
                }
        }
    }

    private fun wrapPrimitive(type: Class<*>): Class<*> {
        return when (type) {
            java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
            java.lang.Byte.TYPE -> java.lang.Byte::class.java
            java.lang.Character.TYPE -> java.lang.Character::class.java
            java.lang.Double.TYPE -> java.lang.Double::class.java
            java.lang.Float.TYPE -> java.lang.Float::class.java
            java.lang.Integer.TYPE -> java.lang.Integer::class.java
            java.lang.Long.TYPE -> java.lang.Long::class.java
            java.lang.Short.TYPE -> java.lang.Short::class.java
            else -> type
        }
    }

    private fun normalizeStateName(state: Any?): String {
        if (state == null) {
            return "UNKNOWN"
        }

        if (state is Enum<*>) {
            return state.name
        }

        val raw = state.toString().trim()
        if (raw.isEmpty()) {
            return state.javaClass.simpleName.ifEmpty { "UNKNOWN" }.uppercase()
        }

        return raw
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .uppercase()
            .ifEmpty { state.javaClass.simpleName.ifEmpty { "UNKNOWN" }.uppercase() }
    }

    private inner class PulseAccessTokenProviderHandler : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            return when (method.name) {
                "getAccessToken" -> handleAccessTokenRequest(args)
                "refreshAccessToken" -> handleRefreshTokenRequest(args)
                "toString" -> "CapgoDisqoPulseAccessTokenProvider"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> null
            }
        }

        private fun handleAccessTokenRequest(args: Array<out Any?>?): Any {
            val continuation = requireContinuation(args)
            pluginScope.launch {
                val tokenResult = runCatching { requestAccessToken() }
                continuation.resumeWith(Result.success(tokenResult))
            }
            return COROUTINE_SUSPENDED
        }

        private fun handleRefreshTokenRequest(args: Array<out Any?>?): Any {
            val continuation = requireContinuation(args)
            pluginScope.launch {
                val refreshResult = runCatching { requestRefreshToken() }
                continuation.resumeWith(Result.success(refreshResult))
            }
            return COROUTINE_SUSPENDED
        }

        @Suppress("UNCHECKED_CAST")
        private fun requireContinuation(args: Array<out Any?>?): Continuation<Any?> {
            val continuation = args?.lastOrNull() as? Continuation<Any?>
            return continuation ?: throw IllegalStateException("Pulse callback was invoked without a continuation.")
        }
    }

    companion object {
        private const val TAG = "Disqo"
        private const val AUTH_REQUEST_TIMEOUT_MS = 30_000L
        private const val PULSE_CLASS_NAME = "com.surveyjunkie.pulse.Pulse"
        private const val PULSE_ACCESS_TOKEN_PROVIDER_CLASS = "com.surveyjunkie.pulse.PulseAccessTokenProvider"

        private val REQUIRED_CLASS_NAMES = listOf(
            PULSE_CLASS_NAME,
            PULSE_ACCESS_TOKEN_PROVIDER_CLASS,
        )

        init {
            Logger.debug(TAG, "Disqo bridge loaded.")
        }
    }
}
