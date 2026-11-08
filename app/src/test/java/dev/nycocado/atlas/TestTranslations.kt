package dev.nycocado.atlas

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.CardRepository
import dev.nycocado.atlas.card.CardRepositoryModule
import dev.nycocado.atlas.card.CardText
import dev.nycocado.atlas.card.CardTranslations
import org.json.JSONArray
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

// Made-up Spanish, for the card language a Spanish device starts in.
val testSpanish = mapOf("classic/traits/altruistic" to CardText("Altruista", listOf("Zumbido del tamaño de la Pool.")))

const val TEST_BASE_SET = "Test Base"
const val TEST_EXPANSION_SET = "Test Expansion"

private val testSets =
    """[{"id": "classic", "name": "$TEST_BASE_SET", "year": null},
        {"id": "test-expansion", "name": "$TEST_EXPANSION_SET", "year": 2023}]"""

private fun withAdorableInTheExpansion(cards: String) =
    JSONArray(cards)
        .apply {
            (0 until length())
                .map(::getJSONObject)
                .first { it.getString("id") == "classic/traits/adorable" }
                .put("set", "test-expansion")
        }.toString()

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CardRepositoryModule::class])
object TestCardRepositoryModule {
    @Provides
    @Singleton
    fun cardRepository(
        @ApplicationContext context: Context,
    ): CardRepository =
        object : CardRepository(context) {
            // Classic's real set has no confirmed name yet, so the set filter runs on made-up sets: Adorable alone in
            // "Test Expansion", every other card in "Test Base".
            override suspend fun library() =
                CardLibrary.decode(withAdorableInTheExpansion(packaged("cards.json")), testSets)

            override suspend fun translations() =
                CardTranslations(
                    mapOf(CardLanguage.Portuguese to testPortuguese, CardLanguage.Spanish to testSpanish),
                )
        }
}
