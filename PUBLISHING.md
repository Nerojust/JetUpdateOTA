# Publishing to Maven Central

The Gradle side is already set up (via the
[`com.vanniktech.maven.publish`](https://github.com/vanniktech/gradle-maven-publish-plugin)
plugin, applied to all five `ota-*` modules) and wired into
[`.github/workflows/release.yml`](.github/workflows/release.yml). What's
left is a one-time setup only a repo owner can do: creating an account,
proving you own the namespace, and generating a signing key.

Do this once. After that, every `git push --tags` with a `v*.*.*` tag
publishes automatically.

## 1. Create a Central Publisher Portal account

Go to [central.sonatype.com](https://central.sonatype.com) and sign up
(this replaced the old OSSRH/JIRA process — if you find instructions
mentioning `oss.sonatype.org` or a JIRA ticket, they're outdated).

## 2. Verify the `io.github.nerojust` namespace

This project publishes under `io.github.nerojust` (see each module's
`coordinates(...)` call in its `build.gradle.kts`). To claim it:

1. In the Central Portal, go to **Namespaces** → **Add Namespace**.
2. Enter `io.github.nerojust`.
3. It'll ask you to prove you control the `nerojust` GitHub account —
   normally by creating a public gist containing a verification code
   it gives you. Follow the on-screen steps.
4. Wait for it to show as verified (usually fast, sometimes a short review).

## 3. Generate a signing key

Maven Central requires every artifact to be GPG-signed.

```bash
gpg --full-generate-key
# RSA and RSA, 4096 bits, no expiration (or a long one) is fine.
# Use your real name/email — it becomes part of the public key record.

gpg --list-secret-keys --keyid-format LONG
# Note the key ID after "sec   rsa4096/" — e.g. sec rsa4096/ABCD1234EF567890

gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EF567890
# Publishes the public key so Maven Central can verify your signatures.

gpg --export-secret-keys --armor ABCD1234EF567890 > private-key.asc
# This file is what CI needs — treat it like a password. Don't commit it.
```

## 4. Get a Central Portal token

In the Central Portal: **Account** → **Generate User Token**. This gives
you a username/password pair used only for publishing (not your login
credentials).

## 5. Add GitHub Actions secrets

Repo → **Settings** → **Secrets and variables** → **Actions** → **New
repository secret**. Add all five:

| Secret name | Value |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | The token username from step 4 |
| `MAVEN_CENTRAL_PASSWORD` | The token password from step 4 |
| `SIGNING_KEY` | The full contents of `private-key.asc` from step 3 |
| `SIGNING_KEY_ID` | The last 8 characters of the key ID from step 3 (e.g. `EF567890`) |
| `SIGNING_KEY_PASSWORD` | The passphrase you set when generating the key |

Delete `private-key.asc` from your machine once it's in GitHub secrets —
it doesn't need to live on disk anywhere after that.

## 6. Cut a release

```bash
git tag v0.1.0
git push origin v0.1.0
```

This triggers `release.yml`, which builds, lints, and tests both
stacks, then runs `./gradlew publishAndReleaseToMavenCentral` for all
five `ota-*` modules using the secrets above. Check the Actions tab —
if it's green, the artifacts show up on
[central.sonatype.com](https://central.sonatype.com) (and then Maven
Central proper) within a few minutes to a few hours, depending on their
sync schedule.

## Bumping the version

Each module's `coordinates("io.github.nerojust", "<name>", "0.1.0")`
call has the version hardcoded. Bump it in all five `build.gradle.kts`
files (`ota-core`, `ota-network`, `ota-download`, `ota-install`,
`ota-testing`) to match the new tag before pushing it, and add an entry
to [CHANGELOG.md](CHANGELOG.md).
