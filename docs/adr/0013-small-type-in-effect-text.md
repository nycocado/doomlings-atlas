# 13. Small Type in Effect Text

- Status: accepted
- Date: 2026-11-19

## Context

A card prints some words of a section in small type, such as "(Cannot discard dominants.)" on Spiny Fins, inside the section they qualify. ADR 0009 stored small-type notes as effects of kind `note`, which the card detail showed as a block of their own, while most small type sat unmarked in the plain text of an effect, such as Nectar's "(Excluding dominants.)". So neither the block nor the type size matched the card.

## Decision

- An effect's text marks a span printed in small type in brackets, like an ink: `[small](Excluding dominants.)[/small]`. It is a type size, not an ink: a small span may hold ink spans and tokens, but no other small span, and no ink span crosses its marks.
- `shared` reads it in the same reading of the text as the inks and tokens: the card face's text parts carry a small flag, and search and citations see the words without the mark. The card detail draws small parts in a smaller type, in the same block.
- The `note` kind is gone: each note joins the effect it is printed in.
- A translated effect has the small spans of its English effect, in count, order and place: on the same line, at its start or end or within it, around the same inks and tokens. The data tests check this and that every small span is well formed.

## Alternatives Considered

- **A `note` field on the effect:** keeps the text free of a new mark. Rejected: it can't place a note in the middle of the text, as on Nectar, and translations would need a second text per effect.
- **Keep the `note` kind and move the parentheticals into it:** no new markup. Rejected: a note is still a block apart from its section, and not every small span is a parenthetical.

## Consequences

- The card detail shows small type where and as the card prints it.
- Translators keep one more mark around the matching words; the data tests catch one lost, added, moved or malformed.
- Which words are small is read from each card image, never from the text, since not every parenthetical is small type.
