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
            CardText("Altruísta", effect = "Zumbido do tamanho da Pool.", flavor = "Doei tudo."),
        "classic/traits/apex-predator" to CardText("Predador Alfa", effect = "Roube uma carta."),
        "classic/traits/antlers" to CardText("Chifres", flavor = "Fico bem na sua parede."),
        "classic/traits/eloquence" to
            CardText("Eloquência", effect = "No Fim do Mundo: jogue outra.", flavor = "Fique quieto."),
        "classic/ages/age-of-dracula" to
            CardText(
                "Era de Drácula",
                effect = "Se Vampirism estiver na sua pilha de traits, roube 1 carta aleatória.",
            ),
        "classic/catastrophes/ice-age" to
            CardText("Era Glacial", effect = "Descarte cartas da mão.", worldsEnd = "Menos pontos vermelhos."),
    )

// Made-up Spanish, for the card language a Spanish device starts in.
val testSpanish = mapOf("classic/traits/altruistic" to CardText("Altruista", effect = "Zumbido del tamaño de la Pool."))

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CardRepositoryModule::class])
object TestCardRepositoryModule {
    @Provides
    @Singleton
    fun cardRepository(
        @ApplicationContext context: Context,
    ): CardRepository =
        object : CardRepository(context) {
            override suspend fun translations() =
                CardTranslations(
                    mapOf(CardLanguage.Portuguese to testPortuguese, CardLanguage.Spanish to testSpanish),
                )
        }
}
