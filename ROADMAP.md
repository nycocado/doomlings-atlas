# Roadmap

Architecture decisions behind this plan are in [docs/adr](docs/adr).

## 0. Foundations (Done)

Everything here comes before assets and code.

- Visual identity: app logo and icon, without the Doomlings logo.
- Design system.
- Set to start with: Classic (2nd edition) base game, no expansions.
- Card sources: images from [Ryeera's Tabletop Simulator mod](https://steamcommunity.com/sharedfiles/filedetails/?id=3151925153), card data checked against the [official Doomlings Compendium](https://www.doomlings.com/pages/compendium).

## 1. Card Data and Compendium (Done, v0.1.0)

- Embedded card database with what is printed on each card: name, set, type, color, points, full text and image.
- Every card verified against a reliable source (physical cards, official material or checked community assets such as Tabletop Simulator mods).
- Compendium: card list with images, search by name, filters by printed attributes, card detail.
- The notice "Unofficial, not affiliated with or endorsed by Doomlings LLC" shown on first launch and always available on an "About" screen.
- Interface in English and Portuguese, following the system language (no in-app selector); card text in English.
- Tests for database integrity: unique identifiers, required fields, every card with an image.
- Screenshot tests from the component previews, once the design system settles.

## 2. Card Text Translation (In Progress)

- Portuguese and Spanish for the cards of every set, drafted by an agent and reviewed by the maintainer; other languages through community contributions.
- Translated card text shown in the compendium, chosen apart from the interface language: card text starts in the language Android gives the app when it is Portuguese or Spanish, and as printed otherwise ([ADR 0008](docs/adr/0008-card-language-defaults-to-the-device.md)), with a language selector in the card detail.
- App interface also in Spanish.
- Card groups: cards that share a name (the Kidneys, the Swarms) stack into one thumb in the compendium and open in place to show their variants.
- Links between cited cards: a card name cited in a card's effect or World's End, printed or translated, opens that card in the card detail, and back returns to the card that cited it.
- Unit and data tests: card counts against the rulesheet, text hygiene in every set's data, signed points and cards that share a name, and search edge cases.
- UI tests with Compose and Robolectric, in CI: navigation on phone and tablet sizes (opening, switching and closing cards, About) and state surviving activity recreation, with Compose's accessibility checks enabled so a touch target, contrast or label problem fails the build.
- A screenshot test of the compendium with its header half collapsed, since the collapse relies on how `animateBounds` sizes an idle node.
- Update check: the app looks up the latest GitHub Release on each start and, when a newer version exists, offers to download it, check its digest and hand it to the system installer; skipped in debug builds and when Google Play installed it. It needs the internet and install permissions, so an ADR amends [ADR 0002](docs/adr/0002-offline-without-backend.md) first.

### Going Public

The repository goes public and the first APK comes out on GitHub Releases once every other issue of this phase is done. Before that, in this order:

- A `SECURITY.md` and a `CODE_OF_CONDUCT.md` (Contributor Covenant) with a reporting contact.
- CI also builds the unsigned release APK, so an R8 break fails the pull request, and reviews the dependencies each pull request adds. The workflows are checked once with [zizmor](https://zizmor.sh).
- Release workflow: a `vX.Y.Z` tag builds the signed APK in the `release` environment and creates a draft GitHub Release with it (see [docs/development.md](docs/development.md#releases)). The maintainer installs the draft's APK on a real device before publishing it.
- README for players: what the app does, screenshots and where to download it. Repository topics so the project shows up in GitHub search.
- Git history rewritten so every commit of the maintainer uses their GitHub noreply address, with the `v0.1.0` tag and Release recreated on the new commits, and the history scanned for secrets with gitleaks. Last, with no pull request open, since the force push invalidates every branch.

Then, right after the repository goes public, since the free plan offers these only to public repositories:

- Rulesets on `main` (pull request, green CI, no force push) and on `v*` tags (only the maintainer creates or moves them), immutable releases, the maintainer's approval of each run in the `release` environment, CodeQL, secret scanning, push protection and private vulnerability reporting.
- Actions runs for outside contributors wait for the maintainer's approval, and the maintainer's GitHub account uses two-factor authentication.
- The first APK published, and Doomlings told that the repository is public.

## 3. Every Set (Planned)

- Every set and every card type in the compendium, after the per-set card data and its integrity checks.
- Card kinds in a scrollable row: Traits, Ages (with The Birth of Life), Catastrophes, Gene Pools, Meanings of Life, Treasures, Trinkets and Merchants. A kind with no card in the filtered sets doesn't show.
- A set filter in a sheet, for every card kind, with the sets in release order (the year printed on the cards) and Special last.
- The card detail names the card's set and shows both colors of a dual-color trait; every trait icon has its symbol.
- The expansions' official rulesheets in the asset folder, as the source for their card types in the glossary.
- Every card image in WebP.
- No declaration of the sets a player owns: the set filter covers it.

## 4. Manual Scoring (Planned)

- Players added by name at the start of the game, each with the Gene Pool card in front of them.
- Players enter the full final state: each player's trait pile and hand, catastrophes, World's End.
- Score calculated automatically, with a final ranking and a breakdown of where each point came from.
- Manual adjustment per player for cards missing from the database and house rules.
- Card picker reuses the compendium's searchable list.
- An in-progress game survives the app being killed; no game history.
- Tests for every card with a special effect, plus real games with hand-checked scores.

## 5. Card Scanner (Planned)

- Card recognition from the camera, running on the device.
- Compendium search by image: photograph a card to open it.
- In a photo of the whole table, each player's Gene Pool card identifies which trait pile belongs to whom.

## 6. Game Tutorial (Planned)

- Interactive tutorial in the app languages.
- Links to the official rulebook and videos.

## Not Planned Yet

Google Play release, F-Droid, game history, collection tracking, favorite cards, iOS app, donations, compendium filters beyond color and type (points, icons, subspecies), swiping between cards in the card detail, an error screen for when the card data fails to load.
