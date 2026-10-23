# Issue Tracker: GitHub

Issues and specs for this repo live as GitHub issues. Use the `gh` CLI for all operations; it infers the repo from the clone.

## Project Conventions

- An issue is the spec of its work ([CONTRIBUTING.md](../../CONTRIBUTING.md#issues-and-specs)). A spec an agent writes gets `needs-triage`; only the maintainer applies `ready-for-agent`, and agents only pick up issues that have it.
- Each [ROADMAP.md](../../ROADMAP.md) phase is a milestone: add the phase's issues to it (`--milestone "2. Card Text Translation"`).
- Besides the triage labels ([triage-labels.md](triage-labels.md)), issues use the topic labels `bug`, `card-data`, `translation`, `scoring`, `accessibility` and `dependencies`.

## Operations

- **Create an issue**: `gh issue create --title "..." --body "..."`. Use a heredoc for multi-line bodies.
- **Read an issue**: `gh issue view <number> --json number,title,body,labels,comments`.
- **List issues**: `gh issue list --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'` with `--label` and `--state` filters.
- **Make an issue a sub-issue of a parent**: `gh issue create --parent <parent> ...`, or `gh issue edit <parent> --add-sub-issue <child>` afterwards.
- **Blocking**: GitHub's native issue dependencies. `gh api --method POST repos/<owner>/<repo>/issues/<child>/dependencies/blocked_by -F issue_id=<blocker-db-id>`, where `<blocker-db-id>` is `gh api repos/<owner>/<repo>/issues/<n> --jq .id` (not the `#number`). A ticket is unblocked when every blocker is closed.
- **Comment on an issue**: `gh issue comment <number> --body "..."`.
- **Apply / remove labels**: `gh issue edit <number> --add-label "..."` / `--remove-label "..."`.
- **Close**: `gh issue close <number> --comment "..."`.

## Pull Requests as a Triage Surface

**PRs as a request surface: no.** External PRs are reviewed as PRs, not triaged as feature requests.

GitHub shares one number space across issues and PRs, so a bare `#42` may be either: resolve with `gh pr view 42` and fall back to `gh issue view 42`.

## When a Skill Says "Publish to the Issue Tracker"

Create a GitHub issue, with `needs-triage`.

## When a Skill Says "Fetch the Relevant Ticket"

Read it as in **Read an issue** above.
