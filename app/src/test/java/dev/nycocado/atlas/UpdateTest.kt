package dev.nycocado.atlas

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// The check runs off the main thread, so the banner shows a moment after launch.
private fun AtlasBehaviorTest.awaitBanner(version: String) {
    compose.waitUntil(5_000) { compose.onAllNodes(banner(version)).fetchSemanticsNodes().isNotEmpty() }
}

private fun AtlasBehaviorTest.bannerText(version: String) = compose.onNode(banner(version))

private fun banner(version: String) =
    hasText(ApplicationProvider.getApplicationContext<Context>().getString(R.string.update_available, version))

@HiltAndroidTest
class UpdateTest : AtlasBehaviorTest() {
    @Before
    fun noReleaseYet() = FakeLatestRelease.reset()

    @After
    fun clear() = FakeLatestRelease.reset()

    @Test
    fun anUpdateShowsTheBanner() {
        FakeLatestRelease.body = releaseJson("1.2.0")

        launchPastNotice()

        awaitBanner("1.2.0")
        text(R.string.update_install).assertIsDisplayed()
        text(R.string.release_notes).assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun dismissingHidesItUntilARelaunchAndTheAppAsksOnce() {
        FakeLatestRelease.body = releaseJson("1.2.0")
        val scenario = launchPastNotice()
        awaitBanner("1.2.0")

        button(R.string.update_dismiss).performClick()

        bannerText("1.2.0").assertDoesNotExist()
        scenario.recreate()
        bannerText("1.2.0").assertDoesNotExist()
        scenario.close()

        launch()

        awaitBanner("1.2.0")
        assertEquals(1, FakeLatestRelease.asked.get())
    }

    @Test
    fun noUpdateShowsNothing() {
        FakeLatestRelease.body = releaseJson("1.2.0", withApk = false)

        launchPastNotice()

        awaitAsked()
        bannerText("1.2.0").assertDoesNotExist()
        text(R.string.update_install).assertDoesNotExist()
    }

    @Test
    fun aFailedRequestShowsNothing() {
        launchPastNotice()

        awaitAsked()
        text(R.string.update_install).assertDoesNotExist()
        button(R.string.about).performClick()
        text(R.string.update_install).assertDoesNotExist()
    }

    @Test
    fun aboutShowsTheVersionTheButtonAndTheReleaseNotesEvenAfterDismissing() {
        FakeLatestRelease.body = releaseJson("1.2.0")
        val scenario = launchPastNotice()
        awaitBanner("1.2.0")
        button(R.string.update_dismiss).performClick()

        button(R.string.about).performClick()

        bannerText("1.2.0").assertIsDisplayed()
        text(R.string.update_install).assertIsDisplayed()
        assertAccessible()
        text(R.string.release_notes).performClick()
        scenario.onActivity {
            val opened = shadowOf(it).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, opened.action)
            assertEquals("https://github.com/nycocado/doomlings-atlas/releases/tag/v1.2.0", opened.dataString)
        }
    }

    private fun awaitAsked() {
        compose.waitUntil(5_000) { FakeLatestRelease.asked.get() > 0 }
        compose.waitForIdle()
    }
}

@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class UpdateWideTest : AtlasBehaviorTest() {
    @Before
    fun aNewerRelease() {
        FakeLatestRelease.reset()
        FakeLatestRelease.body = releaseJson("1.2.0")
    }

    @After
    fun clear() = FakeLatestRelease.reset()

    @Test
    fun theRailLayoutShowsTheBannerAndDismissingHidesIt() {
        launchPastNotice()

        awaitBanner("1.2.0")
        text(R.string.update_install).assertIsDisplayed()
        text(R.string.release_notes).assertIsDisplayed()
        assertAccessible()

        button(R.string.update_dismiss).performClick()

        text(R.string.update_install).assertDoesNotExist()
    }
}
