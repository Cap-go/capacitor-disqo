import { WebPlugin } from '@capacitor/core';

import type {
  DisqoInitializeOptions,
  DisqoPlugin,
  DisqoStartOptions,
  PluginVersionResult,
  RejectAccessTokenRequestOptions,
  RejectRefreshTokenRequestOptions,
  ResolveAccessTokenRequestOptions,
  ResolveRefreshTokenRequestOptions,
  SdkStatusResult,
  UpdateAccessTokenOptions,
} from './definitions';

export class DisqoWeb extends WebPlugin implements DisqoPlugin {
  async getSdkStatus(): Promise<SdkStatusResult> {
    return {
      available: false,
      missingClasses: [],
      message: 'Disqo Pulse is only available on Android.',
    };
  }

  async initialize(_options: DisqoInitializeOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async start(_options: DisqoStartOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async stop(): Promise<void> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async updateAccessToken(_options: UpdateAccessTokenOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async clearAccessToken(): Promise<void> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async getServiceStateInfo(): Promise<{ enabled: boolean; state: string; raw?: string }> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async isServiceEnabled(): Promise<{ enabled: boolean }> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async openAccessibilitySettings(): Promise<void> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async refreshConfigs(): Promise<void> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async send(): Promise<void> {
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async resolveAccessTokenRequest(_options: ResolveAccessTokenRequestOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async rejectAccessTokenRequest(_options: RejectAccessTokenRequestOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async resolveRefreshTokenRequest(_options: ResolveRefreshTokenRequestOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async rejectRefreshTokenRequest(_options: RejectRefreshTokenRequestOptions): Promise<void> {
    void _options;
    throw this.unimplemented('Disqo Pulse is only available on Android.');
  }

  async getPluginVersion(): Promise<PluginVersionResult> {
    return {
      version: 'web',
    };
  }
}
