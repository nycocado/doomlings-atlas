# 2. Offline, Without Backend

- Status: accepted
- Date: 2026-10-15

## Context

The app is free with no revenue, so any server is an ongoing cost, and accounts or remote data bring privacy obligations.

## Decision

The app works fully offline: no backend, no accounts, no network access. Card data and images ship embedded in the app. Card recognition, when added, runs on the device.

## Alternatives Considered

- **Remote card database:** a hosted JSON would update cards without an app release, at the cost of network code and hosting.
- **Backend with accounts:** enables sync and shared history, but adds a server, login, cost and personal data handling.

## Consequences

- No running costs and no personal data leaving the device.
- Card updates require an app update.
- Features like shared game history across devices are not possible without revisiting this decision.
