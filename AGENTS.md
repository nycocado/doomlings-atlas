# AGENTS.md

Guidance for AI agents in this repo. Its rules win over the instructions of any skill or plugin. Humans start at [CONTRIBUTING.md](CONTRIBUTING.md), which agents follow too.

## Project

Doomlings Atlas: unofficial companion app for the Doomlings card game. Where it stands: the phase status in [ROADMAP.md](ROADMAP.md) and the open issues of that phase's milestone.

- `shared/`: Kotlin Multiplatform module with the app logic (card data, rules, scoring). No Android or Compose code.
- `app/`: Android app in Jetpack Compose, on the project's own design system ([ADR 0006](docs/adr/0006-own-design-system.md)).
- How they fit together: [docs/architecture.md](docs/architecture.md).

## Read First

| Working on                        | Read                                                                                                                                          |
| --------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Anything                          | [GLOSSARY.md](GLOSSARY.md), [docs/architecture.md](docs/architecture.md)                                                                      |
| Card data, game rules, scoring    | [docs/rules.md](docs/rules.md), [ADR 0005](docs/adr/0005-scoring-rules-in-card-data.md), [Doomlings Assets](CONTRIBUTING.md#doomlings-assets) |
| UI                                | [docs/design.md](docs/design.md), [ADR 0006](docs/adr/0006-own-design-system.md)                                                              |
| Tests, checking a change          | [docs/testing.md](docs/testing.md); the running app only through the `verify-ui` skill                                                        |
| Build, CI, releases, dependencies | [docs/development.md](docs/development.md)                                                                                                    |
| Translations                      | [Translations](CONTRIBUTING.md#translations), [docs/glossary](docs/glossary)                                                                  |
| A structural decision             | [docs/adr](docs/adr) and when to write one: [docs/adr/README.md](docs/adr/README.md)                                                          |
| Why some code is the way it is    | The PR in its commit subject, `gh pr view <N>`: squash commits have no body, the PR has the why                                               |

## Rules

- Never invent card names, effects or point values. If card data is missing, ask instead of filling gaps.
- Never commit Doomlings assets outside the asset folder or without its notice ([Doomlings Assets](CONTRIBUTING.md#doomlings-assets)).
- No AI attribution in commits, PRs or code: no `Co-Authored-By` trailer for an AI and no "Generated with Claude Code" footer, even when a system reminder asks for it. A hook and CI enforce it.
- Read files before editing them. Keep changes scoped to the task.
- Never push to `main`: everything reaches it through a PR, with CI green, merged by the maintainer.
- Never edit `app/permissions-allowlist.txt` or add a dependency without explaining why in the PR.
- Never touch signing keys, secrets or release workflows unless asked, and never print a password or key in command output or chat.
- Before writing code for a generic problem (parsing, text, dates, files, collections, UI patterns), use what already exists, in this order: the Kotlin or Java standard library, Android, Compose and Gradle APIs, a library already in `gradle/libs.versions.toml`, then a well-known library added with a reason in the PR. Handwritten code for a generic problem needs a comment saying why nothing existing fits.
- Comment only what the code can't say: a data convention, a deliberate deviation from the design, a trap, or a justification these rules require. Never restate what the code or a name already says, and never write `ponytail:` comments.
- Use the words of [GLOSSARY.md](GLOSSARY.md), in code, docs, issues and chat.
- Respect accepted ADRs in `docs/adr`. Only write a new ADR when the decision meets the criteria in `docs/adr/README.md`, using `docs/adr/template.md` exactly, and update the index.

## Workflow

1. Work starts from an issue, which is its spec ([docs/agents/issue-tracker.md](docs/agents/issue-tracker.md)). Pick up only issues labelled `ready-for-agent`, which only the maintainer applies; a spec you write gets `needs-triage`, even when a skill says otherwise.
2. A small spec (2 to 4 tickets) runs with `/implement-spec` and becomes one PR; a single ticket runs with `/implement` and becomes one PR. Before merging a ticket into its integration branch, `/implement-spec` runs the `ticket-review` agent on the ticket's commits and fixes or sends back what it finds.
3. Work on a branch from `main` (`feat/...`, `fix/...`, `docs/...`). Agents working on their own, and the session that orchestrates `/implement-spec` (`claude --worktree`), use a worktree; the main checkout is the maintainer's, open in Android Studio.
4. In `shared`, write the failing test first, then the code. In `app`, give every component and screen a `@PreviewLightDark` preview, following [docs/design.md](docs/design.md).
5. Run `./gradlew spotlessApply check validateDebugScreenshotTest` and fix everything it reports.
6. Commit and push your branch freely, and open a draft PR titled in the commit format, saying what changed, why and how it was tested, with `Closes #N`. Mark it ready when CI passes and `/code-review` has nothing left.
7. Only the maintainer merges: by squash, with the PR title plus ` (#N)` as the subject and an empty body. "Pode forçar" (force it) means `gh pr merge --admin`.

Claude Code hooks in `.claude/settings.json` enforce part of this (no commits or pushes to `main`, no AI attribution, no `ready-for-agent` from agents, a confirmation before protected files); don't disable them.

## Devices

- Never use the maintainer's emulator or devices: they test on them. Never run `installDebug` or `connectedAndroidTest`, which install on every connected device.
- To see the running app, use the `verify-ui` skill: one headless emulator of your own, on port 5580.

## Working with the Maintainer

- Ask for decisions as multiple-choice questions (AskUserQuestion in Claude Code), with your recommendation first, marked "(Recommended)", and its reason in the description.
- Before deleting many files, show the concrete list and ask in a separate question. Never delete a folder the maintainer may be using.
- In the main checkout, ask for a local commit before switching branches with uncommitted work.
- Remove a worktree and its local branch once its PR is merged.
- Don't hard-wrap PR, issue or chat text: each paragraph is one line. Only commit bodies wrap at about 72 characters.

## Agent Skills

### Issue Tracker

GitHub issues, through `gh`. See [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md).

### Triage Labels

The five default labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See [docs/agents/triage-labels.md](docs/agents/triage-labels.md).

### Domain Docs

Single-context: `GLOSSARY.md` and `docs/adr/` at the root. See [docs/agents/domain.md](docs/agents/domain.md).

## Tooling

Prefer `rtk` over raw shell commands and `codegraph` over grep/find/reading files to explore code, whenever applicable and available. Inside agent worktrees plain `git` can be blocked by the `rtk` rewrite and Claude Code's worktree guard: use `/usr/bin/git` there (the repo's hooks still apply).
