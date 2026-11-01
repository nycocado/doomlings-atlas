package dev.nycocado.atlas.update

import java.io.File
import java.security.MessageDigest

// sha256 is the hex digest GitHub gives for the Release asset (AppUpdate). A file that doesn't match is deleted, so a
// corrupt or tampered APK never reaches the installer.
fun keepIfIntact(
    apk: File,
    sha256: String,
): Boolean {
    val digest = MessageDigest.getInstance("SHA-256")
    apk.forEachBlock { buffer, read -> digest.update(buffer, 0, read) }
    val intact = digest.digest().toHexString().equals(sha256, ignoreCase = true)
    if (!intact) apk.delete()
    return intact
}
