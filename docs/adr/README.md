# Architecture Decision Records

Each file records one architecture decision: its context, the alternatives considered and why this one won.

## When to Write One

Write an ADR when a decision meets at least two of these:

- Hard to reverse: changing it later means rewriting large parts.
- Shapes the project structure: modules, data flow or core dependencies.
- Had real alternatives, discussed and rejected.
- A contributor would ask "why is it like this?" and the code does not answer it.

Not an ADR:

- Scope and order of work: [ROADMAP.md](../../ROADMAP.md).
- Project rules (licensing, commits, assets): [CONTRIBUTING.md](../../CONTRIBUTING.md).
- Small libraries, naming, UI and code details: the code, the PR description or the commit message.

## Format

- File name: `NNNN-kebab-case-title.md`, with the next free four-digit number. Numbers are never reused or changed.
- Content: copy [template.md](template.md) and fill every section. No extra sections.
- Write in English, short and direct.

## Lifecycle

- **proposed:** under discussion, may still change.
- **accepted:** in effect. From here on only the Status line may change.
- **superseded:** replaced by a newer ADR. Never edit or delete the old one; set its status to `superseded by [NNNN](NNNN-title.md)` and write a new ADR explaining the change.
- **deprecated:** no longer applies and nothing replaces it.

## Index

| ADR                                                | Title                                        | Status     |
| -------------------------------------------------- | -------------------------------------------- | ---------- |
| [0001](0001-kotlin-multiplatform-android-first.md) | Kotlin Multiplatform, Android First          | accepted   |
| [0002](0002-offline-without-backend.md)            | Offline, Without Backend                     | accepted   |
| [0003](0003-card-data-json-effects-kotlin.md)      | Card Data in JSON, Scoring Effects in Kotlin | superseded |
| [0004](0004-compendium-before-scoring.md)          | Compendium Before Scoring                    | accepted   |
| [0005](0005-scoring-rules-in-card-data.md)         | Scoring Rules in Card Data                   | accepted   |
