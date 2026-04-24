import { registerPlugin } from '@capacitor/core';

import type { DisqoPlugin } from './definitions';

const Disqo = registerPlugin<DisqoPlugin>('Disqo', {
  web: () => import('./web').then((m) => new m.DisqoWeb()),
});

export * from './definitions';
export { Disqo };
