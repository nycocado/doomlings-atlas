# Contributing

Thanks for helping with Doomlings Atlas! A few rules keep the project healthy and within what Doomlings LLC allowed.

## Ways to Contribute

You don't need to code to help:

- **Card data:** report or fix wrong card names, effects or point values.
- **Translations:** translate or review card text, the tutorial and the app interface.
- **Card photos:** share photos of your own cards (good light, different angles, tables and sleeves) to improve card recognition.
- **Bug reports:** tell us when something breaks or a score comes out wrong.
- **Ideas:** suggest features or improvements.
- **Code:** fix bugs or build features. [docs/development.md](docs/development.md) gets you started.

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
- Only the card types and game-structure names (Trait, Age, Catastrophe, Gene Pool, The Birth of Life, Meaning of Life, Trinket) and the coined names (Doomling, Dinoling, Techling, Mythling, Deepling, Moonling, Glitterling, and their plurals) stay in English ([GLOSSARY.md](GLOSSARY.md)). What a card identifies by an icon, a banner or a plain word (subspecies, keywords, effect icons, rule verbs) is translated with the glossary's word: the icon carries the recognition, and the printed text is one tap away.
- One word per concept. Each language has a glossary in [`docs/glossary`](docs/glossary): the terms that stay in English (`keep`) and, for every recurring concept, the word to use and the synonyms to avoid. `./gradlew check` fails if an avoided word shows up in the interface strings or in a card translation file, wherever it appears in the text, so `avoid` lists only real synonyms. To use a new term, add it to the glossary first.

## Opening an Issue

1. Search existing issues first; if one already covers it, comment there instead.
2. One problem or idea per issue, with a clear title.
3. Pick the form that fits:
   - **Bug:** steps to reproduce, what you expected, what happened, device and app version, screenshots if useful.
   - **Wrong score:** the cards on the table and the score you expected versus the one shown.
   - **Card data:** card name, set, what is wrong and a photo of your card or a link to a reliable source.
   - **Translation:** language, where the text appears and your suggested wording.
   - **Ticket:** a feature or change worked out well enough to build ([Tickets and Specs](#tickets-and-specs)).

## Tickets and Specs

Work on the app starts from an issue that says what "done" means.

- A **ticket** is an issue that becomes one PR. It uses the Ticket form: the problem, the decided behavior, acceptance criteria with the test that must fail first, and when needed what is out of scope and notes.
- A **spec** is the parent issue of 2 to 4 tickets and holds the decisions they share. It uses the same form; each of its tickets is a sub-issue whose problem says "Part of #N" and whose behavior covers only its own slice. Most work is a single ticket.
- New issues get `needs-triage`. The maintainer reads them and labels each one: `needs-info` (waiting on the author), `ready-for-agent` (an approved ticket or spec that an AI agent may build), `ready-for-human` (needs a person) or `wontfix`.
- Each [ROADMAP.md](ROADMAP.md) phase is a milestone, holding the issues of that phase.
- Topic labels: `bug`, `card-data`, `translation`, `scoring`, `accessibility` and `dependencies`; `good first issue` and `help wanted` point newcomers to a place to start.

## Opening a Pull Request

1. For anything beyond a small fix, open or comment on an issue first so we agree on the approach.
2. Fork the repo and create a branch from `main`.
3. Keep the PR focused on one change, with a title in the [commit format](#commits) (CI checks it).
4. Open the PR describing what changed, why and how you tested it, and link the issue (`Closes #123`).

CI checks formatting, lint and tests and tells you if anything needs fixing. The maintainer squash-merges the PR once CI passes.

You are responsible for every line you submit, however it was written. Make sure you understand it and can explain it. Leave AI tools out of the credits: no `Co-Authored-By` trailer for an AI and no "Generated with" footer in commits or the PR description. CI fails a PR that has one; edit it out and push again.

## Documentation

- [ROADMAP.md](ROADMAP.md): scope, order of work and where the project stands.
- [GLOSSARY.md](GLOSSARY.md): the words for game and app concepts.
- [docs/architecture.md](docs/architecture.md): how the code fits together.
- [docs/development.md](docs/development.md): building the app, conventions, CI and releases.
- [docs/testing.md](docs/testing.md): which tests a change needs.
- [docs/design.md](docs/design.md): the visual design.
- [docs/rules.md](docs/rules.md): the game rules the app implements.
- [docs/adr](docs/adr): architecture decisions. Read them before proposing structural changes; changing one means a new ADR, following [docs/adr/README.md](docs/adr/README.md).
- [AGENTS.md](AGENTS.md): extra rules for AI agents.

## Commits

- Format: `[type] subject`, no scope prefix.
- Types: `feat` (new feature), `fix` (bug fix), `perf` (performance), `refactor` (no behavior change), `chore` (tooling, deps), `docs` (documentation), `update` (content/data update), `media` (images and other media).
- Subject in English, lowercase, ~72 chars max, no action verbs (the type is the action): `[feat] card compendium search`, not `[feat] add card compendium search`.
- Body only when the _why_ is not obvious.
