# 6. Own Design System on Compose Foundation

- Status: accepted
- Date: 2026-10-18

## Context

[ADR 0001](0001-kotlin-multiplatform-android-first.md) lists Material 3 for the `app` module. The app's visual design ([docs/design.md](../design.md)) is a sticker style: thick outlines, hard offset shadows, tilted cards, a floating pill dock and round color discs. Material 3 components use soft elevation, their own indicators and shapes, and none of them match these screens without fighting their defaults.

## Decision

- The `app` module has its own theme (`AtlasTheme`) built from `design/tokens.json`, and its own components (sticker card, search pill, color disc, segmented selector, dock, badges) on Compose Foundation.
- Material 3 stays as a dependency only for behavior that is costly to rebuild: bottom sheets, dialogs, snackbars and ripple, themed with the same tokens.

## Alternatives Considered

- **Themed Material 3 components:** keeps ADR 0001 as written, but buttons, cards, navigation bar and text fields keep Material shapes, elevation and indicators, far from the designed screens.
- **No Material 3 at all:** full control, but sheets, dialogs and their accessibility would be written and maintained by hand.

## Consequences

- The screens can match the design closely, and the look changes in one place (tokens and a few components).
- Each component needs its own accessibility work (semantics, touch targets, focus), which Material 3 components would give for free.
- Contributors should not use Material 3 visual components (`Button`, `Card`, `NavigationBar`, `TextField`) in screens; the project components replace them.
