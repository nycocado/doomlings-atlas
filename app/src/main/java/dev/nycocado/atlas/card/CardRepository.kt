package dev.nycocado.atlas.card

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

const val CARD_RATIO = 541f / 756f

// Cards packaged by the PackageCards task (app/build.gradle.kts) into assets/cards/.
fun cardImage(image: String) = "file:///android_asset/cards/$image"

@Singleton
class CardRepository
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
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
        suspend fun translations(): CardTranslations =
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
