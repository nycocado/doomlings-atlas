package dev.nycocado.atlas

import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.serialization.encodeToSavedState
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.CardRepository
import dev.nycocado.atlas.compendium.CompendiumFilter
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.compendium.Filters
import dev.nycocado.atlas.ui.theme.AtlasTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

// TestCardRepositoryModule keeps only Classic: it has Gene Pools and The Birth of Life, and none of the expansions' kinds.
@HiltAndroidTest
class CardKindsTest : AtlasBehaviorTest() {
    @Inject
    lateinit var repository: CardRepository

    private fun viewModel(
        saved: SavedStateHandle,
        cards: CardRepository = repository,
    ) = CompendiumViewModel(cards, context, saved)

    // Classic has no Merchants, as after an update that took a kind away. The key comes from a ViewModel, not from here.
    @Test
    fun aRestoredKindWithNoCardsOpensTraitsOnceTheCardsLoad() {
        val probe = SavedStateHandle().also { viewModel(it) }
        val key = probe.keys().single { "savedFilter" in it }
        val saved = SavedStateHandle(mapOf(key to encodeToSavedState(CompendiumFilter(kind = CardKind.Merchant))))

        val cardsArrive = CompletableDeferred<Unit>()
        val restored =
            viewModel(
                saved,
                object : CardRepository(context) {
                    override suspend fun library(): CardLibrary {
                        cardsArrive.await()
                        return repository.library()
                    }
                },
            )
        assertEquals(CardKind.Merchant, restored.filter.value.kind)

        cardsArrive.complete(Unit)
        // No Compose root here to keep the main looper running for the ViewModel's coroutine.
        compose.waitUntil(5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            restored.filter.value.kind == CardKind.Trait
        }
    }

    @Test
    fun aRestoredKindIsBroughtIntoViewOnceTheRowFillsIn() {
        var kinds by mutableStateOf(listOf(CardKind.Trait))
        showFilters(CompendiumFilter(kind = CardKind.Merchant), 240) { kinds }

        kinds = CardKind.entries

        text(R.string.kind_merchants).assertIsDisplayed()
    }

    @Test
    fun theSelectedKindStaysInViewWhenTheRowNarrows() {
        var width by mutableIntStateOf(1200)
        showFilters(CompendiumFilter(kind = CardKind.Merchant), { width }) { CardKind.entries }
        text(R.string.kind_merchants).assertIsDisplayed()

        width = 240

        text(R.string.kind_merchants).assertIsDisplayed()
    }

    private fun showFilters(
        filter: CompendiumFilter,
        width: Int,
        kinds: () -> List<CardKind>,
    ) = showFilters(filter, { width }, kinds)

    private fun showFilters(
        filter: CompendiumFilter,
        width: () -> Int,
        kinds: () -> List<CardKind>,
    ) {
        launchPastNotice().onActivity {
            it.setContent {
                AtlasTheme { Box(Modifier.width(width().dp)) { Filters(filter, kinds(), emptyList()) {} } }
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun aGenePoolShowsItsTypeWhereACardShowsItsName() {
        launchPastNotice()

        text(R.string.kind_gene_pools).performClick()
        compose.onAllNodes(hasContentDescription(string(R.string.type_gene_pool)))[0].performClick()

        compose.onAllNodesWithText(string(R.string.type_gene_pool)).onFirst().assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun theBirthOfLifeIsAmongTheAges() {
        launchPastNotice()

        text(R.string.kind_ages).performClick()

        scrollListTo(hasContentDescription("The Birth of Life"))
        compose.onNode(hasContentDescription("The Birth of Life")).assertIsDisplayed()
    }

    @Test
    fun theKindsAreAccessibleOnAPhone() {
        launchPastNotice()
        card("Adorable").assertIsDisplayed()

        assertAccessible()
    }
}

@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class CardKindsWideWindowTest : AtlasBehaviorTest() {
    @Test
    fun theKindsAreAccessibleOnAWideWindow() {
        launchPastNotice()
        card("Adorable").assertIsDisplayed()

        assertAccessible()
    }
}
