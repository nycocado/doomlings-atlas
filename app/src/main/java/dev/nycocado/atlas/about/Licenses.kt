package dev.nycocado.atlas.about

import com.mikepenz.aboutlibraries.entity.Library

data class LicenseEntry(
    val id: String,
    val name: String,
    val license: String,
    val text: String?,
)

data class Licenses(
    val app: LicenseEntry,
    val fonts: List<LicenseEntry>,
    val libraries: List<LicenseEntry>,
)

/** [fontTexts] maps each `-OFL.txt` file name to its text. */
fun licenses(
    appName: String,
    appText: String,
    fontTexts: Map<String, String>,
    libraries: List<Library>,
): Licenses {
    // OFL texts start with "Copyright ... The <font> Project Authors".
    val fonts =
        fontTexts.map { (file, text) ->
            val name =
                Regex("The (.+?) Project Authors").find(text)?.groupValues?.get(1) ?: file.removeSuffix("-OFL.txt")
            LicenseEntry(file, name, "OFL-1.1", text)
        }
    val libraryEntries =
        libraries
            .map { library ->
                LicenseEntry(
                    library.uniqueId,
                    library.name,
                    library.licenses.joinToString { it.spdxId ?: it.name },
                    library.licenses
                        .mapNotNull { it.licenseContent }
                        .joinToString("\n\n")
                        .ifEmpty { null },
                )
            }.sortedBy { it.name.lowercase() }
    return Licenses(LicenseEntry("app", appName, "GPL-3.0", appText), fonts, libraryEntries)
}
