package dev.nycocado.atlas.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DownloadedApkTest {
    @get:Rule
    val folder = TemporaryFolder()

    // The SHA-256 of "abc", from FIPS 180-2's worked example.
    private val abcSha256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"

    @Test
    fun aFileMatchingTheDigestIsAcceptedAndKept() {
        val apk = folder.newFile("doomlings-atlas-v1.2.0.apk").apply { writeText("abc") }

        assertTrue(keepIfIntact(apk, abcSha256))
        assertTrue(apk.exists())
    }

    @Test
    fun theDigestMatchesInUppercaseToo() {
        val apk = folder.newFile("doomlings-atlas-v1.2.0.apk").apply { writeText("abc") }

        assertTrue(keepIfIntact(apk, abcSha256.uppercase()))
    }

    @Test
    fun aFileDifferingFromTheDigestIsRejectedAndDeleted() {
        val apk = folder.newFile("doomlings-atlas-v1.2.0.apk").apply { writeText("abd") }

        assertFalse(keepIfIntact(apk, abcSha256))
        assertFalse(apk.exists())
    }
}
