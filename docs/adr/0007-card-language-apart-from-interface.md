# 7. Card Language Apart from Interface Language

- Status: accepted
- Date: 2026-10-20

## Context

The app ships its interface in English and Portuguese, and phase 2 adds translated card text. Players hold the physical cards, printed in English, and many read them in another language. The first plan was a "PT" button in the compendium header that switched the whole app's language. Changing the interface language is a one-time choice, while switching card text between the printed original and a translation is something a player does while reading a card. Android 13+ also lets users pick a language per app in system settings.

## Decision

The interface follows the system language, with no language selector in the app. Card text has its own language: it shows as printed until the user picks a translation, through a language selector in the card detail. The two choices never change each other.

## Alternatives Considered

- **One app language switch ("PT" button):** a single choice for interface and cards. Rejected: it puts a rare action on the main screen and forces players to translate both or neither.
- **Card text always in the interface language:** no choice at all. Rejected: players matching a translated card against the printed one need to see the original.
- **In-app interface language setting:** needed only for Android 8 to 12, which lack the system's per-app language. Rejected for now: it needs AppCompat for a minority of users; the system language covers them.

## Consequences

- Phase 1 only adds Portuguese strings and a `locales_config`; no button, no extra dependency.
- Card text language is stored and read on its own, so the translation format (phase 2) must allow any card to fall back to its printed text.
- Users on Android 8 to 12 get the interface in their system language only.
- The compendium header keeps a single button, for About.
