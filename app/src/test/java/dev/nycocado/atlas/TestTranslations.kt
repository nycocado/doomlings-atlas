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
import dev.nycocado.atlas.card.CardTranslation
import dev.nycocado.atlas.card.CardTranslations
import javax.inject.Singleton

// Made-up Portuguese for a few Classic cards (ids from assets/doomlings/cards.json), so UI tests don't depend on the
// shipped translations. Appealing has none: it is the card without Português.
val testPortuguese =
    mapOf(
        "classic/traits/adorable" to CardTranslation("Adorável", flavor = "Esfrega a minha barriga."),
        "classic/traits/altruistic" to
            CardTranslation("Altruísta", effect = "Zumbido do tamanho da Pool.", flavor = "Doei tudo."),
        "classic/traits/antlers" to CardTranslation("Chifres", flavor = "Fico bem na tua parede."),
        "classic/catastrophes/ice-age" to
            CardTranslation("Era Glacial", effect = "Descarta cartas da mão.", worldsEnd = "Menos pontos vermelhos."),
    )

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CardRepositoryModule::class])
object TestCardRepositoryModule {
    @Provides
    @Singleton
    fun cardRepository(
        @ApplicationContext context: Context,
    ): CardRepository =
        object : CardRepository(context) {
            override suspend fun translations() = CardTranslations(mapOf(CardLanguage.Portuguese to testPortuguese))
        }
}
