package dev.nycocado.atlas.card

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

const val CARD_RATIO = 541f / 756f

// Cards packaged by the PackageCards task (app/build.gradle.kts) into assets/cards/.
fun cardImage(image: String) = "file:///android_asset/cards/$image"

// Open and provided by a module, so UI tests can replace the translations with their own.
open class CardRepository(
    private val context: Context,
) {
    suspend fun cards(): List<Card> =
        withContext(Dispatchers.IO) {
            decodeCards(
                context.assets
                    .open("cards/cards.json")
                    .bufferedReader()
                    .use { it.readText() },
            )
        }

    // The translation files packaged beside cards.json; a language with no file has no translations yet.
    open suspend fun translations(): CardTranslations =
        withContext(Dispatchers.IO) {
            val packaged = context.assets.list("cards").orEmpty()
            CardTranslations(
                CardLanguage.entries
                    .filter { "${it.code}.json" in packaged }
                    .associateWith { language ->
                        decodeTranslations(
                            context.assets
                                .open("cards/${language.code}.json")
                                .bufferedReader()
                                .use { it.readText() },
                        )
                    },
            )
        }
}

@Module
@InstallIn(SingletonComponent::class)
object CardRepositoryModule {
    @Provides
    @Singleton
    fun cardRepository(
        @ApplicationContext context: Context,
    ): CardRepository = CardRepository(context)
}
