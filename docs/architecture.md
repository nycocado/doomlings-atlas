# Architecture

A map of the code at the level of modules and data flow. The decisions behind it are in [docs/adr](adr); names of things are in [GLOSSARY.md](../GLOSSARY.md).

## Modules

- **`shared/`** (Kotlin Multiplatform, Android its only target for now, [ADR 0001](adr/0001-kotlin-multiplatform-android-first.md)): the card model (`card/Card.kt`), decoding (`card/CardDecoding.kt`) and the compendium filter (`compendium/CompendiumFilter.kt`). Game rules and scoring go here too. No Android or Compose code; platform bits are `expect`/`actual` (accent stripping).
- **`app/`** (Android, Jetpack Compose, Hilt): screens, navigation, the theme and the build tasks that package card data.
- **`assets/doomlings/`**: card data and images, © Doomlings LLC ([NOTICE](../assets/doomlings/NOTICE.md)). Outside both modules on purpose.
- **`design/`**: design tokens, brand assets and prototype screens ([docs/design.md](design.md)).

## Card Data, from File to Screen

1. `assets/doomlings/cards.json` holds every card of every set, as printed.
2. At build time, `PackageCards` (`app/build.gradle.kts`) copies only the **enabled sets** and **enabled types** (`enabledSets`, `enabledTypes`), with their images, into the APK's `assets/cards/`. Today: the Classic set; traits, ages and catastrophes.
3. `CardRepository` reads `assets/cards/cards.json` and `decodeCards` turns it into `Card` objects. Decoding is strict: a missing field, unknown key or unknown value fails instead of being skipped. Images load from `file:///android_asset/cards/` with Coil.
   Translations are one file per language code beside `cards.json` (`assets/doomlings/pt.json`, `es.json`), keyed by card id, holding the card's `name` and the text fields it has (`effect`, `flavor`, `worldsEnd`). `PackageCards` keeps only the entries of the packaged cards, `CardRepository.translations()` reads them into `CardTranslations`, which gives a card's languages and text with the printed text as fallback ([ADR 0007](adr/0007-card-language-apart-from-interface.md)).
4. `CompendiumViewModel` holds the cards and the `CompendiumFilter`, saved in `SavedStateHandle` so it survives process death. The filtered, sorted list comes from `filterCompendium` in `shared`.
5. `AtlasApp` assembles the screens with Navigation 3 (`NavDisplay`) in a list-detail scene: the compendium, the card detail (beside the list on a wide window) and About. The **card pane** (`cardpane/CardPane.kt`) owns which card is open (read from the back stack), opening, swapping and closing cards, finding the card and going back when it is missing, and the sheet beside the list; the card detail only receives the card. The **list-detail layout** (`cardpane/ListDetailLayout.kt`) is decided once from the window and the open card, and the screens read it.

## Screens and UI

- Screens live in feature packages (`compendium/`, `carddetail/`, `about/`), with the card pane and the list-detail layout in `cardpane/`; shared components in `ui/component/`; the theme (`AtlasTheme`, colors, type, dimensions, stickers) in `ui/theme/`, following `design/tokens.json`.
- Components are the project's own, on Compose Foundation; Material 3 only for sheets, dialogs, snackbars and ripple ([ADR 0006](adr/0006-own-design-system.md)).
- The unofficial notice shows once, on first launch; whether it was seen is a `SharedPreferences` flag.

## Invariants

- The app works offline and declares no permissions ([ADR 0002](adr/0002-offline-without-backend.md)).
- Card data is never guessed: everything in `cards.json` comes from a source ([CONTRIBUTING.md](../CONTRIBUTING.md#doomlings-assets)).
- Scoring rules will live in card data, as calls to named Kotlin functions ([ADR 0005](adr/0005-scoring-rules-in-card-data.md)).
- Card text language is chosen apart from the interface language ([ADR 0007](adr/0007-card-language-apart-from-interface.md)).

## Build Checks

Custom tasks in `app/build.gradle.kts` ([docs/development.md](development.md)):

- `PackageCards`, in every build: fails if an enabled set has no cards.
- `CheckPermissions`, in `./gradlew check`: fails if the merged manifest asks for a permission missing from `app/permissions-allowlist.txt`.
- `CheckGlossary`, in `./gradlew check`: fails if a translation uses a word its glossary in `docs/glossary` rules out.
