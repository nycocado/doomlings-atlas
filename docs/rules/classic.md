# Game Rules (Classic)

Summary of the official rules for Doomlings Classic (2nd edition), the edition this app targets. It is a reference for implementing the app, written in our own words. The official rulesheet is authoritative; if this file disagrees with it, the rulesheet wins.

Source: [official rulesheet](../../assets/doomlings/rules/classic.pdf) and its [transcription](../../assets/doomlings/rules/classic.md) ([original link](https://cdn.shopify.com/s/files/1/0554/6190/4593/files/Doomlings_Rulebook.pdf?v=1709671623), 2024 upload, matches the Classic card counts: 25 ages, 15 catastrophes, 1 Birth of Life).

## Components

- Traits: 4 colors (blue, green, purple, red) plus colorless (gray). Some traits are dominant.
- Ages: 25 normal ages, 15 catastrophes and The Birth of Life. All share the same back.
- Gene Pools: one per player. The number at the top is the player's hand size.

## Setup

1. Separate The Birth of Life and the catastrophes from the ages.
2. Shuffle the ages and deal 3 face-down piles of 3. The remaining ages are not used.
3. Shuffle the catastrophes and add 1 to each pile. Shuffle each pile separately (4 cards each).
4. Stack the 3 piles, put The Birth of Life on top and place the age deck in the center.
5. Each player takes a Gene Pool, set to 5.
6. Shuffle the traits, deal 5 to each player and place the rest next to the age deck as the trait deck.

## Round

1. The 1st player (left of the dealer) flips the next age. The Birth of Life only starts the game.
2. Everyone applies the age effect before taking turns. Age effects last 1 round.
3. In clockwise order, each player takes a turn.

### Turn

1. Play 1 trait from hand, face up, into your trait pile. If you can play a trait, you must.
2. If it has an effect, apply it now.
3. Stabilize: draw or discard until your hand size equals your Gene Pool. Your turn ends.

If your hand is empty, draw 5 and skip your turn. If you hold cards but cannot play any, skip your turn; you may discard your whole hand and draw 3. In either case, do not stabilize.

## Catastrophes

When a catastrophe is flipped:

- The 1st player position moves 1 player to the left.
- Every player applies its Gene Pool effect. It is permanent.
- Every player follows its catastrophic effect before turns. It lasts that round.
- Its gold World's End effect is ignored until the 3rd catastrophe.

Each catastrophe ends an era. A finished game has 3 eras.

## World's End

Flipping the 3rd catastrophe ends the game. No more turns are taken and the 1st player does not move.

1. Apply its Gene Pool and catastrophic effects as usual.
2. In turn order, each player resolves the World's End effects (World's End icon) in their trait pile.
3. Resolve the catastrophe's World's End effect.

## Scoring

1. Points given or taken by the 3rd catastrophe's World's End effect.
2. Face value of every trait in your trait pile. Compass-star ("value is equal to") traits count as 0 here.
3. Bonuses and modifiers from traits with the Drop of Life icon.

Tie: each tied player draws a trait from the deck and adds only its face value.

## Detailed Rules

### Effects

- Text at the bottom of a card is an effect; traits without text are effectless.
- Trait effects apply while the trait is in your trait pile. If you lose the trait, you lose the effect.
- In most cases you cannot choose to ignore an effect.

### Icons

- **Action:** happens once, only when the trait is played directly from your hand. If able, you must play it. Actions are not played on traits that are stolen, swapped, or played from the discard pile, the deck or by other means, unless told to.
- **Play When:** can be played out of turn. Doing so does not count as your turn; do not stabilize until the end of your own turn. It can also be played as a normal turn.
- **World's End:** effect resolved at the end of the game.
- **Drop of Life:** bonus or modifier counted during scoring.
- Expansion icons in the top-right corner identify the set.

### Terms

- **Face value:** the number at the bottom of a trait. It is constant; bonuses are counted separately.
- **Randomly:** when a card is removed at random, flip the set face down and let an opponent pick (ideally the receiving opponent, otherwise the active player chooses).
- **Swap / steal:** from a hand, the card goes to your hand; from a trait pile, it goes to your trait pile.
- **Requirements** ("To play, you must..."): must be met to play the trait from hand, the deck or the discard pile. If a trait played at random cannot meet its requirement, discard it. Requirements are ignored when the trait is moved, swapped or stolen, unless stated.
- **Restrictions:** if a restriction blocks a required part of an effect, the trait cannot be played. If the blocked part is not required, play the trait and ignore that part.

### Gene Pool

- Gene Pool changes do not make you draw or discard; only stabilizing does.
- Catastrophes change all Gene Pools immediately and permanently.
- Traits with Gene Pool effects apply them when they enter your trait pile and reverse them when they leave.
- The Gene Pool stays between 1 and 8; ignore an effect that would go past either limit. A trait leaving the pile always reverses its effect.

### Dominant Traits

- Marked with a gold border, a gold star and "Dominant" at the top.
- Once played, they cannot be swapped, stolen, discarded, returned or removed from the trait pile.
- Each player can play only 2. A 3rd dominant that would enter your pile at random (e.g. played from the top of the deck) is discarded, with no redraw.
- Exception: the age Birth of a Hero lets you play Heroic immediately, even as a 3rd dominant. This is the only way to have 3.

### Colors

- Blue, green, purple and red. Colorless traits don't count for color effects.
- Dual-color borders (expansions) count as both colors.
- The lowest or highest color count is the color with the fewest or most traits.
- A "color pair" is 2 traits of the same color.

### Discard Pile and Disputes

- Discards go face up next to the trait deck. Reshuffle it into a new deck only when the traits run out.
- Precedence: ages, then traits, then the rulesheet.
