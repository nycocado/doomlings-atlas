# Game Rules (Legends of Enderas)

Summary of the official rules for the Doomlings Legends of Enderas expansion, written in our own words as a reference for implementing the app. It adds to [Classic](classic.md). The official rulesheet is authoritative; if this file disagrees with it, the rulesheet wins.

Source: [rulesheet](../../assets/doomlings/rules/legends-of-enderas.pdf) and its [transcription](../../assets/doomlings/rules/legends-of-enderas.md).

## Components

- Techlings: 21 traits (3 dominants), 2 ages, 1 catastrophe. They use attachments.
- Mythlings: 21 traits (3 dominants), 2 ages, 1 catastrophe. Some use attachments; discard effects on cards with the spell icon.
- Dinolings: 21 traits (3 dominants), 2 ages, 1 catastrophe. Their mechanics dig through the discard pile; the rulesheet warns they are fragile, with big points at a high risk.
- Fuzelings: 9 traits (1 dominant), 2 ages, 1 catastrophe. Combinations of Dinolings, Mythlings and Techlings, for bigger Drop of Life bonuses and new combos.
- Treasures: 26 cards.

## Attachments

Attachments work as in [Upgrade](upgrade.md#attachments-techlings), with these points in the Legends of Enderas rulesheet:

- They are played directly into the trait pile, unless the card says otherwise, onto a host. With no suitable host, the attachment is discarded.
- An attachment cannot be targeted for removal. An attachment cannot host an attachment, and a host has 1 attachment unless the card says otherwise.
- When the host is swapped or stolen, the attachment goes with it. When the host is discarded or returned, the attachment is discarded.
- Unless a card says otherwise, a dominant cannot host an attachment. A dominant attachment with a host counts as 1 dominant.
- An attachment cannot be considered for random removal; if its host is selected for it, the rules above apply.
- An attachment counts toward the trait count and the color count.
- Face value: when an attachment changes the variable (±) face value of its host, count the new face value and its Drop of Life bonus points.

## Treasures

### Setup

1. Shuffle the treasures and place them face down: the treasure pile.
2. Deal 1 treasure to each player.

Easy mode, suggested for learning: deal 3 treasures to each player and never draw more; these are the only treasures of the game.

### Gameplay

- A player keeps their treasures hidden from the others.
- When an age is revealed, a player with fewer than 2 treasures draws 1.
- Treasures are not part of the hand, for game effects or for stabilizing.
- A player may use any number of treasures in their turn. Most treasures have 2 effects; use 1 effect per treasure.
- After resolving a treasure's effect, put it face up in the coffer pile beside the treasure pile. When the treasure pile runs out, shuffle the coffer pile face down to make a new one.
- When the final catastrophe is revealed, all treasures are discarded immediately.

### Glyphs

Each effect has a glyph that says when it is played.

| Glyph  | When                                | Effect                                                                                                                                                 |
| ------ | ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Rush   | Before the start of your turn       | Play "rush" effects (there are a variety of them).                                                                                                     |
| Mutate | Start of your turn                  | Choose 1 trait from your trait pile and put it at the bottom of the trait deck, then play 1 trait of the specified color to replace it.                |
| Echo   | On your turn                        | After playing a trait of the specified color, you may play another trait of the same color.                                                            |
| Adapt  | On your turn                        | Play a trait with an action and ignore it, then play an action from any trait pile of the specified color. Treasures can also adapt effectless traits. |
| Cloak  | At any time                         | Play in response to a specified non-dominant effect and prevent it entirely. It prevents age, trait and treasure effects.                              |
