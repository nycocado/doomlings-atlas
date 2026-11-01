package dev.nycocado.atlas.update

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.nycocado.atlas.FakeLatestRelease
import dev.nycocado.atlas.releaseJson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

// Which installs ask GitHub, decided by the installer source Android records for the app.
@RunWith(RobolectricTestRunner::class)
class UpdateCheckTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun aNewerRelease() {
        FakeLatestRelease.reset()
        FakeLatestRelease.body = releaseJson("1.2.0")
    }

    @After
    fun clear() = FakeLatestRelease.reset()

    @Test
    fun anInstallFromGooglePlayNeverAsks() {
        installedBy("com.android.vending")

        assertNull(runBlocking { UpdateCheck(context, FakeLatestRelease).check() })
        assertEquals(0, FakeLatestRelease.asked.get())
    }

    @Test
    fun anInstallFromAFileAsks() {
        installedBy("com.google.android.packageinstaller")

        assertNotNull(runBlocking { UpdateCheck(context, FakeLatestRelease).check() })
        assertEquals(1, FakeLatestRelease.asked.get())
    }

    @Test
    fun anInstallWithNoInstallerRecordedAsks() {
        assertNotNull(runBlocking { UpdateCheck(context, FakeLatestRelease).check() })
        assertEquals(1, FakeLatestRelease.asked.get())
    }

    private fun installedBy(installer: String) =
        shadowOf(context.packageManager).setInstallSourceInfo(context.packageName, installer, installer)
}
