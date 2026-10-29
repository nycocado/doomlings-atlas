# Glossary

The words this project uses for its concepts, in code, docs, issues and chat. Game terms follow [docs/rules.md](docs/rules.md), which follows the official rulesheet; when they disagree, the rulesheet wins. How each word is translated is in [docs/glossary](docs/glossary), one file per language.

## The Game

- **Trait**: the cards players play into their trait pile. Each has a face value and a color, or none.
- **Color**: blue, green, purple or red. A trait with two colors counts as both. _Avoid_: suit.
- **Colorless**: a trait with no color (gray border). It doesn't count for color effects; in the compendium filter it is a choice of its own.
- **Dominant**: a trait with a gold border and star. Once played it can't leave its pile, and each player plays only 2 ([one exception](docs/rules.md#dominant-traits)).
- **Face value**: the number printed at the bottom of a trait. Bonuses are counted apart from it. _Avoid_: points (for the printed number), score.
- **Compass star**: a trait whose value "is equal to" something in the game; it has no fixed face value (`points: null` in the data).
- **Effect**: the text at the bottom of a card. A trait without one is **effectless**.
- **Action**, **Play When**, **World's End**, **Drop of Life**: the icons that say when a trait's effect applies ([docs/rules.md](docs/rules.md#icons)).
- **Age**: the card flipped at the start of each round; its effect lasts that round.
- **Catastrophe**: a card of the age deck that ends an era and changes every Gene Pool; the third one ends the game. Its **World's End** effect counts only then.
- **The Birth of Life**: the card that starts the game, on top of the age deck.
- **Era**: the rounds up to and including a catastrophe. A game has 3.
- **Gene Pool**: each player's card setting their hand size, between 1 and 8.
- **Trait pile**: the traits a player has played, face up in front of them. _Avoid_: board, tableau.
- **Hand**: the traits a player holds.
- **Stabilize**: drawing or discarding at the end of a turn until the hand matches the Gene Pool.
- **Score**: a player's final total: face values plus bonuses and World's End effects ([docs/rules.md](docs/rules.md#scoring)).

## Card Data

- **Set**: the product a card comes from (Classic, Overlush…), the `set` field and folder in `assets/doomlings`. _Avoid_: edition, for this.
- **Edition**: only the printing of the base game. The app targets Classic (2nd edition).
- **Card type**: the `type` field of a card in `cards.json` (`trait`, `age`, `catastrophe`, `birth-of-life`, `gene-pool` and others); one Kotlin class each in `Card.kt`.
- **Enabled sets** and **enabled types**: the sets and types packaged into the app (`app/build.gradle.kts`). The rest stays in the repo until its data is checked or a feature needs it.
- **Card kind**: a segment of the compendium (Traits, Ages, Catastrophes), `CardKind` in code. Not every card type has one.
- **Card library**: the decoded cards of the enabled sets, `CardLibrary` in `shared`. A lookup by id finds the card, misses it (the data no longer has it) or is still loading.
- **Printed text**: a card's name and text as printed on it, in English.
- **Translation**: a card's name and text in another language, written by the project and unofficial, in one regional variant per language, declared in its glossary (pt-BR, es-419). A card may have none in a given language. _Avoid_: localization, for card text.

## The App

- **Compendium**: the screen listing the cards, with search and filters.
- **Card language**: the language card text is shown and searched in: the printed text or one translation. Chosen apart from the interface language, and the device's language until the player picks one ([ADR 0008](docs/adr/0008-card-language-defaults-to-the-device.md); [ADR 0007](docs/adr/0007-card-language-apart-from-interface.md)).
- **Language selector**: the control in the card detail that switches the card language among the languages the card has.
- **Card detail**: the screen of one card. On a wide window it opens **beside the list**, in the **card pane**.
- **Card group**: the cards of a kind that share a name, from any set; the compendium shows it as a **stack** that opens in place into a band. A name no other card shares stays a single card. Grouping comes after the filter, so a group holds only what the filter leaves.
- **Variant**: a card of a card group. Variants of a name differ in art and flavor, not in the game: a Kidney counts as a Kidney from any set.
- **Header**: the dark top of the compendium, with the title, sunset band and search. It **collapses** and **expands** as the list scrolls. _Avoid_: fold, for this.
- **Fold**: only the hinge of a foldable device.
- **Dock**: the app's floating bar; in the rail layout it becomes the **rail**. _Avoid_: navigation bar, tab bar.
- **Rail layout**: the compendium when it is 600dp wide or more, by its own width, not the window's: the header in one row and the dock as the rail beside the list.
- **Stacked layout**: the compendium when narrower: the header stacked, title above the search, and the dock as a bar below the list. Only this header collapses. _Avoid_: phone layout, for this.
- **Navigation bar**: Android's system bar at the bottom of the screen, never the app's.
- **List-detail layout**: what the window and the open card decide together: list and card side by side or not, whether a card sits beside the list, and what follows (About on the list, the compendium centered, who sets the status bar icons, the scene's panes and animation). Decided once; the screens read it.
- **Wide window**: 840dp wide or more (tablets, unfolded foldables, phones on their side), where the list and a card can share the screen.
- **Sticker**: the look of every control and card: thick outline, hard shadow, rounded corners ([docs/design.md](docs/design.md)).
- **Sunset band**: the four color blocks under a header or the dock.
- **Tilt**: the small rotation of a card on the table.
- **Points badge**: the round yellow sticker with a trait's face value.
- **Unofficial notice**: "Unofficial, not affiliated with or endorsed by Doomlings LLC", on first launch and on About. Required by Doomlings' permission.
