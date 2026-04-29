# AGENTS.md

Guidance for contributors working on `@capgo/capacitor-disqo`.

## Scope

- This repository wraps the Disqo Pulse Android SDK for Capacitor.
- The Android implementation is the source of truth. iOS and web stay as unsupported stubs unless the client ships those platforms.

## SDK Wiring

- Keep the docs and packaging aligned with the shipped integration. Do not document placeholder client-side SDK installation steps unless the plugin genuinely requires them.
- The Android bridge is currently reflection-based, so keep the runtime messaging accurate about whether the underlying Pulse classes are available in the build.

## Workflow

```bash
bun install
bun run fmt
bun run build
bun run verify
```

- Use `bun`/`bunx` only.
- Keep `README.md` aligned with `src/definitions.ts`.
- Do not edit `CHANGELOG.md` manually.
- Keep package publish settings and the tag publish workflow aligned with the current release policy.
