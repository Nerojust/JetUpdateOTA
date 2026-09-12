# Contributing to JetUpdateOTA

Thanks for considering a contribution. This project is small on purpose —
please read this before opening a PR so your change lands smoothly.

## Before you start

- **Bug fix or small change:** just open a PR.
- **New feature or anything touching the public API** (`ota-core`'s
  interfaces, `OTAManager`, `OTAConfig`): open an issue first to discuss
  the approach. This library favors a small, well-tested surface over
  breadth — not every reasonable feature belongs in the core modules.

## Development setup

```bash
git clone https://github.com/nerojust/JetUpdateOTA.git
cd JetUpdateOTA
./gradlew build                     # Android modules
cd backend-reference && npm install # backend
```

## Project layout

- `ota-core` — public API and models, pure Kotlin/JVM, no Android
  dependency. Changes here affect every other module.
- `ota-network`, `ota-download` — plain Kotlin/JVM implementation
  modules.
- `ota-install` — the one Android-dependent module (`Context`,
  `PackageManager`, `SharedPreferences`).
- `ota-testing` — fakes for the four collaborator interfaces, shared by
  this repo's own tests and available to consumers.
- `backend-reference` — Node/TypeScript reference server.
- `app` — example app, hosts the composition root.

## Testing expectations

This project is built test-first and every PR is expected to keep that
discipline:

- New behavior needs a new test demonstrating it, written before (or
  alongside) the implementation.
- `ota-core`, `ota-network`, `ota-download`, `ota-testing` use JUnit 5 +
  MockK; run with `./gradlew :<module>:test`.
- `ota-install` mixes JUnit 4 (Robolectric, for anything touching
  `Context`/`PackageManager`) and JUnit 5 (for logic with no Android
  dependency, like `SecurityValidator`) — match the existing pattern for
  the code you're touching, don't introduce a third test runner.
- Backend changes: Jest + Supertest, `cd backend-reference && npm test`.
- A class that's genuinely hard to unit test (real `PackageInstaller`
  sessions, real signed APKs) doesn't need a forced/fake test — but say
  so explicitly in your PR description rather than silently skipping
  coverage.

Run everything before opening a PR:

```bash
./gradlew test && ./gradlew ktlintCheck
cd backend-reference && npm test && npm run lint
```

## Code style

- Kotlin: [ktlint](https://github.com/pinterest/ktlint) default style,
  enforced in CI. Run `./gradlew ktlintFormat` to auto-fix.
- TypeScript: ESLint, enforced in CI. Run `npm run lint -- --fix` in
  `backend-reference/`.
- No new dependency for something the standard library or an
  already-installed dependency already solves.
- Prefer sealed classes/interfaces over exceptions for expected,
  non-exceptional outcomes (see `UpdateCheckResult`, `InstallResult`).

## Commit messages

Describe *why*, not just *what* — the diff already shows what changed.
Squash-merge is fine; a clean, readable final commit message matters
more than a tidy sequence of WIP commits.

## Reporting bugs / requesting features

Use the issue templates. For anything that might be a security
vulnerability, see [SECURITY.md](SECURITY.md) instead of opening a
public issue.
