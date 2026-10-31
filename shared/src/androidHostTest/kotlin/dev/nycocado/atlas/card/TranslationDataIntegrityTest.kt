package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
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

    private val englishCards = Json.parseToJsonElement(cardsJson).jsonArray.map { it.jsonObject }

    // Card id to the text fields a translation has. Kinds, Gene Pool and Value are not text: they stay in cards.json.
    private val fieldsById =
        englishCards.associate { fields ->
            fields.getValue("id").jsonPrimitive.content to
                buildSet {
                    if (fields["name"] != null) add("name")
                    if (effectsOf(fields).isNotEmpty()) add("effects")
                    if (fields["flavor"].let { it != null && it !is JsonNull }) add("flavor")
                }
        }

    private val englishEffectsById =
        englishCards.associate { card ->
            card.getValue("id").jsonPrimitive.content to
                effectsOf(card).map { it.getValue("text").jsonPrimitive.content }
        }

    private fun effectsOf(card: JsonObject) = card["effects"]?.jsonArray.orEmpty().map { it.jsonObject }

    // Each translated effect beside the English effect it translates, as "card-id.effects[i]" to the pair.
    private fun effectPairs(file: File) =
        CardAssets.translationsOf(file).flatMap { (id, fields) ->
            fields["effects"].orEmpty().zip(englishEffectsById[id].orEmpty()).mapIndexed { index, (text, english) ->
                "$id.effects[$index]" to (text to english)
            }
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
                        .filter { (_, texts) -> texts.any { it.isEmpty() || it != it.trim() } }
                        .keys
                        .map { "$id.$it" }
                }
            assertEquals(emptyList<String>(), bad, file.label)
        }
    }

    // A translation is a list of texts in the order of its card's effects, so it has as many as the card.
    @Test
    fun everyTranslationHasAsManyEffectsAsItsCard() {
        files.forEach { file ->
            val wrong =
                CardAssets
                    .translationsOf(file)
                    .filter { (id, fields) ->
                        fields["effects"].orEmpty().size != englishEffectsById[id].orEmpty().size
                    }.keys
            assertEquals(emptyList<String>(), wrong.toList(), file.label)
        }
    }

    // A token is an icon, the same in every language: each translated effect has the tokens of its English effect, in
    // their order. A paragraph moved to the wrong effect shows up here or in the count.
    @Test
    fun everyTranslatedEffectHasTheTokensOfItsEnglishEffect() {
        val token = Regex("""\{[^}]*}""")

        fun tokens(text: String) = token.findAll(text).map { it.value }.toList()
        files.forEach { file ->
            val wrong =
                effectPairs(file)
                    .filter { (_, pair) -> tokens(pair.first) != tokens(pair.second) }
                    .map { (where, _) -> where }
            assertEquals(emptyList<String>(), wrong, file.label)
        }
    }

    // An ink repeats what the words say, so each translated effect has the inks of its English effect, in their order.
    @Test
    fun everyTranslatedEffectHasTheInksOfItsEnglishEffect() {
        files.forEach { file ->
            val wrong =
                effectPairs(file)
                    .filter { (_, pair) -> inks(pair.first) != inks(pair.second) }
                    .map { (where, _) -> where }
            assertEquals(emptyList<String>(), wrong, file.label)
        }
    }

    // Paragraphs follow the printed card, notes included.
    @Test
    fun everyTranslatedEffectHasTheParagraphsOfItsEnglishEffect() {
        files.forEach { file ->
            val wrong =
                effectPairs(file)
                    .filter { (_, pair) -> pair.first.split("\n\n").size != pair.second.split("\n\n").size }
                    .map { (where, _) -> where }
            assertEquals(emptyList<String>(), wrong, file.label)
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
        val names = englishCards.mapNotNull { it["name"]?.jsonPrimitive?.contentOrNull }.toSet()

        files.forEach { file ->
            val missing =
                effectPairs(file).flatMap { (where, pair) ->
                    val (text, english) = pair
                    names.filter { english.cites(it) && !text.cites(it) }.map { "$where: $it" }
                }
            assertEquals(emptyList<String>(), missing, file.label)
        }
    }

    // Singular or plural, and not inside a longer word: "Vampirismo" does not cite Vampirism.
    private fun String.cites(name: String) = Regex("(?<!\\p{L})${Regex.escape(name)}s?(?!\\p{L})").containsMatchIn(this)
}
