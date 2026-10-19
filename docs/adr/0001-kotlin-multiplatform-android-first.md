# 1. Kotlin Multiplatform, Android First

- Status: accepted; UI library amended by [0006](0006-own-design-system.md)
- Date: 2026-10-15

## Context

The maintainer reviews Kotlin and Jetpack Compose code and only has an Android device to test on. Building for iOS requires macOS and Xcode, which the maintainer does not have, and iOS already has a community companion app. Still, an iOS version should stay possible without a rewrite.

## Decision

- A Kotlin Multiplatform `shared` module holds the app logic (card data, game state, scoring) with no Android or Compose dependencies. Android is its only target for now.
- An `app` module is a native Android app: Jetpack Compose, Material 3, Hilt.

## Alternatives Considered

- **Pure Android:** the most familiar setup, but extracting the logic later for iOS would mean a migration.
- **Compose Multiplatform UI:** shares screens with iOS, but limits the UI to multiplatform libraries (no Hilt in shared UI, Material 3 behind `androidx`), with no benefit while iOS is not a target.
- **Flutter / React Native:** different language and UI toolkit, less native on Android, and outside what the maintainer can review.

## Consequences

- Android gets a fully native app, with no compromise on UI, libraries or camera APIs.
- Classes in `shared` cannot use `@Inject`; the `app` module provides them to Hilt explicitly.
- Data and logic libraries in `shared` must be multiplatform (e.g. kotlinx.serialization).
- A future iOS app can reuse `shared` with either SwiftUI or Compose Multiplatform, but needs macOS to build.
