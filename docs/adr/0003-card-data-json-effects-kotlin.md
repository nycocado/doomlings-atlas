# 3. Card Data in JSON, Scoring Effects in Kotlin

- Status: superseded by [0005](0005-scoring-rules-in-card-data.md)
- Date: 2026-10-15

## Context

Every card has printed data (name, edition, type, color, points, text, image), and some cards have special scoring effects that depend on the rest of the game state. Effects vary a lot from card to card.

## Decision

- Printed card data lives in embedded data files, one entry per card with a stable identifier reused across features.
- Special scoring effects are Kotlin code, linked to cards by identifier.

## Alternatives Considered

- **Effects as data (a small rules language):** lets contributors add cards without code, but requires designing and maintaining a rules language that covers every effect.
- **Hybrid:** common effect patterns as data, odd ones in code. Worth revisiting once the real effects show which patterns repeat.

## Consequences

- Effects are type-checked and unit-testable.
- Adding a card with a special effect requires Kotlin code, not only data.
- A test must ensure every card marked as having an effect has one implemented, so no card silently scores zero.
