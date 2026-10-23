# Testing

Which tests a change needs, where they live and how to check the running app. Commands and CI: [development.md](development.md).

## Kinds of Test

| Kind        | What it checks                                                         | Runs on                 | Lives in                                               |
| ----------- | ---------------------------------------------------------------------- | ----------------------- | ------------------------------------------------------ |
| Unit        | One function or class, pure logic                                      | JVM, no Android         | `shared/src/commonTest`, `app/src/test`                |
| Data        | The real `assets/doomlings/cards.json`, every set (unit tests on data) | JVM                     | `shared/src/androidHostTest` (`CardDataIntegrityTest`) |
| Integration | Real parts together: a ViewModel with saved state, Android resources   | JVM with Robolectric    | `app/src/test`                                         |
| UI          | Compose screens: what shows, taps, navigation, accessibility           | JVM with Robolectric    | `app/src/test`                                         |
| Screenshot  | How a preview looks, against a saved image                             | JVM (preview rendering) | `app/src/screenshotTest`                               |
| End to end  | The installed APK, as a player uses it                                 | Headless emulator only  | Not set up; checked by hand before a release           |

Robolectric for integration and UI tests is not set up yet; it comes with phase 2 ([ROADMAP.md](../ROADMAP.md)), following the `testing-setup` skill.

## What a Change Needs

- **Logic in `shared`** (cards, filter, rules, scoring): unit tests, written first. A scoring rule gets a test with a real pile and its expected score.
- **Card data** (`cards.json`, enabled sets or types): the data tests pass; a new kind of mistake they can't catch gets a check in `CardDataIntegrityTest`.
- **A component or screen:** a `@PreviewLightDark` preview, added to the screenshot tests, and the new images saved with `./gradlew updateDebugScreenshotTest` in the same PR.
- **Behavior on screen** (navigation, back, state after recreation, the unofficial notice): a UI test, once Robolectric is set up.
- **Motion and wide windows** the screenshots can't show: look at it on the headless emulator with the `verify-ui` skill, and describe what you saw in the PR.

Test behavior through public interfaces, not implementation details, and take expected values from an independent source (the rulesheet, a card, a worked example), never from the code under test.

## Checking the Running App

The maintainer tests on their own emulator and devices; nothing else may touch them. Agents use the `verify-ui` skill, which starts a separate headless emulator on port 5580 and installs only there. `installDebug` and `connectedAndroidTest` are off limits: they install on every connected device.
