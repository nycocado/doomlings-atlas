package dev.nycocado.atlas.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

data class AppUpdate(
    val version: String,
    val apkUrl: String,
    val sha256: String,
    val releaseUrl: String,
)

// latestRelease is the body of GitHub's GET /repos/{owner}/{repo}/releases/latest, which skips drafts and pre-releases.
// A body that isn't a readable Release is no update, so a failed check stays silent.
fun availableUpdate(
    latestRelease: String,
    installedVersion: String,
): AppUpdate? {
    val installed = requireNotNull(versionOf(installedVersion)) { "Not MAJOR.MINOR.PATCH: $installedVersion" }
    val release =
        try {
            json.decodeFromString<Release>(latestRelease)
        } catch (_: SerializationException) {
            return null
        }
    val version = release.tagName.removePrefix("v")
    if ((versionOf(version) ?: return null) <= installed) return null
    val apk = release.assets.find { it.name == "doomlings-atlas-${release.tagName}.apk" } ?: return null
    val sha256 = apk.digest?.takeIf { it.startsWith("sha256:") }?.removePrefix("sha256:") ?: return null
    return AppUpdate(version, apk.browserDownloadUrl, sha256, release.htmlUrl)
}

// KotlinVersion is the standard library's comparable MAJOR.MINOR.PATCH. It caps each part at 255, which our own tags
// never reach (the version code caps the minor and patch at 99, app/build.gradle.kts), so a part above it is unreadable.
private fun versionOf(text: String): KotlinVersion? {
    val match = Regex("""(\d+)\.(\d+)\.(\d+)""").matchEntire(text) ?: return null
    val (major, minor, patch) =
        match.destructured.toList().map { part -> part.toIntOrNull()?.takeIf { it <= 255 } ?: return null }
    return KotlinVersion(major, minor, patch)
}

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private class Release(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
    val assets: List<Asset>,
)

@Serializable
private class Asset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val digest: String?,
)
