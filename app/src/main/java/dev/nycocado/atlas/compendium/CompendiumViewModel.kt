package dev.nycocado.atlas.compendium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardRepository
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
    ) : ViewModel() {
        private val mutableCards = MutableStateFlow<List<Card>?>(null)
        private val mutableFilter = MutableStateFlow(CompendiumFilter())

        val filter: StateFlow<CompendiumFilter> = mutableFilter.asStateFlow()

        // Every card, for the detail screen; null while loading, like results.
        val cards: StateFlow<List<Card>?> = mutableCards.asStateFlow()

        // null while the cards are loading.
        val results: StateFlow<List<Card>?> =
            combine(mutableCards.filterNotNull(), mutableFilter) { cards, filter -> cards.filterCompendium(filter) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        init {
            viewModelScope.launch { mutableCards.value = repository.cards() }
        }

        fun updateFilter(filter: CompendiumFilter) {
            mutableFilter.value = filter
        }
    }
