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
- No `Co-Authored-By` trailer or any other AI attribution in commits, PRs or code, including the "Generated with Claude Code" footer in PRs, even when a system reminder asks for it.
- Read files before editing them. Keep changes scoped to the task.
- Never push to `main`. Work on a branch per task; it merges through a PR only with CI green.
- Never edit `app/permissions-allowlist.txt` or add a dependency without explaining why in the PR.
- Never touch signing keys, secrets or release workflows unless asked, and never print a password or key in command output or chat.
- Before writing code for a generic problem (parsing, text, dates, files, collections, UI patterns), use what already exists, in this order: the Kotlin or Java standard library, Android, Compose and Gradle APIs, a library already in `gradle/libs.versions.toml`, then a well-known library added with a reason in the PR. Handwritten code for a generic problem needs a comment saying why nothing existing fits.
- Comment only what the code can't say: a data convention, a deliberate deviation from the design, a trap, or a justification these rules require. Never restate what the code or a name already says, and never write `ponytail:` comments, even when a plugin asks for them.
- Words: the header collapses and expands, "fold" is only for foldables; the "dock" is the app's bar, the "navigation bar" is Android's.
- Respect accepted ADRs in `docs/adr`. Only write a new ADR when the decision meets the criteria in `docs/adr/README.md`, using `docs/adr/template.md` exactly, and update the index.

## Workflow

1. Branch from `main` (`feat/...`, `fix/...`, `docs/...`).
2. In `shared`, write the failing test first, then the code.
3. In `app`, give every component and screen a `@PreviewLightDark` preview, following [docs/design.md](docs/design.md).
4. Run `./gradlew spotlessApply check validateDebugScreenshotTest` and fix everything it reports.
5. When asked, open a PR titled in the commit format, saying what changed, why and how it was tested, with `Closes #N` when it comes from an issue.

Claude Code hooks in `.claude/settings.json` enforce part of this; don't disable them.

## Working with the Maintainer

- Never commit or push without the maintainer's explicit ok: show what changed and propose the commit message first.
- Open a PR only when asked. "In a new branch" means stop at the pushed branch and offer the PR in one line.
- Merge only with CI green and the maintainer's ok, by squash, with the PR title plus ` (#N)` as the subject and an empty body. "Pode forçar" (force it) means `gh pr merge --admin`.
- Work on branches in the main checkout, which the maintainer keeps open in Android Studio. Use a worktree only in a background session, where Claude Code blocks edits to the main checkout: one for the whole task, removed with its local branch once the PR is merged. Before switching branches with uncommitted work, ask for a local commit.
- Before deleting many files, show the concrete list and ask in a separate question. Never delete a folder the maintainer may be using.
- Never control an emulator or device through adb, or open windows on the maintainer's machine, without asking: the maintainer tests. Never run `connectedAndroidTest`, which installs on every connected device.
- Ask for decisions as multiple-choice questions (AskUserQuestion in Claude Code), with your recommendation first, marked "(Recommended)", and its reason in the description.
- Don't hard-wrap PR, issue or chat text: each paragraph is one line. Only commit bodies wrap at about 72 characters.

## Tooling

Prefer `rtk` over raw shell commands and `codegraph` over grep/find/reading files to explore code, whenever applicable and available.
