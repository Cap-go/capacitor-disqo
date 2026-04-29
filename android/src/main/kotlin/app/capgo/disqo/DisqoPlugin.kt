package app.capgo.disqo

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@CapacitorPlugin(name = "Disqo")
class DisqoPlugin : Plugin() {
    private val pluginVersion = "8.1.1"
    private val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val implementation = Disqo(this, pluginScope)

    override fun handleOnDestroy() {
        implementation.dispose()
        pluginScope.cancel()
        super.handleOnDestroy()
    }

    @PluginMethod
    fun getSdkStatus(call: PluginCall) {
        call.resolve(implementation.getSdkStatus().toJsObject())
    }

    @PluginMethod
    fun initialize(call: PluginCall) {
        val apiUrl = normalizedValue(call, "apiUrl")
        if (apiUrl == null) {
            call.reject("apiUrl is required")
            return
        }

        try {
            implementation.initialize(context.applicationContext, apiUrl)
            implementation.updateAccessToken(normalizedValue(call, "accessToken"))
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun start(call: PluginCall) {
        val userId = normalizedValue(call, "userId")
        if (userId == null) {
            call.reject("userId is required")
            return
        }

        try {
            implementation.updateAccessToken(normalizedValue(call, "accessToken"))
            implementation.start(userId)
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun stop(call: PluginCall) {
        try {
            implementation.stop()
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun updateAccessToken(call: PluginCall) {
        val accessToken = normalizedValue(call, "accessToken")
        if (accessToken == null) {
            call.reject("accessToken is required")
            return
        }

        implementation.updateAccessToken(accessToken)
        call.resolve()
    }

    @PluginMethod
    fun clearAccessToken(call: PluginCall) {
        implementation.clearAccessToken()
        call.resolve()
    }

    @PluginMethod
    fun getServiceStateInfo(call: PluginCall) {
        try {
            call.resolve(implementation.getServiceStateInfo().toJsObject())
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun isServiceEnabled(call: PluginCall) {
        try {
            val result = JSObject().apply {
                put("enabled", implementation.isServiceEnabled())
            }
            call.resolve(result)
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun openAccessibilitySettings(call: PluginCall) {
        try {
            implementation.openAccessibilitySettings(context.applicationContext)
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun refreshConfigs(call: PluginCall) {
        try {
            implementation.refreshConfigs()
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun send(call: PluginCall) {
        try {
            implementation.send()
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun resolveAccessTokenRequest(call: PluginCall) {
        val requestId = normalizedValue(call, "requestId")
        val accessToken = normalizedValue(call, "accessToken")
        if (requestId == null) {
            call.reject("requestId is required")
            return
        }
        if (accessToken == null) {
            call.reject("accessToken is required")
            return
        }

        try {
            implementation.resolveAccessTokenRequest(requestId, accessToken)
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun rejectAccessTokenRequest(call: PluginCall) {
        val requestId = normalizedValue(call, "requestId")
        if (requestId == null) {
            call.reject("requestId is required")
            return
        }

        try {
            implementation.rejectAccessTokenRequest(requestId, normalizedValue(call, "message"))
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun resolveRefreshTokenRequest(call: PluginCall) {
        val requestId = normalizedValue(call, "requestId")
        if (requestId == null) {
            call.reject("requestId is required")
            return
        }

        try {
            implementation.resolveRefreshTokenRequest(requestId, normalizedValue(call, "accessToken"))
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun rejectRefreshTokenRequest(call: PluginCall) {
        val requestId = normalizedValue(call, "requestId")
        if (requestId == null) {
            call.reject("requestId is required")
            return
        }

        try {
            implementation.rejectRefreshTokenRequest(requestId, normalizedValue(call, "message"))
            call.resolve()
        } catch (error: Exception) {
            rejectCall(call, error)
        }
    }

    @PluginMethod
    fun getPluginVersion(call: PluginCall) {
        val result = JSObject().apply {
            put("version", pluginVersion)
        }
        call.resolve(result)
    }

    private fun normalizedValue(call: PluginCall, key: String): String? {
        return call.getString(key)?.trim()?.takeIf { it.isNotEmpty() }
    }

    internal fun emitBridgeEvent(eventName: String, payload: JSObject) {
        notifyListeners(eventName, payload, true)
    }

    private fun rejectCall(call: PluginCall, error: Exception) {
        call.reject(error.message ?: "An unknown error occurred.", null, error)
    }

    private fun SdkStatus.toJsObject(): JSObject {
        val missingClassesArray = JSArray()
        missingClasses.forEach { missingClass ->
            missingClassesArray.put(missingClass)
        }

        return JSObject().apply {
            put("available", available)
            put("message", message)
            put("missingClasses", missingClassesArray)
        }
    }

    private fun ServiceStateSnapshot.toJsObject(): JSObject {
        return JSObject().apply {
            put("enabled", enabled)
            put("state", state)
            put("raw", raw)
        }
    }
}
