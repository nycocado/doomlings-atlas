package dev.nycocado.atlas

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.nycocado.atlas.update.LatestRelease
import dev.nycocado.atlas.update.UpdateModule
import java.util.concurrent.atomic.AtomicInteger

// GitHub's answer for the latest Release, set by each test; null is a failed request, the default for every other UI
// test. Counts how often the app asked.
object FakeLatestRelease : LatestRelease {
    @Volatile
    var body: String? = null
    val asked = AtomicInteger()

    override suspend fun fetch(): String? {
        asked.incrementAndGet()
        return body
    }

    fun reset() {
        body = null
        asked.set(0)
    }
}

// Written after GitHub's documented response for GET /repos/{owner}/{repo}/releases/latest, trimmed to what the app
// reads. The installed version in tests is 0.0.0 (app/build.gradle.kts), so any Release is newer.
fun releaseJson(
    version: String,
    withApk: Boolean = true,
) = """
    {
      "tag_name": "v$version",
      "html_url": "https://github.com/nycocado/doomlings-atlas/releases/tag/v$version",
      "draft": false,
      "prerelease": false,
      "assets": [
        ${if (withApk) apkAsset(version) else ""}
      ]
    }
    """.trimIndent()

private fun apkAsset(version: String) =
    """
    {
      "name": "doomlings-atlas-v$version.apk",
      "browser_download_url": "https://github.com/nycocado/doomlings-atlas/releases/download/v$version/doomlings-atlas-v$version.apk",
      "digest": "sha256:${"ab".repeat(32)}"
    }
    """.trimIndent()

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UpdateModule::class])
object TestUpdateModule {
    @Provides
    fun latestRelease(): LatestRelease = FakeLatestRelease
}
