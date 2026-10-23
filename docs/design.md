# Design

The app's visual design: a game table at sunset, with **stickers** (thick outline, hard shadow, everything rounded like the doomlings) and **cards lying on the table**, slightly tilted. Built in Compose on our own design system ([ADR 0006](adr/0006-own-design-system.md)).

This file covers the look only. Screen flows, scoring steps and game rules are decided per feature, following [ROADMAP.md](../ROADMAP.md) and [rules.md](rules.md).

## Files

- [`design/tokens.json`](../design/tokens.json): colors for both themes, type, spacing, radii, strokes and shadows. Source of truth for the Compose theme.
- [`design/brand/`](../design/brand): app logo, interface icons, trait color glyphs and the adaptive app icon layers.
- [`design/screens/`](../design/screens): prototype screens in both themes, with placeholder cards. A visual reference only: their content, steps and rules are suggestions, not decisions.
- [`assets/doomlings/symbols/`](../assets/doomlings/symbols): card symbols, redrawn from Doomlings originals. Doomlings material, see its [notice](../assets/doomlings/NOTICE.md).

## Principles

- **Sticker, not Material.** Every control and card is a sticker: `surface-sticker` fill, `outline` border at `stroke-sticker` (2.5) and, when tappable or emphasized, `shadow-sticker` (hard offset shadow, no blur). No soft elevation, no gradients.
- **The table at sunset.** Floor is `surface` (peach paper by day, purple night in the dark theme). Headers and the dock use `inverse-surface`, always with the **sunset band** below: `sunset-red`, `sunset-orange`, `sunset-yellow`, `sunset-sea`, in four equal blocks. On scrolling screens the header title and band collapse as the content scrolls and come back at the top; the search stays.
- **Cards are objects.** Cards have an outline and `shadow-sticker-lg`, tilted between −3° and +2.5°, deterministic by position (never random per render). Never cropped, nothing drawn over the art.
- **Color is data.** The five trait colors (`trait-blue`, `trait-green`, `trait-purple`, `trait-red`, `trait-colorless`) appear only where they mean a card's color. Sunset tones identify players and decorate; they never mean a trait color.
- **Unofficial, and saying so.** The notice "Unofficial, not affiliated with or endorsed by Doomlings LLC" shows on first launch and stays one tap away, on About. No Doomlings logo, nothing imitating its lettering.

## Tone

- Playful and direct, second person ("Which doomling are you looking for?").
- Game terms stay as printed: Trait, Age, Catastrophe, Dominant, Gene Pool, World's End.
- Sentence case in titles and buttons. Uppercase only on sticker labels (`label-sm`: "WORLD'S END", "DOMINANT").
- No emoji. Signed numbers use the typographic minus (−1, +3) and tabular figures.

## Color

Two themes: `light` ("Day", default) and `dark` ("Game night"). No Android dynamic color.

- `on-surface` on `surface` / `surface-sticker*`: main text. `on-surface-variant`: secondary (≥6:1).
- `inverse-surface` + `on-inverse-surface`: headers, dock.
- `primary` / `on-primary`: filled action and active segment (ink by day, yellow at night).
- `highlight` / `on-highlight`: what is selected now (active dock item, chosen option, current step, points badge, dominant).
- `sunset-*` with `on-sunset`: player colors and forward buttons in flows.
- Trait: `trait-<color>` (exact card color, same in both themes) with `on-trait-<color>` on top; `trait-<color>-ink` for glyphs and names on surfaces; `trait-<color>-container` for soft backgrounds.
- Borders and shadows use `outline`: ink by day, light lilac at night. That keeps stickers readable in the dark.

## Typography

- **Fredoka** (`display`) for titles, card names and numbers; **Atkinson Hyperlegible Next** (`sans`) for body, card text and labels. Both are OFL on Google Fonts and ship in the app for offline use.
- Scale (sp): `display` 36, `headline` 30, `title-lg` 22, `title-md` 16, `numeral-xl` 64, `numeral-lg` 32, `numeral` 28, `body-lg` 18 (card effects, weight 600), `body-md` 15, `body-sm` 13, `label-lg` 15, `label-sm` 12. Test at 200% font scale.

## Shape and Space

- 4dp grid: `space-1` 4 · `space-2` 8 · `space-3` 12 · `space-4` 16 · `space-5` 20 · `space-6` 24.
- Radii: `radius-card-sm` 10 and `radius-card-lg` 16 (cards), `radius-md` 22 (stickers), `radius-lg` 32 (header base and panels), `radius-full` (pills, discs, dock).
- Strokes: `stroke-thin` 2, `stroke-sticker` 2.5, `stroke-strong` 3. Shadows: `shadow-sticker` 3 and `shadow-sticker-lg` 5, always offset down and right.
- Minimum touch target: 48dp.

## Logo and App Icon

- The logo is the **trait compass rose**: four points in the exact card colors (north blue, east green, south red, west purple), a colorless disc in the center, sticker outline and shadow. The name is Fredoka 700 converted to curves.
- `mark.svg` on light backgrounds, `mark-dark.svg` on `inverse-surface`, `mark-flat.svg` below 32px, `mark-mono.svg` for single-color uses. Lockups pair the mark with the name. Clear space: half the mark's height. Minimum size: 16px (flat), 120px wide for the horizontal lockup.
- Never recolor or rotate the points, never set the name in another font, never place it near the official game logo.
- The adaptive app icon is `mark-dark` on a plain `inverse-surface` background. Layers in `design/brand/app-icon/` (108dp grid, foreground within the 66dp safe zone), plus a monochrome layer for Android 13+ themed icons. No text, nothing from the official logo.

## Icons

- **Trait glyphs:** drop = blue, leaf = green, hexagon = purple, flame = red, dashed ring = colorless. 24×24, one ink, tinted with `trait-<color>-ink`. Always next to the color name or in `contentDescription`.
- **Interface icons** for game concepts: Gene Pool, catastrophe, age, trait, Meaning of Life, trait pile, hand, face value, scanner, swap, steal. 24 grid, 2.2 stroke, round caps and joins, tinted with `on-surface`.
- **Card symbols** (action, play when, spell, suppress, attachment, World's End, drop of life, dominant, compass star, subspecies, tags): redrawn in the sticker style, recognizable next to the printed card. They are the only version of these symbols, both inside card text and in the interface. Inside text: 1.3em high (action 1.15em), centered on the line, always with `contentDescription`.
- Generic icons (back, search, more): Material Symbols Rounded at a matching weight.
- In the app, SVGs become vector drawables with colors bound to theme tokens (one drawable per symbol, not per theme).

## Components

Visual specs only; content and behavior are defined with each screen.

- **Card thumbnail:** card image with `outline` border at `stroke-sticker`, `radius-card-sm`, `shadow-sticker-lg` and a tilt between −2.5° and +2.5°. Name below in `title-md` with the trait glyph. No badges over the art.
- **Card detail:** top panel in `trait-<color>` with a dot pattern in `on-trait-<color>` (ages and catastrophes in `surface-sunk` with dots in `outline-soft`), `radius-lg` base, large card tilted at a random angle between −3° and −1.5° or +1.5° and +2.5° (never near straight), picked when it opens and kept while it stays open, with `stroke-strong` and `shadow-sticker-lg`, large points badge stuck on the panel edge. Name in `display`; pills for color (glyph + name), type and keywords. Effect text in a `surface-sticker` bubble (`radius-md`, `shadow-sticker-lg`) in `body-lg`. Round 48dp back button with `shadow-sticker`.
- **Color disc filter:** 48dp disc filled with `trait-<color>`, glyph in `on-trait-<color>`, label below. Selected: double ring (`surface` + `outline`, 3 each); glyph and label stay.
- **Segmented selector:** `surface-sticker` track with outline; active segment in `primary` / `on-primary`.
- **Search field:** 52dp `surface-sticker` pill with `outline` border, placeholder in `on-surface-variant`, inside the dark header.
- **Points badge:** round `highlight` sticker with the value in `numeral` (`numeral-lg` at 68) and `outline` border. Sizes 28, 40 and 68 (detail, tilted +8° with `shadow-sticker`). Same yellow regardless of card color.
- **Dominant badge:** `highlight` pill with star and "DOMINANT" in `label-sm`, or the star alone in a 22dp disc. Icon-only always has `contentDescription`.
- **Player token:** round token with the initial on a sunset color (yellow, orange, red, sea, in order) and the name below; the name always shows. Editing: double ring. Done: check seal in `primary`. Add: dashed disc. In tabs it becomes a pill (initial + name), active in `highlight`.
- **Step header:** steps in the dark header. Done: `on-inverse-surface` disc with check. Current: `highlight` disc with the number, bold label. Upcoming: outline only.
- **Score breakdown:** `surface-sticker` card with rows (icon, title, explanation, signed tabular value in `numeral` 24) split by dashed `outline-soft`. Manual adjustments are marked with a pencil. A color summary of five `trait-<color>` blocks with glyph and points is read-only.
- **Dock:** floating `inverse-surface` pill, active item in `highlight`. On wide windows it becomes a vertical rail on the left, below the header, with icons only (labels go to accessibility).
- **Round button:** 48dp sticker disc with a 22dp icon, sinking into its shadow while pressed. On a surface or panel it uses `outline`; on a dark header, a `surface-sticker` disc with `on-inverse-surface-variant` outline and shadow (back on About), or a quiet `inverse-surface` disc with the icon in `on-inverse-surface` for secondary actions (info on the compendium).
- **Unofficial notice:** footer in `body-sm`, `on-surface-variant`, centered and underlined, at the end of the card detail; tapping it opens About. The compendium has no footer: an info button in its header opens About. On first launch and About: a sticker with an "Unofficial" label in `highlight`, the sentence in bold and a short explanation. Never hidden behind a tap, never near the game logo.

## Wide Windows

From 840dp wide (tablets, unfolded foldables, phones on their side):

- **Compendium:** with at least 600dp of its own (alone on the screen, or beside a card on a large tablet), the header is a single row that doesn't collapse: logo and title in the left corner, info button in the right corner, search in the middle of the screen (up to 480dp), narrowing toward the end when there isn't room. The dock becomes the rail. Color discs and type segments center on the screen; the section title stays in line with the first column of cards.
- **List and card:** the list fills the screen until a card opens. Then the list and the card share the width half and half; another card replaces the open one. The list jumps to its new width and the cards glide to their new places; the card's pane slides in and out.
- **Narrower compendium** (beside a card on a foldable or a small tablet): the phone layout, with the stacked header and the dock at the bottom.
- **Card beside the list:** a sheet hanging from the top of the screen, a sticker on its other sides (`outline` border, `shadow-sticker-lg`, `radius-lg` bottom corners). The info button and its space go to the search, since the card has its own way to About.
- **Status bar:** its icons stay light, as the list's dark header sits under them; a 20% black shade behind the bar keeps them readable over light card panels. On phones, the card detail picks the icon color that the design puts on its panel.

## Accessibility

- Text ≥4.5:1 against its background in both themes; borders, glyphs and focus ring ≥3:1.
- Trait color is never the only cue: glyph + name, double ring on selection, color in `contentDescription`.
- Focus: `focus-ring` of 3 with a 3 gap.
- Card tilts are static; nothing moves without user action.
