# AGENTS.md

Guidance for AI agents in this repo. Follow [CONTRIBUTING.md](CONTRIBUTING.md) first; this file only adds agent-specific rules.

## Project

Doomlings Atlas: unofficial companion app for the Doomlings card game.

- `shared/`: Kotlin Multiplatform module with the app logic (card data, rules, scoring). No Android or Compose code.
- `app/`: Android app in Jetpack Compose, on the project's own design system ([ADR 0006](docs/adr/0006-own-design-system.md)).
- `./gradlew check` runs formatting, lint, tests and the permission check, the same as CI. The build needs a full JDK 25 (with `jlink`), not a bare JRE.

## Rules

- Never invent card names, effects or point values. If card data is missing, ask instead of filling gaps.
- Never commit Doomlings assets outside the asset folder or without its notice (see CONTRIBUTING.md).
- No `Co-Authored-By` trailer or any other AI attribution in commits, PRs or code.
- Read files before editing them. Keep changes scoped to the task.
- Never push to `main`. Work on a branch per task and open a PR; it merges only with CI green.
- Never edit `app/permissions-allowlist.txt` or add a dependency without explaining why in the PR.
- Never touch signing keys, secrets or release workflows unless asked.
- Before writing code for a generic problem (parsing, text, dates, files, collections, UI patterns), use what already exists, in this order: the Kotlin or Java standard library, Android, Compose and Gradle APIs, a library already in `gradle/libs.versions.toml`, then a well-known library added with a reason in the PR. Hand-written code for a generic problem needs a comment saying why nothing existing fits.
- Respect accepted ADRs in `docs/adr`. Only write a new ADR when the decision meets the criteria in `docs/adr/README.md`, using `docs/adr/template.md` exactly, and update the index.

## Workflow

1. Branch from `main` (`feat/...`, `fix/...`, `docs/...`).
2. In `shared`, write the failing test first, then the code.
3. In `app`, give every component and screen a `@PreviewLightDark` preview, following [docs/design.md](docs/design.md).
4. Run `./gradlew spotlessApply check` and fix everything it reports.
5. Open a PR titled in the commit format, saying what changed, why and how it was tested, with `Closes #N` when it comes from an issue.

Claude Code hooks in `.claude/settings.json` enforce part of this; don't disable them.

## Tooling

Prefer `rtk` over raw shell commands and `codegraph` over grep/find/reading files to explore code, whenever applicable and available.
