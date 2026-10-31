# 11. Yellow Ink and an Effect's Ink

- Status: accepted
- Date: 2026-10-31

## Context

[ADR 0010](0010-ink-marks-in-effect-text.md) gives a span of an effect's text an ink from marks in the data, among red, blue, green, purple, colorless and rainbow. Cards also print text in yellow: every Catastrophe its whole World's End and every Trinket its whole Pocket Objective, heading included, with no mark in the data; and Stygian two spans of its text. Two parallel changes modelled these apart, a yellow flag per block beside the inks per span, each with its own way to color the text in the app.

## Decision

- Yellow is an ink, like the six of ADR 0010, and a mark can carry it (`[yellow]Choose a color.[/yellow]`).
- An effect has an ink of its own, decided in `shared` from its kind and its card type: yellow for a Catastrophe's World's End and a Trinket's Pocket Objective, none otherwise (a trait's World's End is in the normal text color).
- `shared` gives the effect's ink to every unmarked word of the effect; a span's own ink wins over it. The heading takes the effect's ink.
- The app turns an ink into its colors in one place, for the heading, the text and the spans.

## Alternatives Considered

- **A yellow flag per block, apart from inks:** what the first change did. Rejected: two models and two coloring paths for what the card prints the same way, text in a color.
- **The effect's ink decided in the app:** `shared` stays unaware of it. Rejected: a rule about cards, tested apart from the card face, and a second place where text gets its color.
- **Marking every Catastrophe's World's End in the data:** no new rule. Rejected: a mark per Catastrophe and translation for something the kind already says, and the heading isn't in the text.

## Consequences

- Every color of card text, marked or from the kind, goes through the same ink and the same drawing, and the contrast test covers every ink.
- `shared` holds one card rule about color (which kinds are yellow), next to the card face it shapes.
- The data tests accept `[yellow]`, so a translator must keep it like any other mark.
