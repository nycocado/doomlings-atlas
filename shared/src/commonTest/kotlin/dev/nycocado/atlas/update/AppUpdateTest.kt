package dev.nycocado.atlas.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppUpdateTest {
    @Test
    fun newerReleaseIsAnUpdate() {
        assertEquals(
            AppUpdate(
                version = "1.3.0",
                apkUrl = "$RELEASES/download/v1.3.0/doomlings-atlas-v1.3.0.apk",
                sha256 = DIGEST,
                releaseUrl = "$RELEASES/tag/v1.3.0",
            ),
            availableUpdate(release("v1.3.0"), installedVersion = "1.2.4"),
        )
    }

    @Test
    fun sameVersionIsNoUpdate() {
        assertNull(availableUpdate(release("v1.2.4"), installedVersion = "1.2.4"))
    }

    @Test
    fun olderReleaseIsNoUpdate() {
        assertNull(availableUpdate(release("v1.2.3"), installedVersion = "1.2.4"))
        assertNull(availableUpdate(release("v0.9.9"), installedVersion = "1.0.0"))
    }

    @Test
    fun versionsCompareByNumberNotText() {
        assertEquals("0.10.0", availableUpdate(release("v0.10.0"), installedVersion = "0.9.0")?.version)
        assertNull(availableUpdate(release("v0.9.0"), installedVersion = "0.10.0"))
    }

    @Test
    fun releaseWithoutTheApkIsNoUpdate() {
        assertNull(availableUpdate(release("v1.3.0", assets = ""), installedVersion = "1.2.4"))
        assertNull(
            availableUpdate(
                release("v1.3.0", assets = apkAsset("v1.3.0", name = "doomlings-atlas-v1.3.0.aab")),
                installedVersion = "1.2.4",
            ),
        )
    }

    @Test
    fun apkWithoutDigestIsNoUpdate() {
        assertNull(
            availableUpdate(release("v1.3.0", assets = apkAsset("v1.3.0", digest = null)), installedVersion = "1.2.4"),
        )
        assertNull(
            availableUpdate(
                release("v1.3.0", assets = apkAsset("v1.3.0", digest = "sha512:abc")),
                installedVersion = "1.2.4",
            ),
        )
    }

    @Test
    fun unreadableReleaseIsNoUpdate() {
        assertNull(availableUpdate("<html>rate limited</html>", installedVersion = "1.2.4"))
        assertNull(availableUpdate("""{"message": "Not Found"}""", installedVersion = "1.2.4"))
        assertNull(availableUpdate(release("v1.3"), installedVersion = "1.2.4"))
        assertNull(availableUpdate(release("nightly"), installedVersion = "1.2.4"))
    }

    private companion object {
        const val RELEASES = "https://github.com/nycocado/doomlings-atlas/releases"
        const val DIGEST = "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4"
    }
}

// Shaped after GET /repos/{owner}/{repo}/releases/latest in GitHub's REST API docs, trimmed of the fields nothing reads
// but keeping some it ignores, as the real body has many more.
private fun release(
    tag: String,
    assets: String = apkAsset(tag),
): String =
    """
    {
      "url": "https://api.github.com/repos/nycocado/doomlings-atlas/releases/1",
      "html_url": "https://github.com/nycocado/doomlings-atlas/releases/tag/$tag",
      "id": 1,
      "tag_name": "$tag",
      "target_commitish": "main",
      "name": "$tag",
      "body": "Notes",
      "draft": false,
      "prerelease": false,
      "created_at": "2026-10-30T12:00:00Z",
      "published_at": "2026-10-30T12:05:00Z",
      "author": {"login": "nycocado", "id": 1},
      "assets": [$assets]
    }
    """

private fun apkAsset(
    tag: String,
    name: String = "doomlings-atlas-$tag.apk",
    digest: String? = "sha256:8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
): String =
    """
    {
      "url": "https://api.github.com/repos/nycocado/doomlings-atlas/releases/assets/1",
      "browser_download_url": "https://github.com/nycocado/doomlings-atlas/releases/download/$tag/$name",
      "id": 1,
      "name": "$name",
      "label": "",
      "state": "uploaded",
      "content_type": "application/vnd.android.package-archive",
      "size": 12345678,
      "digest": ${digest?.let { "\"$it\"" } ?: "null"},
      "download_count": 0,
      "created_at": "2026-10-30T12:00:00Z",
      "updated_at": "2026-10-30T12:00:00Z"
    }
    """
