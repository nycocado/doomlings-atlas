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
- **Permissions:** the app declares no permissions, as it works fully offline ([ADR 0002](adr/0002-offline-without-backend.md)). CI fails if the final manifest has a permission missing from `app/permissions-allowlist.txt`. Adding one needs a reason in the PR.
- **Minimum Android version:** API 26 (Android 8).

## Branches and Merging

- `main` is the only long-lived branch and always builds and passes CI. Work happens on short-lived branches merged by PR ([GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow)); merged branches are deleted.
- Each [ROADMAP.md](../ROADMAP.md) phase is a milestone and ends with a minor release (`v0.1.0` for phase 1). Its issues are the tickets and specs of its work ([CONTRIBUTING.md](../CONTRIBUTING.md#tickets-and-specs)).
- PRs are **squash-merged**, with the PR title plus ` (#N)` as the subject and an empty body. One PR is one commit on `main`: every commit there passed CI, so `git bisect` and reverting a change work commit by commit; the changelog gets one line per PR; and the working commits of a branch, often noisy when an agent writes them, stay out of `main`. The why of a change lives in its PR: `gh pr view <N>`.

## CI

- Every PR and push to `main` runs one GitHub Actions workflow: Gradle wrapper validation, `spotlessCheck`, `lint`, unit tests, the permission and glossary checks, the screenshot tests and a debug build.
- On PRs, it also checks that the title follows the [commit format](../CONTRIBUTING.md#commits), since it becomes the squash commit message, and that neither the PR body nor its commits carry AI attribution ([CONTRIBUTING.md](../CONTRIBUTING.md#opening-a-pull-request)).
- Actions are pinned by commit SHA. Dependabot opens weekly grouped updates for Gradle and Actions.

## Releases

There is no release workflow yet. `v0.1.0` is a tag with release notes and no APK; the first APK and the workflow below come with "Going Public", at the end of phase 2 ([ROADMAP.md](../ROADMAP.md#going-public)), and until then `versionCode` and `versionName` stay fixed.

The planned flow:

- A `vX.Y.Z` tag ([SemVer](https://semver.org)) starts it: the version name comes from the tag and the version code from its numbers (`1.2.3` → `10203`).
- The release workflow runs in the `release` environment, after the maintainer approves it, and creates a draft GitHub Release with the signed APK and the merged PRs grouped by type, as notes from [git-cliff](https://git-cliff.org) (`cliff.toml`). The maintainer writes a short summary for players on top and publishes it.
- Before tagging, `git cliff --tag vX.Y.Z -o CHANGELOG.md` updates `CHANGELOG.md` in a `[docs]` PR, so the tag includes it (`uvx git-cliff` runs it without installing).
- The signing key lives only in GitHub Secrets and the maintainer's backup, never in the repo. The build signs the release APK only when the Gradle properties `atlas.release.storeFile`, `atlas.release.storePassword` and `atlas.release.keyAlias` are set (in the maintainer's `~/.gradle/gradle.properties` or by the workflow); otherwise it is built unsigned.
