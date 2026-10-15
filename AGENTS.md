# AGENTS.md

Guidance for AI agents in this repo. Follow [CONTRIBUTING.md](CONTRIBUTING.md) first; this file only adds agent-specific rules.

## Project

Doomlings Atlas: unofficial companion app for the Doomlings card game. No code yet.

## Rules

- Never invent card names, effects or point values. If card data is missing, ask instead of filling gaps.
- Never commit Doomlings assets outside the asset folder or without its notice (see CONTRIBUTING.md).
- No `Co-Authored-By` trailer or any other AI attribution in commits, PRs or code.
- Read files before editing them. Keep changes scoped to the task.
- Respect accepted ADRs in `docs/adr`. Only write a new ADR when the decision meets the criteria in `docs/adr/README.md`, using `docs/adr/template.md` exactly, and update the index.

## Tooling

Prefer `rtk` over raw shell commands and `codegraph` over grep/find/reading files to explore code, whenever applicable and available.
