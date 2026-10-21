# Roadmap

Architecture decisions behind this plan are in [docs/adr](docs/adr).

## 0. Foundations

Everything here comes before assets and code.

- Visual identity: app logo and icon, without the Doomlings logo.
- Design system.
- Set to start with: Classic (2nd edition) base game, no expansions.
- Card sources: images from [Ryeera's Tabletop Simulator mod](https://steamcommunity.com/sharedfiles/filedetails/?id=3151925153), card data checked against the [official Doomlings Compendium](https://www.doomlings.com/pages/compendium).

## 1. Card Data and Compendium

- Embedded card database with what is printed on each card: name, set, type, color, points, full text and image.
- Every card verified against a reliable source (physical cards, official material or checked community assets such as Tabletop Simulator mods).
- Compendium: card list with images, search by name, filters by printed attributes, card detail.
- The notice "Unofficial, not affiliated with or endorsed by Doomlings LLC" shown on first launch and always available on an "About" screen.
- Interface in English and Portuguese, following the system language (no in-app selector); card text in English.
- Tests for database integrity: unique identifiers, required fields, every card with an image.
- Screenshot tests from the component previews, once the design system settles.

## 2. Card Text Translation

- Portuguese and Spanish, reviewed by the maintainer; other languages through community contributions.
- Translated card text shown in the compendium, chosen apart from the interface language: card text stays as printed until the user picks a translation, with a language selector in the card detail.
- App interface also in Spanish.
- UI tests with Compose and Robolectric, in CI: navigation on phone and tablet sizes (opening, switching and closing cards, About) and state surviving activity recreation.

## 3. Manual Scoring

- Players added by name at the start of the game, each with the Gene Pool card in front of them.
- Players enter the full final state: each player's trait pile and hand, catastrophes, World's End.
- Score calculated automatically, with a final ranking and a breakdown of where each point came from.
- Manual adjustment per player for cards missing from the database and house rules.
- Card picker reuses the compendium's searchable list.
- An in-progress game survives the app being killed; no game history.
- Tests for every card with a special effect, plus real games with hand-checked scores.

## 4. Card Scanner

- Card recognition from the camera, running on the device.
- Compendium search by image: photograph a card to open it.
- In a photo of the whole table, each player's Gene Pool card identifies which trait pile belongs to whom.

## 5. Game Tutorial

- Interactive tutorial in the app languages.
- Links to the official rulebook and videos.

## Not Planned Yet

Game history, collection tracking, favorite cards, iOS app, donations, compendium filters beyond color and type (points, icons, subspecies), swiping between cards in the card detail, an error screen for when the card data fails to load.
