package dev.nycocado.atlas.card

import dev.nycocado.atlas.compendium.CompendiumFilter
import dev.nycocado.atlas.compendium.inSetsOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Checks the real card data of every set in assets/doomlings. Decoding is strict, so it also checks required fields.
class CardDataIntegrityTest {
    private val dir = CardAssets.dir
    private val json = CardAssets.cardsJson
    private val cards = decodeCards(json)

    // Every string in the data, any field and set, as card id to text. The fields of an effect count too.
    private val texts =
        Json.parseToJsonElement(json).jsonArray.flatMap { card ->
            val fields = card.jsonObject
            val id = fields.getValue("id").jsonPrimitive.content
            fields.values
                .flatMap { it as? JsonArray ?: listOf(it) }
                .flatMap { if (it is JsonObject) it.values else listOf(it) }
                .filter { it is JsonPrimitive && it.isString }
                .map { id to (it as JsonPrimitive).content }
        }

    private val effects = cards.filterIsInstance<NamedCard>().flatMap { card -> card.effects.map { card to it } }

    // The card detail draws Curious and Exotic as one printed tag with the card's coral, fish or shell.
    @Test
    fun everyCuriousOrExoticTraitHasOneCoralFishOrShell() {
        val aquatic = setOf(TraitIcon.Coral, TraitIcon.Fish, TraitIcon.Shell)
        assertEquals(
            emptyList(),
            cards
                .filterIsInstance<Trait>()
                .filter {
                    (TraitIcon.Curious in it.icons || TraitIcon.Exotic in it.icons) &&
                        it.icons.count(aquatic::contains) != 1
                }.map { it.id },
        )
    }

    @Test
    fun everyEffectKindIsAllowedOnItsCardType() {
        assertEquals(
            emptyList(),
            effects.filter { (card, effect) -> effect.kind?.allowedOn(card) == false }.map { (card, effect) ->
                "${card.id}: ${effect.kind}"
            },
        )
    }

    @Test
    fun everyTokenIsAKnownIcon() {
        val known =
            setOf(
                "action",
                "attachment",
                "drop-of-life",
                "worlds-end",
                "compass",
                "coral",
                "fish",
                "shell",
                "mythling",
                "dinoling",
                "techling",
                "deepling",
                "moonling",
                "glitterling",
            )
        val token = Regex("""\{([^}]*)}""")
        assertEquals(
            emptyList(),
            (
                effects.map { (card, effect) ->
                    card.id to effect.text
                } + CardAssets.translatedTexts
            ).flatMap { (where, text) ->
                token
                    .findAll(text)
                    .map { it.groupValues[1] }
                    .filter { it !in known }
                    .map { "$where: {$it}" }
            },
        )
    }

    // Each case is proved on a broken sample in InkMarksTest.
    @Test
    fun everyInkMarkIsWellFormed() {
        val texts = effects.map { (card, effect) -> card.id to effect.text } + CardAssets.translatedTexts
        assertEquals(
            emptyList(),
            texts.flatMap { (where, text) -> inkMarkProblems(text).map { "$where: $it" } },
        )
    }

    @Test
    fun noEffectTextHasAGenePoolLineOrALabelItShouldHaveAsAKind() {
        // A line of its own: "All players receive: -1 Gene Pool." is rules text.
        val genePoolLine = Regex("""(^|\n)[+-]\d+ Gene Pool\.?(\n|$)""")
        val labels =
            Regex(
                """^(Awesome Power|Pocket Objective|Rush|Mutate|Echo|Cloak|Adapt|Exchange|Buy|Collect|Bargain|""" +
                    """At World's End):""",
            )

        // "At World's End:" stays in the text of a Play When section or a label whose condition it is (Sneaky).
        fun Effect.hasLabel() =
            labels.find(text)?.let {
                it.groupValues[1] != "At World's End" ||
                    (kind != EffectKind.PlayWhen && kind?.labelOf == null)
            } ==
                true
        assertEquals(
            emptyList(),
            effects
                .filter { (_, effect) -> genePoolLine.containsMatchIn(effect.text) || effect.hasLabel() }
                .map { (card, effect) -> "${card.id}: ${effect.text.take(40)}" },
        )
    }

    @Test
    fun noEffectTextIsEmpty() {
        assertEquals(emptyList(), effects.filter { (_, effect) -> effect.text.isBlank() }.map { (card, _) -> card.id })
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

    // The app packages card images as WebP (assets/doomlings/NOTICE.md). The JVM has no WebP reader, so this checks
    // the file header.
    @Test
    fun everyCardImageIsWebP() {
        assertEquals(
            emptyList(),
            cards
                .map { it.image }
                .filterNot { image ->
                    val header = dir.resolve(image).inputStream().use { it.readNBytes(12) }
                    header.size == 12 &&
                        header.copyOfRange(0, 4).decodeToString() == "RIFF" &&
                        header.copyOfRange(8, 12).decodeToString() == "WEBP"
                },
        )
    }

    // Card images share one size (about 541×757, assets/doomlings/NOTICE.md), so the cards line up in the app. Reads
    // the width from the WebP header: the JVM has no WebP reader and no dependency is worth adding for one number.
    // Every card image is extended WebP (VP8X), whose canvas width is 24 bits at byte 24, stored minus one.
    @Test
    fun everyCardImageHasTheStandardWidth() {
        fun widthOf(file: File): Int {
            val b = file.inputStream().use { it.readNBytes(30) }.map { it.toInt() and 0xFF }
            check(b.subList(12, 16).map { it.toChar() }.joinToString("") == "VP8X") { "$file is not extended WebP" }
            return (b[24] or (b[25] shl 8) or (b[26] shl 16)) + 1
        }
        assertEquals(
            emptyList(),
            cards.map { it.image to widthOf(dir.resolve(it.image)) }.filter { (_, width) -> width !in 540..541 },
        )
    }

    // Counts from the official rulesheet (docs/rules/classic.md), an independent source for the data.
    @Test
    fun classicHasTheRulesheetAgesAndCatastrophes() {
        val classic = cards.filter { it.set == "classic" }
        assertEquals(25, classic.filterIsInstance<Age>().size, "ages")
        assertEquals(15, classic.filterIsInstance<Catastrophe>().size, "catastrophes")
        assertEquals(1, classic.filterIsInstance<BirthOfLife>().size, "Birth of Life")
    }

    @Test
    fun filteringToClassicHidesTheExpansionKinds() {
        assertEquals(
            listOf(CardKind.Trait, CardKind.Age, CardKind.Catastrophe, CardKind.GenePool),
            cards.inSetsOf(CompendiumFilter(sets = setOf("classic"))).offeredKinds(),
        )
    }

    // Counts from the official rulesheet's transcription (assets/doomlings/rules/imaginary-ends.md): per subspecies,
    // "16X TRAITS // 2X DOMINANTS" (Deeplings, Glitterlings, Moonlings) and "9X TRAITS // 1X DOMINANT" (Fuzelings),
    // each with "2X AGES // 1X CATASTROPHE". The dominants come on top of the traits.
    @Test
    fun imaginaryEndsHasTheRulesheetTraitsAgesAndCatastrophes() {
        val ends = cards.filter { it.set == "imaginary-ends" }
        val traits = ends.filterIsInstance<Trait>()
        assertEquals(3 * 16 + 9, traits.count { !it.dominant }, "traits")
        assertEquals(3 * 2 + 1, traits.count { it.dominant }, "dominants")
        assertEquals(4 * 2, ends.filterIsInstance<Age>().size, "ages")
        assertEquals(4, ends.filterIsInstance<Catastrophe>().size, "catastrophes")
    }

    // An enabled card must open with something to read: its name, and its text on the types that always print one. A
    // Gene Pool prints neither.
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
            cards
                .filter { it.id in enabled }
                .filterNot { card ->
                    val face = card.face()
                    when (card) {
                        is GenePool -> face.name == null && face.blocks.isEmpty()
                        is Trait -> !face.name.isNullOrBlank()
                        else -> !face.name.isNullOrBlank() && face.blocks.isNotEmpty()
                    }
                }.map { it.id },
        )
    }

    @Test
    fun noTextStartsOrEndsWithWhitespace() {
        assertEquals(emptyList(), texts.filter { (_, text) -> text != text.trim() })
    }

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
    fun noTextOpensMoreParenthesesThanItCloses() {
        assertEquals(
            emptyList(),
            (texts + CardAssets.translatedTexts)
                .filter { (_, text) -> text.count { it == '(' } > text.count { it == ')' } }
                .map { (where, text) -> "$where: $text" },
        )
    }

    @Test
    fun noTraitRepeatsAColor() {
        assertEquals(
            emptyList(),
            cards.filterIsInstance<Trait>().filter { it.colors.size != it.colors.toSet().size }.map { it.id },
        )
    }

    @Test
    fun everyCardIsInItsSetFolder() {
        assertEquals(
            emptyList(),
            CardAssets.cardsByFolder.flatMap { (folder, cards) ->
                cards
                    .map { it.getValue("set").jsonPrimitive.content to it.getValue("id").jsonPrimitive.content }
                    .filter { (set, id) -> set != folder || !id.startsWith("$folder/") }
                    .map { (_, id) -> "$folder: $id" }
            },
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

    @Test
    fun everySetFolderDescribesItselfInASetJson() {
        CardAssets.cardsByFolder.keys.forEach { folder ->
            val set = Json.decodeFromString<CardSet>(dir.resolve(folder).resolve("set.json").readText())
            assertEquals(folder, set.id)
        }
    }
}
