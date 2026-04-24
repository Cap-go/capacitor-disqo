import type { PluginListenerHandle } from '@capacitor/core';

/**
 * Plugin version payload.
 *
 * @since 8.0.0
 */
export interface PluginVersionResult {
  /**
   * Version identifier returned by the platform implementation.
   *
   * @since 8.0.0
   */
  version: string;
}

/**
 * Result of checking whether the private Disqo Pulse SDK is available on Android.
 *
 * @since 8.0.0
 */
export interface SdkStatusResult {
  /**
   * Whether the required Android Pulse classes were found at runtime.
   *
   * @since 8.0.0
   */
  available: boolean;

  /**
   * Fully qualified class names that were missing when the status check ran.
   *
   * @since 8.0.0
   */
  missingClasses: string[];

  /**
   * Human-readable guidance that explains how to fix an unavailable SDK.
   *
   * @since 8.0.0
   */
  message: string;
}

/**
 * Initialization options for the private Pulse bridge.
 *
 * @since 8.0.0
 */
export interface DisqoInitializeOptions {
  /**
   * Base URL used by the private Pulse SDK.
   *
   * @example "https://pulse.example.internal"
   * @since 8.0.0
   */
  apiUrl: string;

  /**
   * Optional cached JWT that can satisfy the SDK's first token lookup without a round-trip to JavaScript listeners.
   *
   * @since 8.0.0
   */
  accessToken?: string;
}

/**
 * Start options for a Pulse session.
 *
 * @since 8.0.0
 */
export interface DisqoStartOptions {
  /**
   * User identifier forwarded to `sdk.start(userId = ...)`.
   *
   * @since 8.0.0
   */
  userId: string;

  /**
   * Optional access token override to cache immediately before `start()`.
   *
   * @since 8.0.0
   */
  accessToken?: string;
}

/**
 * Update the cached JWT used by the native token provider bridge.
 *
 * @since 8.0.0
 */
export interface UpdateAccessTokenOptions {
  /**
   * Latest JWT returned by the host app auth system.
   *
   * @since 8.0.0
   */
  accessToken: string;
}

/**
 * Lightweight view of the current Pulse service state.
 *
 * The private SDK can enrich this state in future versions, so the plugin exposes both a normalized
 * `enabled` boolean and a raw string snapshot from the underlying Android object.
 *
 * @since 8.0.0
 */
export interface ServiceStateInfo {
  /**
   * Whether the accessibility-backed Pulse service is currently enabled.
   *
   * @since 8.0.0
   */
  enabled: boolean;

  /**
   * Best-effort normalized state name derived from the underlying Pulse object.
   *
   * @example "ENABLED"
   * @since 8.0.0
   */
  state: string;

  /**
   * Raw string representation returned by the native Pulse state object.
   *
   * @since 8.0.0
   */
  raw?: string;
}

/**
 * Access-token callback event emitted when the native Pulse provider needs a JWT.
 *
 * @since 8.0.0
 */
export interface AccessTokenRequestedEvent {
  /**
   * Correlation id that must be echoed back to `resolveAccessTokenRequest()` or `rejectAccessTokenRequest()`.
   *
   * @since 8.0.0
   */
  requestId: string;
}

/**
 * Refresh-token callback event emitted when the native Pulse provider asks the host app to refresh auth.
 *
 * @since 8.0.0
 */
export interface RefreshTokenRequestedEvent {
  /**
   * Correlation id that must be echoed back to `resolveRefreshTokenRequest()` or `rejectRefreshTokenRequest()`.
   *
   * @since 8.0.0
   */
  requestId: string;
}

/**
 * Successful response payload for `resolveAccessTokenRequest()`.
 *
 * @since 8.0.0
 */
export interface ResolveAccessTokenRequestOptions {
  /**
   * Correlation id received in the original callback event.
   *
   * @since 8.0.0
   */
  requestId: string;

  /**
   * Fresh JWT returned by the host app auth system.
   *
   * @since 8.0.0
   */
  accessToken: string;
}

/**
 * Error response payload for `rejectAccessTokenRequest()`.
 *
 * @since 8.0.0
 */
export interface RejectAccessTokenRequestOptions {
  /**
   * Correlation id received in the original callback event.
   *
   * @since 8.0.0
   */
  requestId: string;

  /**
   * Optional reason returned to the native bridge.
   *
   * @since 8.0.0
   */
  message?: string;
}

/**
 * Successful response payload for `resolveRefreshTokenRequest()`.
 *
 * @since 8.0.0
 */
export interface ResolveRefreshTokenRequestOptions {
  /**
   * Correlation id received in the original callback event.
   *
   * @since 8.0.0
   */
  requestId: string;

  /**
   * Optional fresh JWT to cache before the native bridge retries its access-token lookup.
   *
   * @since 8.0.0
   */
  accessToken?: string;
}

/**
 * Error response payload for `rejectRefreshTokenRequest()`.
 *
 * @since 8.0.0
 */
export interface RejectRefreshTokenRequestOptions {
  /**
   * Correlation id received in the original callback event.
   *
   * @since 8.0.0
   */
  requestId: string;

  /**
   * Optional reason returned to the native bridge.
   *
   * @since 8.0.0
   */
  message?: string;
}

/**
 * Boolean wrapper for `isServiceEnabled()`.
 *
 * @since 8.0.0
 */
export interface ServiceEnabledResult {
  /**
   * Whether the underlying Pulse service is enabled.
   *
   * @since 8.0.0
   */
  enabled: boolean;
}

/**
 * Capacitor API for the private Disqo Pulse Android SDK.
 *
 * The host app is still responsible for the onboarding UI, prominent disclosure,
 * Google Play declarations, and privacy policy updates.
 *
 * @since 8.0.0
 */
export interface DisqoPlugin {
  /**
   * Check whether the private Android Pulse SDK classes are available at runtime.
   *
   * Call this before `initialize()` when the SDK artifact is supplied outside of this repo.
   *
   * @since 8.0.0
   */
  getSdkStatus(): Promise<SdkStatusResult>;

  /**
   * Initialize the native bridge and register the token-provider proxy required by Pulse.
   *
   * @since 8.0.0
   */
  initialize(options: DisqoInitializeOptions): Promise<void>;

  /**
   * Start Pulse tracking for the provided user.
   *
   * @since 8.0.0
   */
  start(options: DisqoStartOptions): Promise<void>;

  /**
   * Put Pulse into silent mode and clear user state.
   *
   * @since 8.0.0
   */
  stop(): Promise<void>;

  /**
   * Replace the cached JWT used by the native provider bridge.
   *
   * @since 8.0.0
   */
  updateAccessToken(options: UpdateAccessTokenOptions): Promise<void>;

  /**
   * Clear the cached JWT. The next native lookup will ask JavaScript for a new token.
   *
   * @since 8.0.0
   */
  clearAccessToken(): Promise<void>;

  /**
   * Return a normalized snapshot of the current Pulse service state.
   *
   * @since 8.0.0
   */
  getServiceStateInfo(): Promise<ServiceStateInfo>;

  /**
   * Convenience boolean for the current Pulse service state.
   *
   * @since 8.0.0
   */
  isServiceEnabled(): Promise<ServiceEnabledResult>;

  /**
   * Open Android accessibility settings so the host app can drive the enablement flow.
   *
   * @since 8.0.0
   */
  openAccessibilitySettings(): Promise<void>;

  /**
   * Force the native Pulse SDK to refresh its remote configuration.
   *
   * Intended for internal debugging.
   *
   * @since 8.0.0
   */
  refreshConfigs(): Promise<void>;

  /**
   * Force the native Pulse SDK to send its queued events immediately.
   *
   * Intended for internal debugging.
   *
   * @since 8.0.0
   */
  send(): Promise<void>;

  /**
   * Resolve a pending `accessTokenRequested` callback with a fresh JWT.
   *
   * @since 8.0.0
   */
  resolveAccessTokenRequest(options: ResolveAccessTokenRequestOptions): Promise<void>;

  /**
   * Reject a pending `accessTokenRequested` callback.
   *
   * @since 8.0.0
   */
  rejectAccessTokenRequest(options: RejectAccessTokenRequestOptions): Promise<void>;

  /**
   * Resolve a pending `refreshTokenRequested` callback.
   *
   * Optionally includes a fresh JWT to cache immediately.
   *
   * @since 8.0.0
   */
  resolveRefreshTokenRequest(options: ResolveRefreshTokenRequestOptions): Promise<void>;

  /**
   * Reject a pending `refreshTokenRequested` callback.
   *
   * @since 8.0.0
   */
  rejectRefreshTokenRequest(options: RejectRefreshTokenRequestOptions): Promise<void>;

  /**
   * Get the native Capacitor plugin version.
   *
   * @since 8.0.0
   */
  getPluginVersion(): Promise<PluginVersionResult>;

  /**
   * Listen for native access-token lookups.
   *
   * @since 8.0.0
   */
  addListener(
    eventName: 'accessTokenRequested',
    listenerFunc: (event: AccessTokenRequestedEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Listen for native token-refresh requests.
   *
   * @since 8.0.0
   */
  addListener(
    eventName: 'refreshTokenRequested',
    listenerFunc: (event: RefreshTokenRequestedEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Remove every active listener registered through the plugin.
   *
   * @since 8.0.0
   */
  removeAllListeners(): Promise<void>;
}
