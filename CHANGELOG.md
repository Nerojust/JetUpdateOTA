# Changelog

All notable changes to this project are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); this project
does not yet follow strict semver (pre-1.0).

## [Unreleased]

### Planned

- Maven Central publishing.
- A real install-completion signal (`BroadcastReceiver` for
  `PackageInstaller` session status) and a corresponding state-reset
  path — currently `pendingVersionCode`/`downloadedApkPath` are never
  cleared after a successful install, since there's no way to confirm
  completion yet.
- Instrumented/device tests for the real install-commit path
  (`PackageInstallerUpdateInstaller`'s real `PackageManager`/
  `PackageInstaller` interaction is untestable in a unit test and is
  currently only gated-path tested).
- Example app update-checking UI.

## [0.1.0] - 2026-09-12

### Added

- `ota-core`: `OTAManager`, `OTAConfig`, the four collaborator
  interfaces (`UpdateChecker`, `UpdateDownloader`, `UpdateInstaller`,
  `UpdateStateStore`), and the sealed result/model types.
- `ota-network`: `HttpUpdateChecker` — manifest fetch over HTTPS,
  server-authoritative rollout and version decisions.
- `ota-download`: `HttpUpdateDownloader` — resumable download via HTTP
  Range requests, SHA-256 checksum verification.
- `ota-install`: `SecurityValidator` (signature comparison via an
  injectable `SigningCertificateReader` seam), `SharedPrefsUpdateStateStore`,
  `PackageInstallerUpdateInstaller` (PackageInstaller session on API 29+,
  intent-based fallback below).
- `ota-testing`: in-memory fakes of all four collaborator interfaces.
- Node/TypeScript reference backend (`backend-reference/`): manifest and
  analytics endpoints, local-disk storage, a CLI publish script.
- CI for both the Android modules and the backend.

[Unreleased]: https://github.com/nerojust/JetUpdateOTA/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/nerojust/JetUpdateOTA/releases/tag/v0.1.0
