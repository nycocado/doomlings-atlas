# 12. Update Check from GitHub Releases

- Status: accepted
- Date: 2026-10-31

## Context

[ADR 0002](0002-offline-without-backend.md) rules out any network access. The app will ship as an APK on GitHub Releases, outside any store, from "Going Public" on. A player would only learn about a new version by visiting the repository, and updating would mean finding the APK and installing it by hand. Telling the player about a new version needs the network, and installing it from the app needs permission to install packages.

## Decision

- The app reaches the network for one thing, updating itself from GitHub: on cold start, a release build not installed from Google Play asks GitHub's Releases API for the latest published Release, and when the player asks for it, downloads that Release's APK and hands it to the system installer.
- Everything else stays offline as ADR 0002 says: no backend, no accounts, card data and images embedded.

## Alternatives Considered

- **No check:** keeps ADR 0002 whole. Rejected: players stay on old versions without knowing a newer one exists.
- **A link to the Releases page only:** no network code, no install permission. Rejected: the app can't tell whether there is anything new, so the link is always there and says nothing.
- **A background check:** finds updates without opening the app. Rejected: it would need scheduled work and notifications, for an app that is only opened to play.

## Consequences

- The app requests `INTERNET` and `REQUEST_INSTALL_PACKAGES`.
- The check on each cold start, and each download, reveal the device's IP address to GitHub.
- A Google Play build would have to drop `REQUEST_INSTALL_PACKAGES`, since Play updates the app itself.
- Any other network access still needs a new ADR.
