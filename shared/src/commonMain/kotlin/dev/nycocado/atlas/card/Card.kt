package dev.nycocado.atlas.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val effect: String?
}

@Serializable
@SerialName("trait")
data class Trait(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    val flavor: String?,
    override val effect: String?,
    // null when the value depends on the game ("Value is equal to...").
    val points: Int?,
    // Empty for colorless traits.
    val colors: List<CardColor>,
    val dominant: Boolean,
    val subspecies: List<String>,
    val icons: List<TraitIcon>,
) : NamedCard {
    val effectless: Boolean get() = effect == null
}

@Serializable
@SerialName("age")
data class Age(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effect: String,
) : NamedCard

@Serializable
@SerialName("catastrophe")
data class Catastrophe(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effect: String,
    val worldsEnd: String,
) : NamedCard

@Serializable
@SerialName("birth-of-life")
data class BirthOfLife(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effect: String,
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
    override val effect: String,
    val flavor: String?,
) : NamedCard

@Serializable
@SerialName("trinket")
data class Trinket(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effect: String,
    val flavor: String,
) : NamedCard

@Serializable
@SerialName("treasure")
data class Treasure(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effect: String,
) : NamedCard

@Serializable
@SerialName("merchant")
data class Merchant(
    override val id: String,
    override val set: String,
    override val image: String,
    override val name: String,
    override val effect: String,
) : NamedCard

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
