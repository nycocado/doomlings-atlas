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

## Running Tests

All of them run on the JVM; none needs an emulator or device.

| To run                         | Command                                                     |
| ------------------------------ | ----------------------------------------------------------- |
| Everything CI runs             | `./gradlew spotlessApply check validateDebugScreenshotTest` |
| Unit, data, integration and UI | `./gradlew test` (`:app:testDebugUnitTest` for `app` only)  |
| One UI test class              | `./gradlew :app:testDebugUnitTest --tests '*NoticeTest'`    |
| Screenshot tests               | `./gradlew validateDebugScreenshotTest`                     |
| Save new screenshot images     | `./gradlew updateDebugScreenshotTest`                       |

## UI Tests on Robolectric

Robolectric is set up in `app`. The UI tests in `app/src/test` run the real app: the real `MainActivity` with Hilt (`@HiltAndroidTest`, `HiltTestApplication`) and the enabled sets' card data, packaged into the assets. They drive it like a player with Compose's test APIs and check what shows.

- Extend `AtlasBehaviorTest`: it sets the phone-sized window, Hilt, the Compose rule and Compose's accessibility checks. Put `@HiltAndroidTest` on the test class itself, as Hilt requires.
- Look text up by its string resource (`text(R.string.about)`, `button(R.string.back)`), never hard-coded; take card names from `assets/doomlings/cards.json`.
- The notice's "seen" flag lives in the `atlas` preferences: `launch()` starts as on first launch, `launchPastNotice()` after accepting it.
- Robolectric runs on Android 16 (SDK 36) with native graphics, set in `app/src/test/resources/robolectric.properties`: Espresso, which Compose tests idle through, can't inject input on Android 17 yet. Change `sdk` there once it can.
- Compose's built-in accessibility checks are on in every test, but on Robolectric they report NOT_RUN: they only evaluate Android views, not Compose content ("Composable content is not evaluated in this environment"). Don't count on them.
- `assertAccessible()` is the accessibility sweep that does work here: call it on every screen a test reaches (compendium, card detail on a phone and beside the list, About, the notice). It fails if any node with a click action has neither text nor a content description, skipping nodes scrolled out of the window. It doesn't check touch size (Compose widens every touch area to 48dp, which is what [docs/design.md](design.md#accessibility) asks for) nor contrast (the design tokens and a visual review cover that).
- Wide windows: `@Config(qualifiers = "w1280dp-h800dp-xhdpi")` on the test class (see `WideWindowTest`). Let the screen settle (`compose.waitForIdle()`) before pressing back, and advance `compose.mainClock` after it while the card's pane slides out.

## What a Change Needs

- **Logic in `shared`** (cards, filter, rules, scoring): unit tests, written first. A scoring rule gets a test with a real pile and its expected score.
- **Card data** (`cards.json`, enabled sets or types): the data tests pass; a new kind of mistake they can't catch gets a check in `CardDataIntegrityTest`.
- **A component or screen:** a `@PreviewLightDark` preview, added to the screenshot tests, and the new images saved with `./gradlew updateDebugScreenshotTest` in the same PR.
- **Behavior on screen** (navigation, back, state after recreation, the unofficial notice): a UI test on Robolectric.
- **Motion and wide windows** the screenshots can't show: look at it on the headless emulator with the `verify-ui` skill, and describe what you saw in the PR.

Test behavior through public interfaces, not implementation details, and take expected values from an independent source (the rulesheet, a card, a worked example), never from the code under test.

## Checking the Running App

The maintainer tests on their own emulator and devices; nothing else may touch them. Agents use the `verify-ui` skill, which starts a separate headless emulator on port 5580 and installs only there. `installDebug` and `connectedAndroidTest` are off limits: they install on every connected device.
