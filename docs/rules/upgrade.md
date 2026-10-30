# Game Rules (Upgrade)

Summary of the official rules for the Doomlings Upgrade expansion, written in our own words as a reference for implementing the app. It adds to [Classic](classic.md). The official rulesheet is authoritative; if this file disagrees with it, the rulesheet wins.

Source: [rulesheet](../../assets/doomlings/rules/upgrade.pdf) and its [transcription](../../assets/doomlings/rules/upgrade.md).

## Components

- The Meaning of Life: 18 cards, each a hidden objective.
- Techlings: 15 traits (1 dominant), 1 age, 1 catastrophe. They introduce attachments.
- Dinolings: 15 traits (1 dominant), 1 age, 1 catastrophe. Their effects revolve around the discard pile and need no new rules.
- Mythlings: 16 traits (1 dominant), 1 age, 1 catastrophe. Their cards say what to do; no new rules.
- Multicolor: 1 dominant with all four colors, 6 traits with two colors and 1 colorless trait. A border with several colors counts as each of its colors, and icons above the face value show them for color-blind players.

Techlings, Dinolings and Mythlings are added to or removed from the decks to taste.

## Meaning of Life

### Setup

After dealing the traits as in Classic (5 each):

1. Shuffle the Meaning of Life cards.
2. Deal 2 to each player, face down.
3. Each player secretly keeps 1 and returns the other to the box. They may look at both before choosing.

### Scoring

- A Meaning of Life is a hidden objective. A player who achieves their card's objective earns extra points at the end of the game.
- When a card has several point thresholds, only the best applicable bonus counts.
- These points are counted after all other points and added to the total.

## Attachments (Techlings)

- A trait with the attach effect goes into the trait pile, unless the card says otherwise, but must be attached to a host trait, even when played at random. With no suitable host, it is discarded.
- Host: a dominant cannot be a host and an attachment cannot be a host. A host has at most 1 attachment.
- An attachment cannot be swapped, stolen or discarded by itself. By default, when its host is:
  - swapped or stolen: the attachment goes with it.
  - discarded or returned to a hand: the attachment is discarded.
- An attachment alone cannot be picked by a random discard, swap or steal. When the host is considered for one, set the attachment aside and shuffle the suitable targets; if the host ends up selected, follow the rules above.
- An attachment counts toward the trait count and the color count.

Open question: this rulesheet does not say whether an attachment may itself be dominant; the Legends of Enderas rulesheet only says that a dominant attachment with a host counts as 1 dominant ([legends-of-enderas.md](legends-of-enderas.md#attachments)).
