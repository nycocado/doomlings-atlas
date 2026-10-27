package dev.nycocado.atlas.about

import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import org.junit.Assert.assertEquals
import org.junit.Test

class LicensesTest {
    @Test
    fun fontIsNamedFromItsLicenseText() {
        val text = "Copyright 2020 The Fredoka Project Authors\n\nSIL Open Font License"

        val fonts = licenses("Atlas", "GPL text", mapOf("fredoka-OFL.txt" to text), emptyList()).fonts

        assertEquals(listOf(LicenseEntry("fredoka-OFL.txt", "Fredoka", "OFL-1.1", text)), fonts)
    }

    @Test
    fun fontWithoutAuthorsLineIsNamedByItsFile() {
        val fonts =
            licenses(
                "Atlas",
                "GPL text",
                mapOf("fredoka-OFL.txt" to "SIL Open Font License"),
                emptyList(),
            ).fonts

        assertEquals(listOf("fredoka"), fonts.map { it.name })
    }

    @Test
    fun libraryIsNamedByItsSpdxId() {
        val library =
            library(
                "androidx.core:core",
                "Core",
                License("Apache License 2.0", null, spdxId = "Apache-2.0", licenseContent = "Apache text", hash = "a"),
            )

        val libraries = licenses("Atlas", "GPL text", emptyMap(), listOf(library)).libraries

        assertEquals(listOf(LicenseEntry("androidx.core:core", "Core", "Apache-2.0", "Apache text")), libraries)
    }

    @Test
    fun libraryWithoutSpdxIdIsNamedByItsLicenseName() {
        val library = library("com.example:lib", "Lib", License("Custom License", null, hash = "c"))

        val libraries = licenses("Atlas", "GPL text", emptyMap(), listOf(library)).libraries

        assertEquals(listOf(LicenseEntry("com.example:lib", "Lib", "Custom License", null)), libraries)
    }

    @Test
    fun librariesAreSortedByNameIgnoringCase() {
        val license = License("MIT License", null, spdxId = "MIT", hash = "m")

        val libraries =
            licenses(
                "Atlas",
                "GPL text",
                emptyMap(),
                listOf(library("c", "okio", license), library("a", "Kotlin", license), library("b", "Hilt", license)),
            ).libraries

        assertEquals(listOf("Hilt", "Kotlin", "okio"), libraries.map { it.name })
    }

    private fun library(
        uniqueId: String,
        name: String,
        license: License,
    ) = Library(uniqueId, null, name, null, null, emptyList(), null, null, setOf(license))
}
