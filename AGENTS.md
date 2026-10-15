# AGENTS.md

Guidance for AI agents in this repo.

## Project

Doomlings Atlas: free, open-source, unofficial companion app for the Doomlings card game (scoring, card compendium, card text translation, tutorial). No code yet.

## Doomlings Assets

Permission granted by Doomlings LLC (email, 2026-10-15) under the fan art terms of their ToS (§1.6). Keep every commitment:

- Doomlings card images, card text, rulebook content and the Doomlings name are © Doomlings LLC, used with permission, and are **not** covered by the GPL-3.0. Keep them apart from the code (e.g. `assets/doomlings/` with its own notice).
- Never use the Doomlings logo.
- Always show the notice "Unofficial, not affiliated with or endorsed by Doomlings LLC".
- No paid features, no ads. Donations not confirmed yet.
- Translations: Portuguese and Spanish (reviewed by the maintainer); other languages only through community contributions. Adopt official terminology if an official translation is released.

## Tooling

Prefer `rtk` over raw shell commands and `codegraph` over grep/find/reading files to explore code, whenever applicable and available. Read files before editing them.

## Git / Commits

- Format: `[type] subject` — types `feat`/`fix`/`perf`/`refactor`/`chore`/`docs`/`update`/`media`, no scope prefix.
- Lowercase English subject, no action verbs (the type is the action), ~72 chars max.
- Body only when the _why_ is complex. No `Co-Authored-By`.
