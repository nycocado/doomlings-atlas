# Triage Labels

The engineering skills speak in terms of five triage roles. This repo uses their default label strings.

| Role              | Label             | Meaning                                                                   |
| ----------------- | ----------------- | ------------------------------------------------------------------------- |
| `needs-triage`    | `needs-triage`    | The maintainer needs to evaluate this issue                               |
| `needs-info`      | `needs-info`      | Waiting on the reporter for more information                              |
| `ready-for-agent` | `ready-for-agent` | Approved ticket or spec, ready for an agent. Only the maintainer sets it. |
| `ready-for-human` | `ready-for-human` | Needs a human to implement it                                             |
| `wontfix`         | `wontfix`         | Will not be done                                                          |

A skill that asks for the agent-ready label (for example `to-spec`) gets `needs-triage` instead: approving a ticket or spec is the maintainer's gate (AGENTS.md), and a hook blocks agents from applying `ready-for-agent`. The maintainer applies it on GitHub, or by running `gh issue edit` with `!` in the prompt; an agent can't, even when asked to.
