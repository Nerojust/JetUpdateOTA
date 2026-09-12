# Security Policy

JetUpdateOTA handles APK downloads and installation — a compromised
implementation or a bug in the signature-verification path is a real
security issue, not just a bug. Please report vulnerabilities
responsibly.

## Reporting a Vulnerability

**Do not open a public issue for a suspected vulnerability.**

Instead, use GitHub's private vulnerability reporting:
`Security` tab → `Report a vulnerability` on this repository. If that's
unavailable, open a regular issue asking a maintainer to contact you
privately, without describing the vulnerability itself.

Please include:

- The affected module/version.
- Steps to reproduce, or a proof of concept if you have one.
- The potential impact as you understand it (e.g., "bypasses signature
  verification," "allows a downgrade install," "leaks the manifest URL").

## What's in scope

- `ota-install`'s signature verification (`SecurityValidator`,
  `SigningCertificateReader`) and install gating.
- `ota-download`'s checksum verification.
- `ota-network`'s handling of the manifest response (including rollout
  and version-comparison trust).
- The reference backend, if the vulnerability is in code shipped from
  this repo (not your own deployment configuration).

## What's out of scope

- The example app's UI is not exercised as a security boundary.
- The reference backend has no authentication by design (documented as
  a reference implementation, not production-hardened) — reports about
  its lack of auth are welcome as an issue, but aren't a vulnerability
  report in the sense of "this is broken," since it's explicitly scoped
  that way.

## Response

We'll acknowledge reports within a few days and aim to have a fix or a
mitigation plan within 30 days for confirmed issues, coordinating
disclosure timing with the reporter.
