package dev.nycocado.atlas

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardRepository
import dev.nycocado.atlas.card.CardRepositoryModule
import dev.nycocado.atlas.card.CardText
import dev.nycocado.atlas.card.CardTranslations
import javax.inject.Singleton

// Made-up Portuguese for a few Classic cards (ids from assets/doomlings/classic/cards.json), so UI tests don't depend
// on the shipped translations. Appealing has none: it is the card without Português.
val testPortuguese =
    mapOf(
        "classic/traits/adorable" to CardText("Adorável", flavor = "Esfregue minha barriga."),
        "classic/traits/altruistic" to
            CardText("Altruísta", listOf("Zumbido do tamanho da Pool."), flavor = "Doei tudo."),
        "classic/traits/apex-predator" to CardText("Predador Alfa", listOf("Roube uma carta.")),
        "classic/traits/antlers" to CardText("Chifres", flavor = "Fico bem na sua parede."),
        "classic/traits/eloquence" to
            CardText("Eloquência", listOf("Jogue outra."), flavor = "Fique quieto."),
        "classic/ages/age-of-dracula" to
            CardText(
                "Era de Drácula",
                listOf("Descarte 1 carta.", "Se Vampirism estiver na sua pilha de traits, roube 1 carta aleatória."),
            ),
        "classic/traits/self-replicating" to
            CardText("Autocópia", listOf("Jogue 1 carta. Ignore a ação dela ({action}).", "Volte para a mão.")),
        "classic/catastrophes/ice-age" to
            CardText("Era Glacial", listOf("Descarte cartas da mão.", "Menos pontos vermelhos.")),
    )

// Ink marks on two Classic effects, as the map of #192 has them, so UI tests don't wait for the marked card data:
// Heat Vision's "red" and Hyper-Intelligence's "Choose a color.".
val testInks =
    mapOf(
        "+1 for each red trait in your trait pile." to "+1 for each [red]red[/red] trait in your trait pile.",
        "Choose a color. Opponents discard 1 trait of that color" to
            "[rainbow]Choose a color.[/rainbow] Opponents discard 1 trait of that color",
    )

// Made-up Spanish, for the card language a Spanish device starts in.
val testSpanish = mapOf("classic/traits/altruistic" to CardText("Altruista", listOf("Zumbido del tamaño de la Pool.")))

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CardRepositoryModule::class])
object TestCardRepositoryModule {
    @Provides
    @Singleton
    fun cardRepository(
        @ApplicationContext context: Context,
    ): CardRepository =
        object : CardRepository(context) {
            // Fails once the card data has its own marks, so these give way to them.
            override fun cardsJson() =
                testInks.entries.fold(super.cardsJson()) { json, (plain, inked) ->
                    check(plain in json) { "No unmarked \"$plain\" left in cards.json" }
                    json.replace(plain, inked)
                }

            override suspend fun translations() =
                CardTranslations(
                    mapOf(CardLanguage.Portuguese to testPortuguese, CardLanguage.Spanish to testSpanish),
                )
        }
}
