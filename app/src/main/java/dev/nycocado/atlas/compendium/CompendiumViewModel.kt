package dev.nycocado.atlas.compendium

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.nycocado.atlas.CARD_LANGUAGE
import dev.nycocado.atlas.NOTICE_PREFS
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardRepository
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.NamedCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
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
        private val mutableCards = MutableStateFlow<List<Card>?>(null)
        private val mutableTranslations = MutableStateFlow(CardTranslations())
        private val prefs = context.getSharedPreferences(NOTICE_PREFS, Context.MODE_PRIVATE)

        // The language every card is read in, stored across launches. A card without it shows its printed text.
        private val mutableCardLanguage =
            MutableStateFlow(
                CardLanguage.entries.find { it.code == prefs.getString(CARD_LANGUAGE, null) } ?: CardLanguage.English,
            )

        // The list's scroll position is restored too, and only matches the same filter.
        private var savedFilter by savedState.saved { CompendiumFilter() }
        private val mutableFilter = MutableStateFlow(savedFilter)

        val filter: StateFlow<CompendiumFilter> = mutableFilter.asStateFlow()

        // Every card, for the detail screen; null while loading, like results.
        val cards: StateFlow<List<Card>?> = mutableCards.asStateFlow()

        val translations: StateFlow<CardTranslations> = mutableTranslations.asStateFlow()
        val cardLanguage: StateFlow<CardLanguage> = mutableCardLanguage.asStateFlow()

        // null while the cards are loading.
        val results: StateFlow<List<NamedCard>?> =
            combine(
                mutableCards.filterNotNull(),
                mutableFilter,
                mutableTranslations,
                mutableCardLanguage,
            ) { cards, filter, translations, language -> cards.filterCompendium(filter, translations, language) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        init {
            viewModelScope.launch { mutableCards.value = repository.cards() }
            viewModelScope.launch { mutableTranslations.value = repository.translations() }
        }

        fun chooseCardLanguage(language: CardLanguage) {
            mutableCardLanguage.value = language
            prefs.edit { putString(CARD_LANGUAGE, language.code) }
        }

        fun updateFilter(filter: CompendiumFilter) {
            mutableFilter.value = filter
            savedFilter = filter
        }
    }
