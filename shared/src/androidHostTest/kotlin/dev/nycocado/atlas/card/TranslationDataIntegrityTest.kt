package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Checks the real translation files in assets/doomlings (pt.json, es.json), for every set and card type.
// Passes with no file present; each check starts to bite with the first one.
class TranslationDataIntegrityTest {
    private val dir = File(System.getProperty("cards.dir"))

    // Card id to the text fields it has, with the names of cards.json. Only the cards that can be translated.
    private val fieldsById =
        Json.parseToJsonElement(dir.resolve("cards.json").readText()).jsonArray.associate { card ->
            val fields = card.jsonObject
            fields.getValue("id").jsonPrimitive.content to
                (
                    listOf("name", "effect", "flavor", "worldsEnd").filter {
                        fields[it].let { v ->
                            v != null &&
                                v !is JsonNull
                        }
                    }
                ).toSet()
        }

    // Same cards as the app packages: enabledSets and enabledTypes in app/build.gradle.kts.
    private val enabledClassicIds =
        Json
            .parseToJsonElement(dir.resolve("cards.json").readText())
            .jsonArray
            .map { it.jsonObject }
            .filter {
                it.getValue("set").jsonPrimitive.content == "classic" &&
                    it.getValue("type").jsonPrimitive.content in setOf("trait", "age", "catastrophe")
            }.map { it.getValue("id").jsonPrimitive.content }

    private val files =
        dir
            .listFiles { file -> file.extension == "json" && file.name != "cards.json" }
            .orEmpty()
            .toList()

    private fun fieldsOf(file: File) =
        Json.parseToJsonElement(file.readText()).jsonObject.mapValues { (_, translation) ->
            translation.jsonObject.mapValues { (_, value) -> value.jsonPrimitive.content }
        }

    @Test
    fun everyFileIsNamedAfterACardLanguage() {
        assertEquals(
            emptyList<String>(),
            files.map { it.nameWithoutExtension } - CardLanguage.entries.map { it.code }.toSet(),
        )
    }

    @Test
    fun everyFileDecodes() {
        files.forEach { decodeTranslations(it.readText()) }
    }

    @Test
    fun everyTranslatedIdExistsInCards() {
        files.forEach { file ->
            assertEquals(emptyList<String>(), (fieldsOf(file).keys - fieldsById.keys).toList(), file.name)
        }
    }

    @Test
    fun everyTranslationHasExactlyTheTextFieldsOfItsCard() {
        files.forEach { file ->
            val wrong = fieldsOf(file).filter { (id, fields) -> fields.keys != fieldsById[id] }.keys
            assertEquals(emptyList<String>(), wrong.toList(), file.name)
        }
    }

    @Test
    fun noTranslatedFieldIsEmptyOrHasStrayWhitespace() {
        files.forEach { file ->
            val bad =
                fieldsOf(file).flatMap { (id, fields) ->
                    fields
                        .filter { (_, text) ->
                            text.isEmpty() ||
                                text != text.trim()
                        }.keys
                        .map { "$id.$it" }
                }
            assertEquals(emptyList<String>(), bad, file.name)
        }
    }

    @Test
    fun everyEnabledClassicCardIsTranslatedInPortugueseAndSpanish() {
        listOf("pt", "es").forEach { code ->
            val translated =
                files
                    .find { it.nameWithoutExtension == code }
                    ?.let(::fieldsOf)
                    ?.keys
                    .orEmpty()
            assertEquals(emptyList<String>(), enabledClassicIds - translated, code)
        }
    }
}
