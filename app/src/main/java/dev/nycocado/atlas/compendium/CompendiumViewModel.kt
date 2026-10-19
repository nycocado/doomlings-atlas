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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompendiumViewModel
    @Inject
    constructor(
        repository: CardRepository,
    ) : ViewModel() {
        private val cards = MutableStateFlow<List<Card>?>(null)
        private val mutableFilter = MutableStateFlow(CompendiumFilter())

        val filter: StateFlow<CompendiumFilter> = mutableFilter.asStateFlow()

        // null while the cards are loading.
        val results: StateFlow<List<Card>?> =
            combine(cards.filterNotNull(), mutableFilter) { cards, filter -> cards.filterCompendium(filter) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        init {
            viewModelScope.launch { cards.value = repository.cards() }
        }

        fun updateFilter(change: (CompendiumFilter) -> CompendiumFilter) = mutableFilter.update(change)
    }
