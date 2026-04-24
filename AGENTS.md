# AGENTS.md

Guidance for contributors working on `@capgo/capacitor-disqo`.

## Scope

- This repository wraps the private Disqo Pulse Android SDK for Capacitor.
- Keep the repository and package private until the client SDK artifact, Play Console declarations, and public docs are ready.
- The Android implementation is the source of truth. iOS and web stay as unsupported stubs unless the client ships those platforms.

## Private SDK Wiring

- The Disqo SDK is not fetched from a public Maven repository in this repo.
- During development, either:
  - place the private SDK AAR in `android/libs/`, or
  - replace the local AAR flow with the client's internal Maven repository setup.
- The Android bridge is intentionally reflection-based so the repo still builds when the private SDK artifact is absent.

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
- Before any future public release, remove `"private": true` from `package.json` and revisit the tag publish workflow.
