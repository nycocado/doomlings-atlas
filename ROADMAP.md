# Roadmap

Architecture decisions behind this plan are in [docs/adr](docs/adr).

## 0. Foundations

Everything here comes before assets and code.

- Visual identity: app logo and icon, without the Doomlings logo.
- Design system.
- Edition to start with.
- Reliable source to verify card data.

## 1. Card Data and Compendium

- Embedded card database with what is printed on each card: name, edition, type, color, points, full text and image.
- Every card verified against a reliable source (physical cards, official material or checked community assets such as Tabletop Simulator mods).
- Compendium: card list with images, search by name, filters by printed attributes, card detail.
- The notice "Unofficial, not affiliated with or endorsed by Doomlings LLC" shown on first launch and always available on an "About" screen.
- Interface in English and Portuguese; card text in English.
- Tests for database integrity: unique identifiers, required fields, every card with an image.

## 2. Manual Scoring

- Players added by name at the start of the game, each with the Gene Pool card in front of them.
- Players enter the full final state: each player's trait pile and hand, catastrophes, World's End.
- Score calculated automatically, with a final ranking and a breakdown of where each point came from.
- Manual adjustment per player for cards missing from the database and house rules.
- Card picker reuses the compendium's searchable list.
- An in-progress game survives the app being killed; no game history.
- Tests for every card with a special effect, plus real games with hand-checked scores.

## 3. Card Text Translation

- Portuguese and Spanish, reviewed by the maintainer; other languages through community contributions.
- Translated card text shown in the compendium.
- App interface also in Spanish.

## 4. Card Scanner

- Card recognition from the camera, running on the device.
- Compendium search by image: photograph a card to open it.
- In a photo of the whole table, each player's Gene Pool card identifies which trait pile belongs to whom.

## 5. Game Tutorial

- Interactive tutorial in the app languages.
- Links to the official rulebook and videos.

## Not Planned Yet

Game history, collection tracking, iOS app, donations.
