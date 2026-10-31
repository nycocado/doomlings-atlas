# 9. Card Text as a List of Effects

- Status: accepted; markup inside the text amended by [0010](0010-ink-marks-in-effect-text.md)
- Date: 2026-10-30

## Context

A card's text was one `effect` string in `cards.json`, plus `worldsEnd` on Catastrophes. The printed text is a list of sections: a divider or an icon separates them, an icon below a section says when it applies, small-type notes stand apart, and Trinkets, Treasures and Merchants head their sections with labels. The "+N Gene Pool" line was text in every language, with a period on some cards and not on others. The inline icons were lost. The card detail could only print the text as one block, and every translation repeated the Gene Pool line and the labels.

## Decision

- A card's text is a list of **effects** in printed order, `effects` in `cards.json`. Each is `{kind?, text}`; an effect with no kind is a plain effect.
- The kind is an enum, decoded strictly: an unknown kind fails. `action`, `play-when`, `worlds-end` and `note` fit any card with text. The labels `awesome-power`, `pocket-objective` (Trinket), `rush`, `mutate`, `echo`, `cloak`, `adapt` (Treasure), `exchange`, `buy`, `collect` and `bargain` (Merchant) fit only their card type. A label and "At World's End:" leave the text and become the kind.
- A Catastrophe's World's End is a `worlds-end` effect, and `worldsEnd` is gone.
- The "+N Gene Pool" line of a trait or Catastrophe is `genePool`, and a Trinket's Value is `value`: numbers the app writes in the card language.
- Inline icons are tokens in braces in the text (`{action}`, `{compass}`). Search matches the text without them.
- The translation files keep their flat `effect` text until a later change makes a translation a list of texts in the order of the effects.

## Alternatives Considered

- **Keep the flat string and parse it in the app:** no data change. Rejected: the sections can't be told apart from the text, and a guess breaks on cards whose text only looks like a label.
- **A field per section (`action`, `worldsEnd`, `note`):** simple to read. Rejected: a card has any number of sections in any order, and each new card type adds fields.
- **Markup inside the text:** one string, as printed. Rejected: every consumer must parse it, and search and translation would see the markup.

## Consequences

- The card detail can show one block per effect, with its kind, and the Gene Pool in the card language.
- Translations are one list of texts per card later on; until then the app shows a translation's flat text as one plain section.
- Card data is longer, and a new card needs its sections split, which the data tests check: kinds on their card types, known tokens, no label or Gene Pool line left in a text.
