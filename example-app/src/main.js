import './style.css';
import { Disqo } from '@capgo/capacitor-disqo';

const output = document.getElementById('plugin-output');
const apiUrlInput = document.getElementById('api-url');
const userIdInput = document.getElementById('user-id');
const accessTokenInput = document.getElementById('access-token');
const sdkStatusButton = document.getElementById('get-sdk-status');
const versionButton = document.getElementById('get-version');
const initializeButton = document.getElementById('initialize');
const startButton = document.getElementById('start');
const stopButton = document.getElementById('stop');
const openSettingsButton = document.getElementById('open-settings');
const getStateButton = document.getElementById('get-state');

const setOutput = (value) => {
  output.textContent = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
};

const getAccessToken = () => accessTokenInput.value.trim();
const getApiUrl = () => apiUrlInput.value.trim();
const getUserId = () => userIdInput.value.trim();

const run = async (label, task) => {
  try {
    const result = await task();
    setOutput({ action: label, result: result ?? 'ok' });
  } catch (error) {
    setOutput({ action: label, error: error?.message ?? String(error) });
  }
};

const registerNativeCallbacks = async () => {
  await Disqo.addListener('accessTokenRequested', async ({ requestId }) => {
    const accessToken = getAccessToken();
    if (!accessToken) {
      await Disqo.rejectAccessTokenRequest({
        requestId,
        message: 'Set an access token in the example app first.',
      });
      setOutput({ event: 'accessTokenRequested', requestId, resolved: false });
      return;
    }

    await Disqo.resolveAccessTokenRequest({
      requestId,
      accessToken,
    });
    setOutput({ event: 'accessTokenRequested', requestId, resolved: true });
  });

  await Disqo.addListener('refreshTokenRequested', async ({ requestId }) => {
    const accessToken = getAccessToken();
    if (!accessToken) {
      await Disqo.rejectRefreshTokenRequest({
        requestId,
        message: 'Set an access token in the example app before testing refresh callbacks.',
      });
      setOutput({ event: 'refreshTokenRequested', requestId, resolved: false });
      return;
    }

    await Disqo.resolveRefreshTokenRequest({
      requestId,
      accessToken,
    });
    setOutput({ event: 'refreshTokenRequested', requestId, resolved: true });
  });
};

registerNativeCallbacks().catch((error) => {
  setOutput({ action: 'registerNativeCallbacks', error: error?.message ?? String(error) });
});

sdkStatusButton.addEventListener('click', async () => {
  await run('getSdkStatus', async () => Disqo.getSdkStatus());
});

versionButton.addEventListener('click', async () => {
  await run('getPluginVersion', async () => Disqo.getPluginVersion());
});

initializeButton.addEventListener('click', async () => {
  await run('initialize', async () =>
    Disqo.initialize({
      apiUrl: getApiUrl(),
      accessToken: getAccessToken() || undefined,
    }),
  );
});

startButton.addEventListener('click', async () => {
  await run('start', async () =>
    Disqo.start({
      userId: getUserId(),
      accessToken: getAccessToken() || undefined,
    }),
  );
});

stopButton.addEventListener('click', async () => {
  await run('stop', async () => Disqo.stop());
});

openSettingsButton.addEventListener('click', async () => {
  await run('openAccessibilitySettings', async () => Disqo.openAccessibilitySettings());
});

getStateButton.addEventListener('click', async () => {
  await run('getServiceStateInfo', async () => Disqo.getServiceStateInfo());
});
