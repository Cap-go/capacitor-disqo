# @capgo/capacitor-disqo
<a href="https://capgo.app/"><img src="https://raw.githubusercontent.com/Cap-go/capgo/main/assets/capgo_banner.png" alt="Capgo - Instant updates for Capacitor"/></a>

<div align="center">
  <h2><a href="https://capgo.app/?ref=plugin_disqo">➡️ Ship Instant Updates with Capgo</a></h2>
  <h2><a href="https://capgo.app/consulting/?ref=plugin_disqo">Missing a feature? We’ll build the plugin for you 💪</a></h2>
</div>

Private Capacitor bridge for Disqo's Android Pulse SDK.

This repo is intentionally configured for internal/client work first:
- and it expects the private Pulse Android SDK artifact to be supplied by the client.

## What it does

- Wraps the private Android Pulse SDK behind a Capacitor-friendly TypeScript API.
- Lets the host app initialize the SDK, start or stop tracking, deep-link into Android accessibility settings, and query service state.
- Bridges Pulse token callbacks back to JavaScript so the host app can keep using its own auth stack.

## What it does not do

- It does not ship the private Disqo SDK artifact itself.
- It does not implement the client-facing onboarding UI or Google Play disclosures for you.
- It does not automate Play Console declarations, privacy policy updates, or the review video requirement.

## Compatibility

| Plugin version | Capacitor compatibility | Maintained |
| -------------- | ----------------------- | ---------- |
| v8.\*.\*       | v8.\*.\*                | ✅          |
| v7.\*.\*       | v7.\*.\*                | On demand   |
| v6.\*.\*       | v6.\*.\*                | ❌          |

## Install

```bash
bun add @capgo/capacitor-disqo
bunx cap sync android
```

## Private SDK Setup

This plugin is buildable without the client SDK, but the Android runtime bridge only works once the private Pulse artifact is present.

Choose one of these setups before calling `initialize()`:

1. Drop the client-provided AAR into `android/libs/` inside this plugin repo.
2. Replace the local AAR flow in [android/build.gradle](/Users/martindonadieu/Projects/capgo_all/capgo_plugins/capacitor-disqo/android/build.gradle) with the client's internal Maven repository and dependency coordinates.

If the SDK is missing at runtime, `getSdkStatus()` will report that the required classes could not be found.

## Android Notes

- The underlying Pulse SDK is Android-only.
- The host app is still responsible for the prominent disclosure screen, onboarding flow, Play Console declarations, privacy policy updates, and review video.
- This plugin ships default resource strings for:
  - `accessibility_service_label`
  - `tracking_service_description`
  - `tracking_service_summary`
- The host app can override those values in its own `android/app/src/main/res/values/strings.xml`.

## Usage

```typescript
import { Disqo } from '@capgo/capacitor-disqo';

await Disqo.addListener('accessTokenRequested', async ({ requestId }) => {
  const accessToken = await authStore.getPulseToken();
  await Disqo.resolveAccessTokenRequest({
    requestId,
    accessToken,
  });
});

await Disqo.addListener('refreshTokenRequested', async ({ requestId }) => {
  const refreshedSession = await authStore.refreshPulseToken();
  await Disqo.resolveRefreshTokenRequest({
    requestId,
    accessToken: refreshedSession.accessToken,
  });
});

const sdkStatus = await Disqo.getSdkStatus();
if (!sdkStatus.available) {
  throw new Error(sdkStatus.message);
}

await Disqo.initialize({
  apiUrl: 'https://pulse.example.internal',
  accessToken: await authStore.getPulseToken(),
});

await Disqo.start({
  userId: 'member-123',
});

await Disqo.openAccessibilitySettings();

const state = await Disqo.getServiceStateInfo();
console.log(state.enabled, state.state);
```

## Example App

The bundled `example-app/` is wired to:

- register the token callback listeners,
- reuse the token entered in the UI,
- initialize the bridge,
- start or stop tracking,
- open Android accessibility settings,
- and inspect the current service state.

## API

<docgen-index>

* [`getSdkStatus()`](#getsdkstatus)
* [`initialize(...)`](#initialize)
* [`start(...)`](#start)
* [`stop()`](#stop)
* [`updateAccessToken(...)`](#updateaccesstoken)
* [`clearAccessToken()`](#clearaccesstoken)
* [`getServiceStateInfo()`](#getservicestateinfo)
* [`isServiceEnabled()`](#isserviceenabled)
* [`openAccessibilitySettings()`](#openaccessibilitysettings)
* [`refreshConfigs()`](#refreshconfigs)
* [`send()`](#send)
* [`resolveAccessTokenRequest(...)`](#resolveaccesstokenrequest)
* [`rejectAccessTokenRequest(...)`](#rejectaccesstokenrequest)
* [`resolveRefreshTokenRequest(...)`](#resolverefreshtokenrequest)
* [`rejectRefreshTokenRequest(...)`](#rejectrefreshtokenrequest)
* [`getPluginVersion()`](#getpluginversion)
* [`addListener('accessTokenRequested', ...)`](#addlisteneraccesstokenrequested-)
* [`addListener('refreshTokenRequested', ...)`](#addlistenerrefreshtokenrequested-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Capacitor API for the private Disqo Pulse Android SDK.

The host app is still responsible for the onboarding UI, prominent disclosure,
Google Play declarations, and privacy policy updates.

### getSdkStatus()

```typescript
getSdkStatus() => Promise<SdkStatusResult>
```

Check whether the private Android Pulse SDK classes are available at runtime.

Call this before `initialize()` when the SDK artifact is supplied outside of this repo.

**Returns:** <code>Promise&lt;<a href="#sdkstatusresult">SdkStatusResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### initialize(...)

```typescript
initialize(options: DisqoInitializeOptions) => Promise<void>
```

Initialize the native bridge and register the token-provider proxy required by Pulse.

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#disqoinitializeoptions">DisqoInitializeOptions</a></code> |

**Since:** 8.0.0

--------------------


### start(...)

```typescript
start(options: DisqoStartOptions) => Promise<void>
```

Start Pulse tracking for the provided user.

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#disqostartoptions">DisqoStartOptions</a></code> |

**Since:** 8.0.0

--------------------


### stop()

```typescript
stop() => Promise<void>
```

Put Pulse into silent mode and clear user state.

**Since:** 8.0.0

--------------------


### updateAccessToken(...)

```typescript
updateAccessToken(options: UpdateAccessTokenOptions) => Promise<void>
```

Replace the cached JWT used by the native provider bridge.

| Param         | Type                                                                          |
| ------------- | ----------------------------------------------------------------------------- |
| **`options`** | <code><a href="#updateaccesstokenoptions">UpdateAccessTokenOptions</a></code> |

**Since:** 8.0.0

--------------------


### clearAccessToken()

```typescript
clearAccessToken() => Promise<void>
```

Clear the cached JWT. The next native lookup will ask JavaScript for a new token.

**Since:** 8.0.0

--------------------


### getServiceStateInfo()

```typescript
getServiceStateInfo() => Promise<ServiceStateInfo>
```

Return a normalized snapshot of the current Pulse service state.

**Returns:** <code>Promise&lt;<a href="#servicestateinfo">ServiceStateInfo</a>&gt;</code>

**Since:** 8.0.0

--------------------


### isServiceEnabled()

```typescript
isServiceEnabled() => Promise<ServiceEnabledResult>
```

Convenience boolean for the current Pulse service state.

**Returns:** <code>Promise&lt;<a href="#serviceenabledresult">ServiceEnabledResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### openAccessibilitySettings()

```typescript
openAccessibilitySettings() => Promise<void>
```

Open Android accessibility settings so the host app can drive the enablement flow.

**Since:** 8.0.0

--------------------


### refreshConfigs()

```typescript
refreshConfigs() => Promise<void>
```

Force the native Pulse SDK to refresh its remote configuration.

Intended for internal debugging.

**Since:** 8.0.0

--------------------


### send()

```typescript
send() => Promise<void>
```

Force the native Pulse SDK to send its queued events immediately.

Intended for internal debugging.

**Since:** 8.0.0

--------------------


### resolveAccessTokenRequest(...)

```typescript
resolveAccessTokenRequest(options: ResolveAccessTokenRequestOptions) => Promise<void>
```

Resolve a pending `accessTokenRequested` callback with a fresh JWT.

| Param         | Type                                                                                          |
| ------------- | --------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#resolveaccesstokenrequestoptions">ResolveAccessTokenRequestOptions</a></code> |

**Since:** 8.0.0

--------------------


### rejectAccessTokenRequest(...)

```typescript
rejectAccessTokenRequest(options: RejectAccessTokenRequestOptions) => Promise<void>
```

Reject a pending `accessTokenRequested` callback.

| Param         | Type                                                                                        |
| ------------- | ------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#rejectaccesstokenrequestoptions">RejectAccessTokenRequestOptions</a></code> |

**Since:** 8.0.0

--------------------


### resolveRefreshTokenRequest(...)

```typescript
resolveRefreshTokenRequest(options: ResolveRefreshTokenRequestOptions) => Promise<void>
```

Resolve a pending `refreshTokenRequested` callback.

Optionally includes a fresh JWT to cache immediately.

| Param         | Type                                                                                            |
| ------------- | ----------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#resolverefreshtokenrequestoptions">ResolveRefreshTokenRequestOptions</a></code> |

**Since:** 8.0.0

--------------------


### rejectRefreshTokenRequest(...)

```typescript
rejectRefreshTokenRequest(options: RejectRefreshTokenRequestOptions) => Promise<void>
```

Reject a pending `refreshTokenRequested` callback.

| Param         | Type                                                                                          |
| ------------- | --------------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#rejectrefreshtokenrequestoptions">RejectRefreshTokenRequestOptions</a></code> |

**Since:** 8.0.0

--------------------


### getPluginVersion()

```typescript
getPluginVersion() => Promise<PluginVersionResult>
```

Get the native Capacitor plugin version.

**Returns:** <code>Promise&lt;<a href="#pluginversionresult">PluginVersionResult</a>&gt;</code>

**Since:** 8.0.0

--------------------


### addListener('accessTokenRequested', ...)

```typescript
addListener(eventName: 'accessTokenRequested', listenerFunc: (event: AccessTokenRequestedEvent) => void) => Promise<PluginListenerHandle>
```

Listen for native access-token lookups.

| Param              | Type                                                                                                |
| ------------------ | --------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'accessTokenRequested'</code>                                                                 |
| **`listenerFunc`** | <code>(event: <a href="#accesstokenrequestedevent">AccessTokenRequestedEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 8.0.0

--------------------


### addListener('refreshTokenRequested', ...)

```typescript
addListener(eventName: 'refreshTokenRequested', listenerFunc: (event: RefreshTokenRequestedEvent) => void) => Promise<PluginListenerHandle>
```

Listen for native token-refresh requests.

| Param              | Type                                                                                                  |
| ------------------ | ----------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'refreshTokenRequested'</code>                                                                  |
| **`listenerFunc`** | <code>(event: <a href="#refreshtokenrequestedevent">RefreshTokenRequestedEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 8.0.0

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove every active listener registered through the plugin.

**Since:** 8.0.0

--------------------


### Interfaces


#### SdkStatusResult

Result of checking whether the private Disqo Pulse SDK is available on Android.

| Prop                 | Type                  | Description                                                              | Since |
| -------------------- | --------------------- | ------------------------------------------------------------------------ | ----- |
| **`available`**      | <code>boolean</code>  | Whether the required Android Pulse classes were found at runtime.        | 8.0.0 |
| **`missingClasses`** | <code>string[]</code> | Fully qualified class names that were missing when the status check ran. | 8.0.0 |
| **`message`**        | <code>string</code>   | Human-readable guidance that explains how to fix an unavailable SDK.     | 8.0.0 |


#### DisqoInitializeOptions

Initialization options for the private Pulse bridge.

| Prop              | Type                | Description                                                                                                     | Since |
| ----------------- | ------------------- | --------------------------------------------------------------------------------------------------------------- | ----- |
| **`apiUrl`**      | <code>string</code> | Base URL used by the private Pulse SDK.                                                                         | 8.0.0 |
| **`accessToken`** | <code>string</code> | Optional cached JWT that can satisfy the SDK's first token lookup without a round-trip to JavaScript listeners. | 8.0.0 |


#### DisqoStartOptions

Start options for a Pulse session.

| Prop              | Type                | Description                                                           | Since |
| ----------------- | ------------------- | --------------------------------------------------------------------- | ----- |
| **`userId`**      | <code>string</code> | User identifier forwarded to `sdk.start(userId = ...)`.               | 8.0.0 |
| **`accessToken`** | <code>string</code> | Optional access token override to cache immediately before `start()`. | 8.0.0 |


#### UpdateAccessTokenOptions

Update the cached JWT used by the native token provider bridge.

| Prop              | Type                | Description                                      | Since |
| ----------------- | ------------------- | ------------------------------------------------ | ----- |
| **`accessToken`** | <code>string</code> | Latest JWT returned by the host app auth system. | 8.0.0 |


#### ServiceStateInfo

Lightweight view of the current Pulse service state.

The private SDK can enrich this state in future versions, so the plugin exposes both a normalized
`enabled` boolean and a raw string snapshot from the underlying Android object.

| Prop          | Type                 | Description                                                                 | Since |
| ------------- | -------------------- | --------------------------------------------------------------------------- | ----- |
| **`enabled`** | <code>boolean</code> | Whether the accessibility-backed Pulse service is currently enabled.        | 8.0.0 |
| **`state`**   | <code>string</code>  | Best-effort normalized state name derived from the underlying Pulse object. | 8.0.0 |
| **`raw`**     | <code>string</code>  | Raw string representation returned by the native Pulse state object.        | 8.0.0 |


#### ServiceEnabledResult

Boolean wrapper for `isServiceEnabled()`.

| Prop          | Type                 | Description                                      | Since |
| ------------- | -------------------- | ------------------------------------------------ | ----- |
| **`enabled`** | <code>boolean</code> | Whether the underlying Pulse service is enabled. | 8.0.0 |


#### ResolveAccessTokenRequestOptions

Successful response payload for `resolveAccessTokenRequest()`.

| Prop              | Type                | Description                                             | Since |
| ----------------- | ------------------- | ------------------------------------------------------- | ----- |
| **`requestId`**   | <code>string</code> | Correlation id received in the original callback event. | 8.0.0 |
| **`accessToken`** | <code>string</code> | Fresh JWT returned by the host app auth system.         | 8.0.0 |


#### RejectAccessTokenRequestOptions

Error response payload for `rejectAccessTokenRequest()`.

| Prop            | Type                | Description                                             | Since |
| --------------- | ------------------- | ------------------------------------------------------- | ----- |
| **`requestId`** | <code>string</code> | Correlation id received in the original callback event. | 8.0.0 |
| **`message`**   | <code>string</code> | Optional reason returned to the native bridge.          | 8.0.0 |


#### ResolveRefreshTokenRequestOptions

Successful response payload for `resolveRefreshTokenRequest()`.

| Prop              | Type                | Description                                                                           | Since |
| ----------------- | ------------------- | ------------------------------------------------------------------------------------- | ----- |
| **`requestId`**   | <code>string</code> | Correlation id received in the original callback event.                               | 8.0.0 |
| **`accessToken`** | <code>string</code> | Optional fresh JWT to cache before the native bridge retries its access-token lookup. | 8.0.0 |


#### RejectRefreshTokenRequestOptions

Error response payload for `rejectRefreshTokenRequest()`.

| Prop            | Type                | Description                                             | Since |
| --------------- | ------------------- | ------------------------------------------------------- | ----- |
| **`requestId`** | <code>string</code> | Correlation id received in the original callback event. | 8.0.0 |
| **`message`**   | <code>string</code> | Optional reason returned to the native bridge.          | 8.0.0 |


#### PluginVersionResult

Plugin version payload.

| Prop          | Type                | Description                                                 | Since |
| ------------- | ------------------- | ----------------------------------------------------------- | ----- |
| **`version`** | <code>string</code> | Version identifier returned by the platform implementation. | 8.0.0 |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### AccessTokenRequestedEvent

Access-token callback event emitted when the native Pulse provider needs a JWT.

| Prop            | Type                | Description                                                                                               | Since |
| --------------- | ------------------- | --------------------------------------------------------------------------------------------------------- | ----- |
| **`requestId`** | <code>string</code> | Correlation id that must be echoed back to `resolveAccessTokenRequest()` or `rejectAccessTokenRequest()`. | 8.0.0 |


#### RefreshTokenRequestedEvent

Refresh-token callback event emitted when the native Pulse provider asks the host app to refresh auth.

| Prop            | Type                | Description                                                                                                 | Since |
| --------------- | ------------------- | ----------------------------------------------------------------------------------------------------------- | ----- |
| **`requestId`** | <code>string</code> | Correlation id that must be echoed back to `resolveRefreshTokenRequest()` or `rejectRefreshTokenRequest()`. | 8.0.0 |

</docgen-api>
