# Notice

Unofficial, not affiliated with or endorsed by Doomlings LLC.

Everything in this folder (card images, card text and the Doomlings name) is © Doomlings LLC, used with permission, and is **not** covered by this repository's GPL-3.0 license. The permission was granted to this project only; forks do not inherit it.

## Contents

- One folder per set (`classic/`, `overlush/`, ...), for the Classic (2nd edition) base game and the expansions, each with:
  - `cards.json`: the set's card data.
  - `pt.json`, `es.json`: unofficial Portuguese (Brazil) and Spanish (Latin America) translations of the set's card text (name, effect, flavor and World's End), written by this project and derived from the text in `cards.json`, which is © Doomlings LLC. They are not official translations.
  - Card images, one file per card, at the path given by each card's `image` field.
- `playmats/`: playmats (six player mats, the central mat and the trinkets mat), cropped from the same mod's table image. `central.jpg` shows the Doomlings logo: kept for reference only, never shown in the app.
- `rules/`: the rulesheets of the sets that have one (Classic, Upgrade, Imaginary Ends, Legends of Enderas, Castle Glass and Overlush), one `<set>.pdf` each, named after the set's folder. Each rulesheet has a `<set>.md` beside it: a transcription of its text, © Doomlings LLC like the rulesheet, written by this project so the rules can be read without the PDF. The expansion ones leave out what the mod's guide adds (see Sources).
- `screenshots/`: screenshots of this app for the README, in the light and dark themes, taken on an emulator. They show card images and card text.
- `symbols/`: card symbols, redrawn for this app in its sticker style from the symbols printed on the cards. One version each; the app colors them for its theme.

## Sources

- Card images come from Ryeera's [Doomlings Tabletop Simulator mod](https://steamcommunity.com/sharedfiles/filedetails/?id=3151925153), which uses high-resolution images provided by the Doomlings team. Card images were converted to lossy WebP at quality 80, about 541×757 pixels. Headless was scaled down from a larger image; Conjured, Immortal and The Bramble Brawler only exist at 400×562 in the mod and were scaled up.
- Card text and data come from the same mod. Each card was checked against its image.
- The Classic rulesheet is the [official PDF](https://cdn.shopify.com/s/files/1/0554/6190/4593/files/Doomlings_Rulebook.pdf?v=1709671623) from the Doomlings website, kept here because the site now links the 3rd edition rules.
- The rulesheets of Upgrade, Imaginary Ends, Legends of Enderas and Castle Glass are pages of the rules guide PDF embedded in the same mod ([PDF](https://steamusercontent-a.akamaihd.net/ugc/11787521121722816433/C759C9139A4B6BAE9EDDF63A7A06955A1411EF64/)), split by set and otherwise unchanged. Doomlings does not host a rulesheet for these expansions, so the guide is the source. It shows the official Doomlings rules cards laid out by Ryeera, with his own additions (page numbers, title banners, a thank-you note on the Castle Glass page and a photo and its caption on a Legends of Enderas page), which are not © Doomlings LLC.
- The Overlush rulesheet is the [PDF](https://officialgamerules.org/wp-content/uploads/2025/05/Doomlings_Official_Rules_02_Expansion_Overlush.pdf) linked from the [Overlush page](https://officialgamerules.org/game-rules/doomlings-overlush-mystery-expansion/) of officialgamerules.org, a third-party site that hosts it as the official Doomlings rules.
