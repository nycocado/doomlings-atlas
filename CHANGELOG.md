# Changelog

Notable changes in each release of Doomlings Atlas, generated from the commit history by [git-cliff](https://git-cliff.org).

## 0.2.0 (2026-10-31)

### Features

- Card text translation ([#121](https://github.com/nycocado/doomlings-atlas/pull/121))
- Interface in spanish ([#126](https://github.com/nycocado/doomlings-atlas/pull/126))
- Special set card translations ([#130](https://github.com/nycocado/doomlings-atlas/pull/130))
- Castle-glass set card translations ([#131](https://github.com/nycocado/doomlings-atlas/pull/131))
- Nightflower-island set card translations ([#132](https://github.com/nycocado/doomlings-atlas/pull/132))
- Shadow-puppets set card translations ([#133](https://github.com/nycocado/doomlings-atlas/pull/133))
- Mokoko-village set card translations ([#134](https://github.com/nycocado/doomlings-atlas/pull/134))
- Legends-of-enderas set card translations ([#135](https://github.com/nycocado/doomlings-atlas/pull/135))
- Upgrade set card translations ([#136](https://github.com/nycocado/doomlings-atlas/pull/136))
- Imaginary-ends set card translations ([#137](https://github.com/nycocado/doomlings-atlas/pull/137))
- Overlush set card translations ([#138](https://github.com/nycocado/doomlings-atlas/pull/138))
- The birth of life card translations ([#140](https://github.com/nycocado/doomlings-atlas/pull/140))
- Card groups: cards that share a name stack and open in place ([#143](https://github.com/nycocado/doomlings-atlas/pull/143))
- Card names cited in card text open that card ([#156](https://github.com/nycocado/doomlings-atlas/pull/156))
- Compendium search tolerates typos ([#166](https://github.com/nycocado/doomlings-atlas/pull/166))
- Card data in effects, with Gene Pool and Value as numbers ([#167](https://github.com/nycocado/doomlings-atlas/pull/167))
- Card translations in effects ([#188](https://github.com/nycocado/doomlings-atlas/pull/188))
- Creature symbols and the missing tag banners ([#189](https://github.com/nycocado/doomlings-atlas/pull/189))
- Card detail shows each effect as its own block ([#190](https://github.com/nycocado/doomlings-atlas/pull/190))
- Ink marks in effect text ([#197](https://github.com/nycocado/doomlings-atlas/pull/197))
- Ink in the card data and translations ([#200](https://github.com/nycocado/doomlings-atlas/pull/200))
- Card detail draws ink ([#199](https://github.com/nycocado/doomlings-atlas/pull/199))
- Catastrophe World's End and Pocket Objective in yellow ([#198](https://github.com/nycocado/doomlings-atlas/pull/198))
- Age of Wonder and The Birth of Life in their printed colors ([#202](https://github.com/nycocado/doomlings-atlas/pull/202))

### Fixes

- Layout switch without the two-frame flash ([#46](https://github.com/nycocado/doomlings-atlas/pull/46))
- Card pane slides out with the card beside the list ([#48](https://github.com/nycocado/doomlings-atlas/pull/48))
- List grows as the card beside it closes ([#49](https://github.com/nycocado/doomlings-atlas/pull/49))
- Main guard reads the branch where the command runs ([#51](https://github.com/nycocado/doomlings-atlas/pull/51))
- Compendium labels whole at 200% font scale ([#81](https://github.com/nycocado/doomlings-atlas/pull/81))
- Header follows the list after leaving the rail layout ([#82](https://github.com/nycocado/doomlings-atlas/pull/82))
- Search field no longer takes focus on its own in a wide window ([#85](https://github.com/nycocado/doomlings-atlas/pull/85))
- Gene pool images in webp like the other cards ([#154](https://github.com/nycocado/doomlings-atlas/pull/154))
- Card images off the standard size ([#158](https://github.com/nycocado/doomlings-atlas/pull/158))
- Card text errors from the source data ([#160](https://github.com/nycocado/doomlings-atlas/pull/160))
- Tapping back quickly crashes the app ([#169](https://github.com/nycocado/doomlings-atlas/pull/169))
- Search test expects a token's text to stay unmatched ([#174](https://github.com/nycocado/doomlings-atlas/pull/174))
- Number search, translation tokens and docs from the shared review ([#186](https://github.com/nycocado/doomlings-atlas/pull/186))
- Status bar, card language, search field and About from the app review ([#184](https://github.com/nycocado/doomlings-atlas/pull/184))

### Refactoring

- Header collapse outside the lookahead scope ([#44](https://github.com/nycocado/doomlings-atlas/pull/44))
- Compendium header and filters in their own files ([#45](https://github.com/nycocado/doomlings-atlas/pull/45))
- Side by side decided once, from the pane directive ([#47](https://github.com/nycocado/doomlings-atlas/pull/47))
- Card pane and list-detail layout, each in one module ([#65](https://github.com/nycocado/doomlings-atlas/pull/65))
- Header collapse in one module ([#74](https://github.com/nycocado/doomlings-atlas/pull/74))
- Layout changes in wide windows happen at once ([#87](https://github.com/nycocado/doomlings-atlas/pull/87))
- Comments only for what the code can't say ([#119](https://github.com/nycocado/doomlings-atlas/pull/119))
- Android studio inspection cleanup ([#120](https://github.com/nycocado/doomlings-atlas/pull/120))
- About's license list apart from reading assets ([#127](https://github.com/nycocado/doomlings-atlas/pull/127))
- Card face decided in shared ([#129](https://github.com/nycocado/doomlings-atlas/pull/129))
- Card library in shared for decoding and lookup by id ([#139](https://github.com/nycocado/doomlings-atlas/pull/139))
- Card data and translations in one file per set ([#144](https://github.com/nycocado/doomlings-atlas/pull/144))
- Card face resolved per card language ([#176](https://github.com/nycocado/doomlings-atlas/pull/176))
- Card names resolved in one place ([#175](https://github.com/nycocado/doomlings-atlas/pull/175))
- Card face hands over the card detail's blocks ([#178](https://github.com/nycocado/doomlings-atlas/pull/178))
- Compendium grid cells built in shared ([#177](https://github.com/nycocado/doomlings-atlas/pull/177))
- Card detail split and one round button ([#183](https://github.com/nycocado/doomlings-atlas/pull/183))
- Card kind lives in the card model ([#185](https://github.com/nycocado/doomlings-atlas/pull/185))
- Ink review follow-ups ([#201](https://github.com/nycocado/doomlings-atlas/pull/201))

### Documentation

- Header screenshot and update check in phase 2 ([#42](https://github.com/nycocado/doomlings-atlas/pull/42))
- Going public in phase 2 ([#43](https://github.com/nycocado/doomlings-atlas/pull/43))
- Agent rules for working with the maintainer ([#50](https://github.com/nycocado/doomlings-atlas/pull/50))
- Agent workflow, specs and documentation map ([#52](https://github.com/nycocado/doomlings-atlas/pull/52))
- Lean tickets in place of long specs ([#84](https://github.com/nycocado/doomlings-atlas/pull/84))
- Phase 2 plan: card language terms and Going Public order ([#117](https://github.com/nycocado/doomlings-atlas/pull/117))
- Card translation guidance for agents ([#125](https://github.com/nycocado/doomlings-atlas/pull/125))
- Glossary variant and style in contributing ([#128](https://github.com/nycocado/doomlings-atlas/pull/128))
- Every set phase before scoring ([#152](https://github.com/nycocado/doomlings-atlas/pull/152))
- Expansion rulesheets and glossary for their card types ([#155](https://github.com/nycocado/doomlings-atlas/pull/155))
- ADR 0012: update check amends offline without backend ([#203](https://github.com/nycocado/doomlings-atlas/pull/203))
- Release PR in the release flow

### Tooling

- Subagents on sonnet and their worktrees ignored ([#64](https://github.com/nycocado/doomlings-atlas/pull/64))
- Unit and data tests for card data and the compendium filter ([#66](https://github.com/nycocado/doomlings-atlas/pull/66))
- Ui and integration tests on robolectric ([#78](https://github.com/nycocado/doomlings-atlas/pull/78))
- Agent workflow fixes from the first pilots ([#80](https://github.com/nycocado/doomlings-atlas/pull/80))
- Codegraph index per worktree ([#123](https://github.com/nycocado/doomlings-atlas/pull/123))
- Integrity checks for the per-set card data and translations ([#153](https://github.com/nycocado/doomlings-atlas/pull/153))
- CI builds the release APK and reviews dependencies, workflows pass zizmor ([#204](https://github.com/nycocado/doomlings-atlas/pull/204))
- A vX.Y.Z tag builds the signed APK and opens a draft Release ([#207](https://github.com/nycocado/doomlings-atlas/pull/207))
- Release PR that tags the release when merged

## 0.1.0 (2026-10-21)

### Features

- Card data model ([#8](https://github.com/nycocado/doomlings-atlas/pull/8))
- Package only enabled sets into the apk ([#9](https://github.com/nycocado/doomlings-atlas/pull/9))
- Atlas theme foundation ([#10](https://github.com/nycocado/doomlings-atlas/pull/10))
- Compendium filter by kind, color and search ([#12](https://github.com/nycocado/doomlings-atlas/pull/12))
- Compendium state with hilt ([#14](https://github.com/nycocado/doomlings-atlas/pull/14))
- Compendium screen ([#16](https://github.com/nycocado/doomlings-atlas/pull/16))
- Focus ring for keyboard and switch navigation ([#17](https://github.com/nycocado/doomlings-atlas/pull/17))
- Card detail ([#18](https://github.com/nycocado/doomlings-atlas/pull/18))
- Unofficial notice on first launch and about screen ([#21](https://github.com/nycocado/doomlings-atlas/pull/21))
- Card symbols in the detail pills ([#22](https://github.com/nycocado/doomlings-atlas/pull/22))
- About button in the header and text sizes from the type scale ([#25](https://github.com/nycocado/doomlings-atlas/pull/25))
- Portuguese interface and translation glossary ([#27](https://github.com/nycocado/doomlings-atlas/pull/27))
- Tablet and foldable layout ([#28](https://github.com/nycocado/doomlings-atlas/pull/28))
- Release build signing and card types in the apk ([#38](https://github.com/nycocado/doomlings-atlas/pull/38))

### Fixes

- Seam between stickers and their shadows ([#24](https://github.com/nycocado/doomlings-atlas/pull/24))
- Header buttons and status bar contrast on dark headers ([#26](https://github.com/nycocado/doomlings-atlas/pull/26))
- Card move animation and clipping in the compendium ([#29](https://github.com/nycocado/doomlings-atlas/pull/29))
- Window background in the dark theme ([#30](https://github.com/nycocado/doomlings-atlas/pull/30))
- Text containers growing with the font size ([#31](https://github.com/nycocado/doomlings-atlas/pull/31))
- Compendium filter and missing card after process restore ([#32](https://github.com/nycocado/doomlings-atlas/pull/32))
- Header collapse and about button animations ([#35](https://github.com/nycocado/doomlings-atlas/pull/35))
- Back button above a dragged card ([#37](https://github.com/nycocado/doomlings-atlas/pull/37))
- Accessibility of touch targets, headings and choices ([#40](https://github.com/nycocado/doomlings-atlas/pull/40))

### Media

- Card images and data
- Playmats from the tabletop simulator mod
- App icon without the sunset band ([#19](https://github.com/nycocado/doomlings-atlas/pull/19))

### Refactoring

- Ready-made apis over hand-written code ([#13](https://github.com/nycocado/doomlings-atlas/pull/13))
- Remove hardcoded couplings and duplicates ([#23](https://github.com/nycocado/doomlings-atlas/pull/23))
- Rail and theme names in the app ([#36](https://github.com/nycocado/doomlings-atlas/pull/36))

### Documentation

- Readme with unofficial notice and licensing
- Agent guidelines with asset licensing and commit conventions
- Contributing guidelines, agents file narrowed to agent-only rules
- Contributing ways to help, licensing, issue and pr guides
- Roadmap, architecture decision records and adr guidelines
- Classic edition and card sources, acknowledgements
- Scoring rules in card data, supersedes adr 0003
- Official classic rules and summary
- Design system and development workflow ([#1](https://github.com/nycocado/doomlings-atlas/pull/1))
- Review fixes: adr amendment, set terminology, planned release ([#6](https://github.com/nycocado/doomlings-atlas/pull/6))
- Translation before manual scoring in the roadmap ([#11](https://github.com/nycocado/doomlings-atlas/pull/11))
- Phase 1 leftovers in not planned yet ([#34](https://github.com/nycocado/doomlings-atlas/pull/34))

### Tooling

- Gpl-3.0 license
- Kmp project scaffold and ci ([#2](https://github.com/nycocado/doomlings-atlas/pull/2))
- Gradle wrapper 9.8.1 ([#3](https://github.com/nycocado/doomlings-atlas/pull/3))
- Dependabot coverage for ktlint and gradle alerts ([#4](https://github.com/nycocado/doomlings-atlas/pull/4))
- Drop android studio template leftovers ([#5](https://github.com/nycocado/doomlings-atlas/pull/5))
- Gitattributes and editorconfig for line endings and whitespace ([#7](https://github.com/nycocado/doomlings-atlas/pull/7))
- Inspection fixes ([#15](https://github.com/nycocado/doomlings-atlas/pull/15))
- Drop unused points medals and dark symbol variants ([#20](https://github.com/nycocado/doomlings-atlas/pull/20))
- Screenshot tests in ci ([#33](https://github.com/nycocado/doomlings-atlas/pull/33))
- Changelog generation with git-cliff ([#39](https://github.com/nycocado/doomlings-atlas/pull/39))

