package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Checks the real translation files in assets/doomlings (each set's pt.json, es.json), for every set and card type:
// every set with text is translated whole into every card language.
class TranslationDataIntegrityTest {
    private val cardsJson = CardAssets.cardsJson

    // Card id to the text fields it has, with the names of cards.json. Only the cards that can be translated.
    private val fieldsById =
        Json.parseToJsonElement(cardsJson).jsonArray.associate { card ->
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

    private val files = CardAssets.translationFiles

    // The set folder and file name, e.g. classic/pt.json: the name alone is the same in every set.
    private val File.label get() = relativeTo(CardAssets.dir).path

    private fun setIdsOf(file: File) =
        CardAssets.cardsByFolder[file.parentFile.name].orEmpty().map { it.getValue("id").jsonPrimitive.content }

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
            assertEquals(
                emptyList<String>(),
                (CardAssets.translationsOf(file).keys - fieldsById.keys).toList(),
                file.label,
            )
        }
    }

    @Test
    fun noFileRepeatsACard() {
        files.forEach { file ->
            assertEquals(
                emptyList<String>(),
                CardAssets
                    .keysOf(file)
                    .groupBy { it }
                    .filterValues { it.size > 1 }
                    .keys
                    .toList(),
                file.label,
            )
        }
    }

    @Test
    fun everyFileTranslatesOnlyCardsOfItsSet() {
        files.forEach { file ->
            assertEquals(
                emptyList<String>(),
                (CardAssets.translationsOf(file).keys - setIdsOf(file).toSet()).toList(),
                file.label,
            )
        }
    }

    @Test
    fun everyTranslationHasExactlyTheTextFieldsOfItsCard() {
        files.forEach { file ->
            val wrong = CardAssets.translationsOf(file).filter { (id, fields) -> fields.keys != fieldsById[id] }.keys
            assertEquals(emptyList<String>(), wrong.toList(), file.label)
        }
    }

    @Test
    fun noTranslatedFieldIsEmptyOrHasStrayWhitespace() {
        files.forEach { file ->
            val bad =
                CardAssets.translationsOf(file).flatMap { (id, fields) ->
                    fields
                        .filter { (_, text) ->
                            text.isEmpty() ||
                                text != text.trim()
                        }.keys
                        .map { "$id.$it" }
                }
            assertEquals(emptyList<String>(), bad, file.label)
        }
    }

    @Test
    fun everySetWithTextHasAFileForEveryCardLanguage() {
        val names = CardLanguage.entries.filter { it != CardLanguage.English }.map { "${it.code}.json" }
        val missing =
            CardAssets.cardsByFolder
                .filterValues { cards ->
                    cards.any { fieldsById[it.getValue("id").jsonPrimitive.content].orEmpty().isNotEmpty() }
                }.keys
                .flatMap { folder -> names.map { "$folder/$it" } }
                .filterNot { CardAssets.dir.resolve(it).isFile }
        assertEquals(emptyList<String>(), missing)
    }

    // A set is translated whole into a language or not at all. Cards with no text, like the Gene Pools, have no entry.
    @Test
    fun everyFileTranslatesEveryCardWithTextOfItsSet() {
        files.forEach { file ->
            val missing =
                setIdsOf(file).filter { fieldsById[it].orEmpty().isNotEmpty() } - CardAssets.translationsOf(file).keys
            assertEquals(emptyList<String>(), missing, file.label)
        }
    }

    // A card name cited in the text stays as printed, so the player finds the physical card.
    @Test
    fun everyCardNameCitedInTheEnglishTextStaysInEnglishInTheTranslation() {
        val englishById =
            Json
                .parseToJsonElement(cardsJson)
                .jsonArray
                .map { it.jsonObject }
                .associateBy { it.getValue("id").jsonPrimitive.content }
        val names = englishById.values.mapNotNull { it["name"]?.jsonPrimitive?.contentOrNull }.toSet()

        files.forEach { file ->
            val missing =
                CardAssets.translationsOf(file).flatMap { (id, fields) ->
                    fields.filterKeys { it == "effect" || it == "worldsEnd" }.flatMap { (field, text) ->
                        val english =
                            englishById
                                .getValue(id)
                                .getValue(field)
                                .jsonPrimitive.content
                        names.filter { english.cites(it) && !text.cites(it) }.map { "$id.$field: $it" }
                    }
                }
            assertEquals(emptyList<String>(), missing, file.label)
        }
    }

    // Singular or plural, and not inside a longer word: "Vampirismo" does not cite Vampirism.
    private fun String.cites(name: String) = Regex("(?<!\\p{L})${Regex.escape(name)}s?(?!\\p{L})").containsMatchIn(this)
}
