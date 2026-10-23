# Domain Docs

How the engineering skills read this repo's domain documentation. It is a single-context repo.

## Before Exploring, Read These

- **[`GLOSSARY.md`](../../GLOSSARY.md)**: the words for game and app concepts.
- **[`docs/rules.md`](../rules.md)**: the game rules (Classic), for anything about cards, games or scoring.
- **[`docs/adr/`](../adr)**: the ADRs that touch the area you're about to work in.

Translated words are not domain terms: each language's word list is in [`docs/glossary/`](../glossary), checked by `./gradlew check`.

## Use the Glossary's Vocabulary

When your output names a domain concept (an issue title, a test name, a proposal), use the term as `GLOSSARY.md` defines it, not a synonym it rules out. If the concept isn't there yet, either the project doesn't use it (reconsider) or it is a real gap: add it to `GLOSSARY.md` in the same change.

## Flag ADR Conflicts

If your output contradicts an accepted ADR, say so explicitly instead of overriding it silently, e.g. "Contradicts ADR 0002 (offline, without backend), but worth reopening because…". Changing an ADR follows [docs/adr/README.md](../adr/README.md).
