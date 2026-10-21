# Contributing

Thanks for helping with Doomlings Atlas! A few rules keep the project healthy and within what Doomlings LLC allowed.

## Ways to Contribute

You don't need to code to help:

- **Card data:** report or fix wrong card names, effects or point values.
- **Translations:** translate or review card text, the tutorial and the app interface.
- **Card photos:** share photos of your own cards (good light, different angles, tables and sleeves) to improve card recognition.
- **Bug reports:** tell us when something breaks or a score comes out wrong.
- **Ideas:** suggest features or improvements.
- **Code:** fix bugs or build features.

## Licensing

- The source code is licensed under the [GPL-3.0](LICENSE). By contributing, you agree your contributions are licensed under it too.
- Card images, card text, rulebook content and the Doomlings name are © Doomlings LLC, used with permission, and are **not** covered by the GPL-3.0.
- The permission was granted to this project only. Forks do not inherit it and must ask Doomlings LLC themselves.

## Doomlings Assets

Doomlings LLC gave permission for this app (2026-10-15) under the fan art terms of their [Terms of Service](https://www.doomlings.com/policies/terms-of-service) (§1.6). To keep that permission:

- Keep Doomlings assets apart from the code (e.g. `assets/doomlings/` with its own notice).
- Never use the Doomlings logo.
- Always keep the notice "Unofficial, not affiliated with or endorsed by Doomlings LLC" visible in the app and its store listings.
- No paid features and no ads.
- Card data (names, effects, points) must be traceable to a reliable source: the physical cards, official material or checked community assets. Never guess or invent it.
- Card images in issues, PRs or datasets must be photos of cards you own or official images already in the repo.

## Translations

- Portuguese and Spanish are reviewed by the maintainer.
- Other languages are welcome as community contributions, ideally with a second native speaker reviewing.
- If Doomlings releases an official translation, its terminology wins.
- Game terms stay as printed on the cards, in English: Trait, Age, Catastrophe, Dominant, World's End, Gene Pool and the like.
- One word per concept. Each language has a glossary in [`docs/glossary`](docs/glossary): the terms that stay in English (`keep`) and, for every recurring concept, the word to use and the synonyms to avoid. `./gradlew check` fails if an avoided word shows up. To use a new term, add it to the glossary first.

## Opening an Issue

1. Search existing issues first; if one already covers it, comment there instead.
2. One problem or idea per issue, with a clear title.
3. Include what applies:
   - **Bug:** steps to reproduce, what you expected, what happened, device and app version, screenshots if useful.
   - **Wrong score:** the cards on the table and the score you expected versus the one shown.
   - **Card data:** card name, set, what is wrong and a photo of your card or a link to a reliable source.
   - **Translation:** language, where the text appears and your suggested wording.

## Opening a Pull Request

1. For anything beyond a small fix, open or comment on an issue first so we agree on the approach.
2. Fork the repo and create a branch from `main`.
3. Keep the PR focused on one change, with a title in the [commit format](#commits) (CI checks it).
4. Open the PR describing what changed, why and how you tested it, and link the issue (`Closes #123`).

CI checks formatting, lint and tests and tells you if anything needs fixing. The maintainer squash-merges the PR once CI passes. [Development](#development) explains the checks if you work on code.

You are responsible for every line you submit, however it was written. Make sure you understand it and can explain it.

## Development

How the code is kept consistent. Most of it runs in CI, so you only need the parts that touch your change.

- **Tests first in `shared`:** card data, game rules and scoring are written test-first. Each scoring rule gets a test with a real pile and its expected score.
- **Previews in `app`:** every component and screen has a `@PreviewLightDark` preview (light and dark themes). The previews in `app/src/screenshotTest` are screenshot tests: `./gradlew validateDebugScreenshotTest` compares them with the saved images, and after an intended visual change `./gradlew updateDebugScreenshotTest` saves the new ones, which go in the same PR.
- **Style:** Spotless with ktlint formats the code and Android Lint runs with warnings as errors. `./gradlew spotlessApply check` fixes formatting and runs the same checks as CI.
- **Dependencies:** versions live in `gradle/libs.versions.toml`. A new dependency needs a reason in the PR.
- **Permissions:** the app declares no permissions, as it works fully offline ([ADR 0002](docs/adr/0002-offline-without-backend.md)). CI fails if the final manifest has a permission missing from `app/permissions-allowlist.txt`. Adding one needs a reason in the PR.
- **Minimum Android version:** API 26 (Android 8).

### Branches and Issues

- `main` is the only long-lived branch and always builds and passes CI. Work happens on short-lived branches merged by PR ([GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow)); merged branches are deleted.
- Each [ROADMAP.md](ROADMAP.md) phase is a milestone and ends with a minor release (`v0.1.0` for phase 1). Its issues are small, each with what "done" means.
- Labels: `bug`, `card-data`, `translation` and `scoring`.

### CI and Releases

- Every PR and push to `main` runs one GitHub Actions workflow: Gradle wrapper validation, `spotlessCheck`, `lint`, unit tests, the permission check, the screenshot tests and a debug build. The PR title must follow the commit format, since it becomes the squash commit message.
- Actions are pinned by commit SHA. Dependabot opens weekly grouped updates for Gradle and Actions.
- Releases (not set up yet; planned for the first release) come from a `vX.Y.Z` tag ([SemVer](https://semver.org)): the version name comes from the tag and the version code from its numbers (`1.2.3` → `10203`). The release workflow runs in the `release` environment, after the maintainer approves it, and creates a draft GitHub Release with the signed APK and the merged PRs grouped by type, as notes from [git-cliff](https://git-cliff.org) (`cliff.toml`). The maintainer writes a short summary for players on top and publishes it. Before tagging, `git cliff --tag vX.Y.Z -o CHANGELOG.md` updates `CHANGELOG.md` in a `[docs]` PR, so the tag includes it (`uvx git-cliff` runs it without installing). The signing key lives only in GitHub Secrets and the maintainer's backup, never in the repo. The build signs the release APK only when the Gradle properties `atlas.release.storeFile`, `atlas.release.storePassword` and `atlas.release.keyAlias` are set (in the maintainer's `~/.gradle/gradle.properties` or by the workflow); otherwise it is built unsigned.

## Documentation

- [ROADMAP.md](ROADMAP.md): scope and order of work.
- [docs/adr](docs/adr): architecture decisions. Read them before proposing structural changes; changing one means a new ADR, following [docs/adr/README.md](docs/adr/README.md).

## Commits

- Format: `[type] subject`, no scope prefix.
- Types: `feat` (new feature), `fix` (bug fix), `perf` (performance), `refactor` (no behavior change), `chore` (tooling, deps), `docs` (documentation), `update` (content/data update), `media` (images and other media).
- Subject in English, lowercase, ~72 chars max, no action verbs (the type is the action): `[feat] card compendium search`, not `[feat] add card compendium search`.
- Body only when the _why_ is not obvious.
