# 4. Compendium Before Scoring

- Status: accepted
- Date: 2026-10-15

## Context

Scoring is the main reason for the app, but it depends on correct card data, and picking cards for scoring needs a searchable card list anyway. Card data comes partly from community sources and must be checked before use.

## Decision

The first milestone is the card database and the compendium. Manual scoring comes second and reuses the compendium's searchable list as its card picker.

## Alternatives Considered

- **Scoring first:** delivers the main feature sooner, but builds on unverified data and a throwaway card picker.

## Consequences

- The project structure, card database and images are proven on a simple feature before the scoring logic.
- The compendium is the tool to review card data visually.
- The first milestone already needs card images and full card text, so asset sourcing must be solved up front.
