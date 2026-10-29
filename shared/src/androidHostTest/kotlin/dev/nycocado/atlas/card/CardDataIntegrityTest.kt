package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

// Checks the real card data of every set in assets/doomlings. Decoding is strict, so it also checks required fields.
class CardDataIntegrityTest {
    private val dir = CardAssets.dir
    private val json = CardAssets.cardsJson
    private val cards = decodeCards(json)

    // Every string in the data, any field and set, as card id to text.
    private val texts =
        Json.parseToJsonElement(json).jsonArray.flatMap { card ->
            val fields = card.jsonObject
            val id = fields.getValue("id").jsonPrimitive.content
            fields.values
                .flatMap { it as? JsonArray ?: listOf(it) }
                .filter { it is JsonPrimitive && it.isString }
                .map { id to (it as JsonPrimitive).content }
        }

    @Test
    fun idsAreUnique() {
        assertEquals(
            emptyList(),
            cards
                .groupBy { it.id }
                .filterValues { it.size > 1 }
                .keys
                .toList(),
        )
    }

    @Test
    fun everyCardHasItsImage() {
        assertEquals(emptyList(), cards.map { it.image }.filterNot { dir.resolve(it).isFile })
    }

    // Counts from the official rulesheet (docs/rules.md), an independent source for the data.
    @Test
    fun classicHasTheRulesheetAgesAndCatastrophes() {
        val classic = cards.filter { it.set == "classic" }
        assertEquals(25, classic.filterIsInstance<Age>().size, "ages")
        assertEquals(15, classic.filterIsInstance<Catastrophe>().size, "catastrophes")
        assertEquals(1, classic.filterIsInstance<BirthOfLife>().size, "Birth of Life")
    }

    // A card type without a face would open as an empty card detail.
    @Test
    fun everyEnabledCardHasAFace() {
        val sets = System.getProperty("enabled.sets")!!.split(",")
        val types = System.getProperty("enabled.types")!!.split(",")
        val enabled =
            Json
                .parseToJsonElement(json)
                .jsonArray
                .map { it.jsonObject }
                .filter {
                    it.getValue("set").jsonPrimitive.content in sets &&
                        it.getValue("type").jsonPrimitive.content in types
                }.map { it.getValue("id").jsonPrimitive.content }
                .toSet()
        assertEquals(
            emptyList(),
            cards.filter { it.id in enabled && (it as? NamedCard)?.face() == null }.map { it.id },
        )
    }

    @Test
    fun noTextStartsOrEndsWithWhitespace() {
        assertEquals(emptyList(), texts.filter { (_, text) -> text != text.trim() })
    }

    // An effectless trait has a null effect, never an empty one.
    @Test
    fun noTextIsEmpty() {
        assertEquals(emptyList(), texts.filter { (_, text) -> text.isEmpty() })
    }

    // The data uses plain ' " and -, which players type, so search matches them.
    @Test
    fun noTypographicQuotesMinusSignsOrDashes() {
        val typographic = "\u2018\u2019\u201C\u201D\u2212\u2013" // ‘ ’ “ ” − –
        assertEquals(
            emptyList(),
            texts.filter { (_, text) -> text.any { it in typographic } },
        )
    }

    @Test
    fun noTraitRepeatsAColor() {
        assertEquals(
            emptyList(),
            cards.filterIsInstance<Trait>().filter { it.colors.size != it.colors.toSet().size }.map { it.id },
        )
    }

    // Each set keeps its images in its own folder (assets/doomlings/NOTICE.md).
    @Test
    fun everyImageIsInsideItsSetFolder() {
        assertEquals(
            emptyList(),
            cards.filterNot { dir.resolve(it.image).normalize().startsWith(dir.resolve(it.set)) }.map { it.id },
        )
    }
}
