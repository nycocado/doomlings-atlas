# 5. Scoring Rules in Card Data

- Status: accepted
- Date: 2026-10-16

## Context

[ADR 0003](0003-card-data-json-effects-kotlin.md) put every special scoring effect in Kotlin, linked by card identifier, and left a hybrid approach to revisit once the real effects were known. With all 902 cards extracted, about 48 Classic cards and 225 across the expansions change the final score. Most of them repeat a few patterns ("+1 for each red trait", "value is equal to the number of Kidneys"), and only a handful need logic of their own. One Kotlin entry per card would repeat the same code dozens of times.

## Decision

- A card's scoring rule lives in its card data entry, as a call to a named Kotlin scoring function with parameters, e.g. `{"fn": "perEach", "amount": 1, "zone": "ownPile", "color": "red"}`.
- Generic functions cover the common patterns. Cards with unique logic call a function written only for them, e.g. `{"fn": "symbiosis"}`.
- Cards without a scoring rule score their printed face value.
- Player choices and state changes at World's End (which trait to discard, a chosen color, a drawn card) are app input, not scoring rules.

## Alternatives Considered

- **One Kotlin effect per card (ADR 0003):** type-checked, but repeats the same logic for every card that shares a pattern and splits each card between data and code.
- **Text expression language:** flexible, but needs a parser and its own error handling.
- **Embedded scripts (Lua, JavaScript):** most flexible, but adds a script engine to every platform and makes effects hard to test.

## Consequences

- Most new cards need only data; Kotlin changes only for a new pattern or a unique effect.
- Scoring logic stays in Kotlin, type-checked and unit-testable.
- A test must check that every `fn` in the card data exists and gets valid parameters, so no card silently scores wrong.
- The set of generic functions is defined while implementing scoring and grows with new patterns.
