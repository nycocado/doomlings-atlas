# Security Policy

## Supported Versions

Only the latest [GitHub Release](https://github.com/nycocado/doomlings-atlas/releases/latest) gets security fixes.

## Reporting a Vulnerability

Report it privately with the **Report a vulnerability** button in the repository's [Security tab](https://github.com/nycocado/doomlings-atlas/security). Don't open a public issue, pull request or discussion about it.

Say what is affected, how to reproduce it and which version you tested.

## Scope

The app works offline, with no backend and no accounts; it only goes online to update itself from GitHub Releases ([ADR 0012](docs/adr/0012-update-check-from-github-releases.md)). What counts:

- The APK: its permissions, the update check and the install of a downloaded update.
- Its build chain: the GitHub Actions workflows, release signing and dependencies.

## What to Expect

Doomlings Atlas is kept by one maintainer in their spare time. Replies are best effort, with no promised response time. A confirmed vulnerability is fixed in a new Release.
