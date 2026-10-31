# 10. Ink Marks in Effect Text

- Status: accepted
- Date: 2026-10-31

## Context

Some printed effects color a span of their text: "red" in red, "rainbow" in rainbow. ADR 0009 split a card's text into effects and kept inline icons as tokens in braces, but rejected markup inside the text, so the data has no way to say which words are printed in color, and nothing downstream can show it.

## Decision

- An effect's text marks an inked span in brackets: `[red]red[/red]`. The inks are `red`, `blue`, `green`, `purple`, `colorless` and `rainbow`.
- `shared` reads the marks with the tokens, in one reading of the text: the card face's text parts are words, with an ink when marked, and tokens. Search and citations see the words without the marks, as they do without tokens.
- A span is closed by its own ink, holds no other span and doesn't cross a token. A translated effect has the inks of its English effect, in kind and order. The data tests check both, in `cards.json` and in every translation.
- This revises ADR 0009's rejected "markup inside the text" alternative for inks only: sections stay a list of effects.

## Alternatives Considered

- **A list of spans per effect in the data (`[{text, ink}]`):** no parsing. Rejected: every effect and every translation would become a list for a few colored words, and translators would split sentences by hand.
- **Derive the ink from the words (color every "red"):** no data change. Rejected: not every color word is printed in color, and translations name colors differently.
- **Character ranges beside the text:** the text stays plain. Rejected: ranges break on every edit and differ per translation.

## Consequences

- The app can draw an ink once it renders `TextPart.Words.ink`; until then inked words show as plain words.
- Card data and translations carry brackets that translators must keep around the matching words; the data tests catch a mark lost, added, reordered or malformed.
- One more markup to parse, but in the same place and pass as the tokens, so the card face and search keep a single reading of the text.
