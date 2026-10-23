---
name: verify-ui
description: Check the running app on a separate headless emulator, without touching the maintainer's devices. Use when a UI change needs more than the screenshot tests: animations frame by frame, layout switches, wide windows, back handling.
---

# Verify UI on a Headless Emulator

The maintainer keeps their own emulator and devices connected and tests on them. This procedure starts a second, windowless emulator and talks only to it.

## Rules

- Only `emulator-5580`. Every `adb` call takes `-s emulator-5580`, every `android` call `--device emulator-5580`. The `android` CLI picks the only connected device on its own, which may be the maintainer's.
- Never `./gradlew installDebug`, `connectedAndroidTest` or `connectedDebugAndroidTest`: they install on every connected device. Build the APKs and install them with `adb -s`.
- One emulator at a time; kill it when done: `adb -s emulator-5580 emu kill`.
- A probe test (below) is never committed.

## Start the Emulator

```sh
"$ANDROID_HOME/emulator/emulator" -list-avds
"$ANDROID_HOME/emulator/emulator" -avd <phone AVD> -read-only -no-window -no-audio -no-boot-anim -no-snapshot \
  -camera-back none -camera-front none -gpu host -port 5580 &
adb -s emulator-5580 wait-for-device shell 'while [ "$(getprop sys.boot_completed)" != 1 ]; do sleep 1; done'
```

- Pick a phone AVD that is not the one the maintainer has open (theirs runs on `emulator-5554`). `-read-only` lets it share an AVD with a running instance.
- `-gpu swiftshader_indirect` segfaults at startup on the maintainer's machine; use `-gpu host`.
- `adb -s emulator-5580 shell wm density 240` gives a 720dp-wide screen, enough for the rail layout (600dp+); `wm density reset` goes back.

## Look at the App

```sh
./gradlew assembleDebug
adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5580 shell am start -n dev.nycocado.atlas/.MainActivity
android screen capture --device emulator-5580 -o <file>.png
android layout --device emulator-5580 --pretty
```

Read the PNG to see the screen; `android layout` gives the UI tree with bounds for `android input tap`.

## Probe an Animation Frame by Frame

For motion the screenshot tests can't see (header collapse, list and card panes moving), write a temporary instrumented test in a copy of the checkout, so its build changes never reach the branch:

```sh
rsync -a --exclude build --exclude .gradle ./ "$TMPDIR/atlas-probe/"
```

In the copy:

- `app/build.gradle.kts`: `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`, `androidTestImplementation` of the Compose BOM `ui-test-junit4`, androidx.test `runner` 1.7.0, `ext:junit` 1.3.0 and `espresso-core` 3.7.0, plus `debugImplementation` of `ui-test-manifest`. Older Espresso fails on the API 37 image (`InputManager.getInstance` is missing).
- The test uses `createAndroidComposeRule<MainActivity>()`, sets `mainClock.autoAdvance = false` and steps with `mainClock.advanceTimeByFrame()`, logging the `positionInRoot` of the moving parts (`Log.d("Probe", ...)`) and using `captureToImage()` where a pixel check helps. Drag in irregular steps: a part that trails by a frame stands out.
- The emulator runs the app in Portuguese (per-app language): look labels up with `getString(R.string...)` and format numbers with `Locale.ROOT`. The search field matches `hasSetTextAction()`.

Run it on the headless emulator only:

```sh
./gradlew assembleDebug assembleDebugAndroidTest
adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w dev.nycocado.atlas.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5580 logcat -d -s Probe
```

Report what the frames show; if the finding should stay checked, turn it into a Robolectric or screenshot test ([docs/testing.md](../../../docs/testing.md)).
