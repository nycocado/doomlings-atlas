package dev.nycocado.atlas.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// One class per "type" in the card data (assets/doomlings/<set>/cards.json), with only the fields printed on that kind
// of card.
@Serializable
sealed interface Card {
    val id: String
    val set: String
    val image: String
}

@Serializable
sealed interface NamedCard : Card {
    val name: String
    val effects: List<Effect>
}

@Serializable
@SerialName("trait")
data class Trait(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    val flavor: String?,
    override val effects: List<Effect>,
    // The "+N Gene Pool" line, as a number.
    val genePool: Int?,
    // null when the value depends on the game ("Value is equal to...").
    val points: Int?,
    // Empty for colorless traits.
    val colors: List<CardColor>,
    val dominant: Boolean,
    val subspecies: List<String>,
    val icons: List<TraitIcon>,
) : NamedCard {
    val effectless: Boolean get() = effects.isEmpty() && genePool == null
}

@Serializable
@SerialName("age")
data class Age(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
) : NamedCard

@Serializable
@SerialName("catastrophe")
data class Catastrophe(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
    val genePool: Int?,
) : NamedCard

@Serializable
@SerialName("birth-of-life")
data class BirthOfLife(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
) : NamedCard

// Only the color and art are printed on it.
@Serializable
@SerialName("gene-pool")
data class GenePool(
    override val id: String,
    override val set: String,
    override val image: String,
) : Card

@Serializable
@SerialName("meaning-of-life")
data class MeaningOfLife(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
    val flavor: String?,
) : NamedCard

@Serializable
@SerialName("trinket")
data class Trinket(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
    val value: Int,
    val flavor: String,
) : NamedCard

@Serializable
@SerialName("treasure")
data class Treasure(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
) : NamedCard

@Serializable
@SerialName("merchant")
data class Merchant(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effects: List<Effect>,
) : NamedCard

// One section of a card's printed text, in printed order. No kind is a plain effect. The text may hold tokens in braces
// ({action}) for the inline icons.
@Serializable
data class Effect(
    val kind: EffectKind? = null,
    val text: String,
)

// The kinds for when the text applies and for small-type notes fit any card with text. A label kind is the printed label
// of a text section and only its card type has it.
@Serializable
enum class EffectKind(
    val labelOf: KClass<out NamedCard>? = null,
) {
    @SerialName("action")
    Action,

    @SerialName("play-when")
    PlayWhen,

    @SerialName("worlds-end")
    WorldsEnd,

    @SerialName("note")
    Note,

    @SerialName("awesome-power")
    AwesomePower(Trinket::class),

    @SerialName("pocket-objective")
    PocketObjective(Trinket::class),

    @SerialName("rush")
    Rush(Treasure::class),

    @SerialName("mutate")
    Mutate(Treasure::class),

    @SerialName("echo")
    Echo(Treasure::class),

    @SerialName("cloak")
    Cloak(Treasure::class),

    @SerialName("adapt")
    Adapt(Treasure::class),

    @SerialName("exchange")
    Exchange(Merchant::class),

    @SerialName("buy")
    Buy(Merchant::class),

    @SerialName("collect")
    Collect(Merchant::class),

    @SerialName("bargain")
    Bargain(Merchant::class),

    ;

    fun allowedOn(card: NamedCard) = labelOf == null || labelOf.isInstance(card)
}

@Serializable
enum class CardColor {
    @SerialName("blue")
    Blue,

    @SerialName("green")
    Green,

    @SerialName("purple")
    Purple,

    @SerialName("red")
    Red,
}

@Serializable
enum class TraitIcon {
    @SerialName("action")
    Action,

    @SerialName("attachment")
    Attachment,

    @SerialName("coral")
    Coral,

    @SerialName("curious")
    Curious,

    @SerialName("dragon")
    Dragon,

    @SerialName("drop-of-life")
    DropOfLife,

    @SerialName("exotic")
    Exotic,

    @SerialName("fish")
    Fish,

    @SerialName("play-when")
    PlayWhen,

    @SerialName("shell")
    Shell,

    @SerialName("spell")
    Spell,

    @SerialName("suppress")
    Suppress,

    @SerialName("worlds-end")
    WorldsEnd,
}
