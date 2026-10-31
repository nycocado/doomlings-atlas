# Development

How to build the app and how the code is kept consistent. Most of it runs in CI, so you only need the parts that touch your change. How the code fits together: [architecture.md](architecture.md); which tests a change needs: [testing.md](testing.md).

## Getting Started

1. Install a full JDK 25 (with `jlink`, not a bare JRE), the Android SDK and, to run the app, Android Studio. If a session inherits another `JAVA_HOME` (for example an editor's bare JRE), set it to the JDK 25.
2. Gradle finds the SDK through `local.properties` (`sdk.dir`, written by Android Studio and not in git) or `ANDROID_HOME`. A fresh git worktree has no `local.properties`, so set `ANDROID_HOME` there.
3. Run `./gradlew check`: formatting, lint, unit tests and the project's own checks, the same as CI.
4. Open the project in Android Studio and run the `app` configuration on an emulator or device (API 26 or newer).

## Conventions

- **Tests first in `shared`:** card data, game rules and scoring are written test-first. Each scoring rule gets a test with a real pile and its expected score.
- **Previews in `app`:** every component and screen has a `@PreviewLightDark` preview (light and dark themes). The previews in `app/src/screenshotTest` are screenshot tests: `./gradlew validateDebugScreenshotTest` compares them with the saved images, and after an intended visual change `./gradlew updateDebugScreenshotTest` saves the new ones, which go in the same PR.
- **Style:** Spotless with ktlint formats the code and Android Lint runs with warnings as errors. `./gradlew spotlessApply check` fixes formatting and runs the same checks as CI.
- **Dependencies:** versions live in `gradle/libs.versions.toml`. A new dependency needs a reason in the PR.
- **Permissions:** the app works offline ([ADR 0002](adr/0002-offline-without-backend.md)) except for the update check ([ADR 0012](adr/0012-update-check-from-github-releases.md)), which alone needs `INTERNET` and `REQUEST_INSTALL_PACKAGES`. CI fails if the final manifest has a permission missing from `app/permissions-allowlist.txt`. Adding one needs a reason in the PR.
- **Minimum Android version:** API 26 (Android 8).

## Branches and Merging

- `main` is the only long-lived branch and always builds and passes CI. Work happens on short-lived branches merged by PR ([GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow)); merged branches are deleted.
- Each [ROADMAP.md](../ROADMAP.md) phase is a milestone and ends with a minor release (`v0.1.0` for phase 1). Its issues are the tickets and specs of its work ([CONTRIBUTING.md](../CONTRIBUTING.md#tickets-and-specs)).
- PRs are **squash-merged**, with the PR title plus ` (#N)` as the subject and an empty body. One PR is one commit on `main`: every commit there passed CI, so `git bisect` and reverting a change work commit by commit; the changelog gets one line per PR; and the working commits of a branch, often noisy when an agent writes them, stay out of `main`. The why of a change lives in its PR: `gh pr view <N>`.

## CI

- Every PR and push to `main` runs the CI workflow: Gradle wrapper validation, `spotlessCheck`, `lint`, unit tests, the permission and glossary checks, the screenshot tests, a debug build and, in its own job, the unsigned release build, the only one that runs R8 and resource shrinking.
- On PRs, it also checks that the title follows the [commit format](../CONTRIBUTING.md#commits), since it becomes the squash commit message, and that neither the PR body nor its commits carry AI attribution ([CONTRIBUTING.md](../CONTRIBUTING.md#opening-a-pull-request)).
- On PRs, a dependency review fails on a dependency with a known vulnerability and lists the ones the PR adds. It needs a public repository on the free plan, so it skips until the repository is public. Submitting the PR's Gradle graph needs write permission, which PRs from forks and from Dependabot don't get, so on those it fails until #205 splits it in two workflows.
- Actions are pinned by commit SHA, and the workflows were checked with [zizmor](https://docs.zizmor.sh) at its default level (`uvx zizmor .github/`). Dependabot opens weekly grouped updates for Gradle and Actions, for versions at least 7 days old.

## Releases

`v0.1.0` is a tag with release notes and no APK; the first APK comes with "Going Public", at the end of phase 2 ([ROADMAP.md](../ROADMAP.md#going-public)). A release, in order:

1. `git cliff --tag vX.Y.Z -o CHANGELOG.md` updates `CHANGELOG.md` in a `[docs]` PR, so the tag includes it (`uvx git-cliff` runs it without installing).
2. The maintainer pushes the `vX.Y.Z` tag ([SemVer](https://semver.org)) on `main`, which starts `release.yml`. It runs in the `release` environment, which holds the signing key's secrets and accepts only `v*` tags; the maintainer's approval of each run (required reviewers) comes with the public repository, since GitHub Free doesn't offer it in a private one.
3. The workflow builds the signed APK with the version name from the tag and the version code from its numbers (`1.2.3` → `10203`, so the minor and patch stay below 100), checks its signature, and creates a draft GitHub Release with the APK as `doomlings-atlas-vX.Y.Z.apk`, the name the update check looks for ([ADR 0012](adr/0012-update-check-from-github-releases.md)), and the merged PRs since the previous tag grouped by type, as notes from [git-cliff](https://git-cliff.org) (`cliff.toml`).
4. The maintainer installs the draft's APK on a real device, writes a short summary for players on top of the notes and publishes it.

- Other builds are version `0.0.0`, code `1`; the workflow passes the version as the Gradle property `atlas.versionName`.
- The signing key lives only in the `release` environment's secrets (`RELEASE_KEYSTORE_BASE64`, the PKCS12 file in base64, `RELEASE_STORE_PASSWORD` and `RELEASE_KEY_ALIAS`) and the maintainer's backup, never in the repo. The build signs the release APK only when the Gradle properties `atlas.release.storeFile`, `atlas.release.storePassword` and `atlas.release.keyAlias` are set (in the maintainer's `~/.gradle/gradle.properties` or by the workflow); otherwise it is built unsigned.
