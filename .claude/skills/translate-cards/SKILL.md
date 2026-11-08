---
name: translate-cards
description: Translate card text (name, effects, flavor) into a language of docs/glossary, in its regional variant and in idiomatic style. Use when translating a set, adding a language, or rewording a card translation.
---

# Translate Cards

A card translation reads as if a native speaker of the glossary's variant wrote the card, never as _translatese_: effects as precise as the printed rule, names and flavor as natural as the printed English.

## 1. Read the Language

From `docs/glossary/<lang>.json`:

- `language`: the variant every line is written in (pt-BR, es-419).
- `styleNote`: the tone and regional choices of that variant.
- `keep` and `keepNote`: the English terms, their gender in running text, and how cited card names are written.
- `cards`: the one word per concept, and the synonyms `./gradlew check` rejects.

The rules shared by every language are in [CONTRIBUTING.md#translations](../../../CONTRIBUTING.md#translations).

## 2. Translate

Write into `assets/doomlings/<set>/<lang>.json`, keyed by card id, with exactly the text fields the card has in the set's `cards.json` (`name`, `effects`, `flavor`). `effects` is a list of texts, one per effect of the card in the same order ([ADR 0009](../../../docs/adr/0009-card-text-as-a-list-of-effects.md)): each is the text of that effect only, without its kind's label ("At World's End:", "Awesome Power:", "Rush:"...), the Gene Pool line or the Value, which the app writes in the card language. Check each card against its image in the same folder: the art often explains a pun or a name.

- **Effects** say the printed rule with the glossary's words, keeping the card's line breaks and sign formats (`+2`, `-1 Gene Pool`). Tokens (`{action}`, `{compass}`) stay as in the English text, after the word they follow there: "Ignore actions ({action})." is "Ignore ações ({action})." Ink marks (`[red]…[/red]`) keep their inks in the English order, around the words that translate the marked ones: "Play a [red]red[/red] trait." is "Jogue um trait [red]vermelho[/red]."
- **Names** are what a speaker of the variant would call that trait: an everyday word over a technical one, matched to the art.
- **Flavor** carries the joke, not the words. A pun becomes a local pun or idiom that fits the art; a reference becomes one the variant's players know, or stays in English when the local release kept it. When nothing works, a plain line beats a forced pun.
- A term missing from the glossary goes into the glossary first.
- A gap or a mismatch between `cards.json` and the image is a data doubt for the PR, never filled in.

Done when every card in scope has every field it has in `cards.json`, and `./gradlew checkGlossary` and the translation data tests (`./gradlew :shared:testAndroidHostTest --tests '*TranslationDataIntegrityTest'`) pass.

## 3. Review

Per language, a fresh subagent, with none of your context, gets the printed text, the translation, the glossary and its `styleNote`, and reads every card as a native player of the variant. It reports each line of translatese and each effect that drifts from the printed rule. You decide each point and apply the fixes.

Done when every reported line is fixed or kept with a reason.

## 4. Open for Review

The maintainer reviews every line. The PR carries a review comment with:

- a table per card and field: printed / each language;
- the non-obvious choices: names, puns, references, terms added to the glossary, each with its reason;
- the data doubts.

## Examples

From the Classic set, in `assets/doomlings/classic/pt.json` and `es.json`:

| Printed | pt-BR | es-419 | Why |
| --- | --- | --- | --- |
| Doting | Paparicador | Consentidor | the everyday word for someone who spoils others |
| Prepper | Precavido | Precavido | an everyday adjective, like the other trait names |
| The sharpest tools in the head. | Mais afiadas que língua de sogra. | Más afilados que la lengua de mi suegra. | a local saying in place of the English idiom |
| 5-alarm chili? Fire in the hole! | Feijão com pimenta? Sai de baixo! | ¿Frijoles bien picantes? ¡Cuerpo a tierra! | the dish and the cry each variant knows |
| My bite is worse than my bark. | Eu não ladro, mas mordo. | Yo no ladro, pero muerdo. | the proverb's local form |
| More like Apocalypse No. | Tá mais para Apocalypse No. | Más bien Apocalypse No. | the local releases kept the film title |
| When this is over, my victory will be shellebrated! | …vai ser cascomemorada! | …¡con toda la coraza! | the pun rebuilt; in es a pun on `concha` reads vulgar in parts of South America |
| I'd like to make a toast. | Gostaria de propor um brinde. | Quisiera proponer un brindis. | the bread pun has no equivalent: a plain line |
