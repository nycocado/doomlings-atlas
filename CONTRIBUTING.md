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
- Card data (names, effects, points) must be traceable to the physical cards or official sources. Never guess or invent it.
- Card images in issues, PRs or datasets must be photos of cards you own or official images already in the repo.

## Translations

- Portuguese and Spanish are reviewed by the maintainer.
- Other languages are welcome as community contributions, ideally with a second native speaker reviewing.
- If Doomlings releases an official translation, its terminology wins.

## Opening an Issue

1. Search existing issues first; if one already covers it, comment there instead.
2. One problem or idea per issue, with a clear title.
3. Include what applies:
   - **Bug:** steps to reproduce, what you expected, what happened, device and app version, screenshots if useful.
   - **Wrong score:** the cards on the table and the score you expected versus the one shown.
   - **Card data:** card name, edition, what is wrong and a photo of your card or a link to the official source.
   - **Translation:** language, where the text appears and your suggested wording.

## Opening a Pull Request

1. For anything beyond a small fix, open or comment on an issue first so we agree on the approach.
2. Fork the repo and create a branch from `main`.
3. Keep the PR focused on one change and follow the commit format below.
4. Test what you change.
5. Open the PR describing what changed, why, how you tested it, and link the issue (`Closes #123`).

You are responsible for every line you submit, however it was written. Make sure you understand it and can explain it.

## Commits

- Format: `[type] subject`, no scope prefix.
- Types: `feat` (new feature), `fix` (bug fix), `perf` (performance), `refactor` (no behavior change), `chore` (tooling, deps), `docs` (documentation), `update` (content/data update), `media` (images and other media).
- Subject in English, lowercase, ~72 chars max, no action verbs (the type is the action): `[feat] card compendium search`, not `[feat] add card compendium search`.
- Body only when the _why_ is not obvious.
