package dev.nycocado.atlas.compendium

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nycocado.atlas.APP_PREFS
import dev.nycocado.atlas.CARD_LANGUAGE
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.CardRepository
import dev.nycocado.atlas.card.CardTranslations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompendiumViewModel
    @Inject
    constructor(
        repository: CardRepository,
        @ApplicationContext context: Context,
        savedState: SavedStateHandle,
    ) : ViewModel() {
        private val mutableLibrary = MutableStateFlow(CardLibrary.loading)
        private val mutableTranslations = MutableStateFlow(CardTranslations())
        private val prefs = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)

        // The language every card is read in. Until the player picks one, nothing is stored and it is the language
        // Android gives the app (ADR 0008); the pick is stored and wins from then on. A card without it shows its
        // printed text.
        private val mutableCardLanguage =
            MutableStateFlow(
                cardLanguageOf(prefs.getString(CARD_LANGUAGE, null))
                    ?: cardLanguageOf(
                        context.resources.configuration.locales[0]
                            .language,
                    )
                    ?: CardLanguage.English,
            )

        // The list's scroll position is restored too, and only matches the same filter.
        private var savedFilter by savedState.saved { CompendiumFilter() }
        private val mutableFilter = MutableStateFlow(savedFilter)

        val filter: StateFlow<CompendiumFilter> = mutableFilter.asStateFlow()

        // The names of the card groups that are open, saved with the filter; a fresh start has them all collapsed.
        private var savedOpenGroups by savedState.saved { emptySet<String>() }
        private val mutableOpenGroups = MutableStateFlow(savedOpenGroups)
        val openGroups: StateFlow<Set<String>> = mutableOpenGroups.asStateFlow()

        // Every card, for the detail screen.
        val library: StateFlow<CardLibrary> = mutableLibrary.asStateFlow()

        val translations: StateFlow<CardTranslations> = mutableTranslations.asStateFlow()
        val cardLanguage: StateFlow<CardLanguage> = mutableCardLanguage.asStateFlow()

        // null while the cards are loading.
        val results: StateFlow<List<CompendiumEntry>?> =
            combine(
                mutableLibrary.mapNotNull { it.cards },
                mutableFilter,
                mutableTranslations,
                mutableCardLanguage,
            ) { cards, filter, translations, language ->
                cards
                    .filterCompendium(
                        filter,
                        translations,
                        language,
                    ).groupByName()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        init {
            viewModelScope.launch { mutableLibrary.value = repository.library() }
            viewModelScope.launch { mutableTranslations.value = repository.translations() }
        }

        fun chooseCardLanguage(language: CardLanguage) {
            mutableCardLanguage.value = language
            prefs.edit { putString(CARD_LANGUAGE, language.code) }
        }

        fun toggleGroup(name: String) {
            val open = mutableOpenGroups.value.let { if (name in it) it - name else it + name }
            mutableOpenGroups.value = open
            savedOpenGroups = open
        }

        fun updateFilter(filter: CompendiumFilter) {
            mutableFilter.value = filter
            savedFilter = filter
        }
    }

private fun cardLanguageOf(code: String?) = CardLanguage.entries.find { it.code == code }
