# 8. Card Language Defaults to the Device Language

- Status: accepted
- Date: 2026-10-25

## Context

[ADR 0007](0007-card-language-apart-from-interface.md) decided that card text shows as printed until the user picks a translation. Players who read Portuguese or Spanish want the cards translated from the first card, without finding the selector first, and the printed text is still one tap away. Android already tells the app which language the player reads: the system language, or the per-app choice on Android 13+.

## Decision

Until the player picks a card language, it is the first language of the app's configuration when it is a card language (Portuguese or Spanish), and English, the printed text, otherwise. Nothing is stored until the player taps the selector; once picked, the stored choice wins from then on, whatever the device language does. A card without a translation in the default language shows its printed text, as with any chosen language. The compendium search uses the same language. The interface language and the card language stay separate choices: ADR 0007 still holds for everything else.

## Alternatives Considered

- **Printed text until the user picks (ADR 0007):** the card language starts as English. Rejected: a player who reads Portuguese has to find the selector on every install before reading a card in it.
- **Card text always in the interface language:** no choice at all. Still rejected, as in ADR 0007: players matching a translated card against the printed one need to see the original. Only the starting point follows the device now.
- **Store the default on first launch:** writes the device language as if the player had picked it. Rejected: a later change of the device language would then never reach the cards, and the player never chose it.

## Consequences

- Players with a Portuguese or Spanish device read translated cards from the first launch; everyone else sees no change.
- Changing the device language changes the card language only for players who never picked one.
- Spanish works as a default before the Spanish interface ships, since it only reads the language.
- Language switching animates, as the player's own action ([design](../design.md)).
