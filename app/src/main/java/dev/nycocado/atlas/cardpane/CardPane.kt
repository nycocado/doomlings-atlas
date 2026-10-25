package dev.nycocado.atlas.cardpane

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.carddetail.CardDetailScreen
import dev.nycocado.atlas.ui.theme.StatusBarIcons
import kotlinx.serialization.Serializable

@Serializable
data class CardKey(
    val id: String,
) : NavKey

// The back stack is the only record of the open card, so it is restored with it.
@Composable
fun rememberCardPane(
    backStack: NavBackStack<NavKey>,
    cards: State<List<Card>?>,
    translations: State<CardTranslations>,
    language: State<CardLanguage>,
    onLanguageChange: (CardLanguage) -> Unit,
): CardPane {
    val pane = remember(backStack) { CardPane(backStack, cards, translations, language, onLanguageChange) }
    backStack.lastOrNull { it is CardKey }?.let { open -> SideEffect { pane.lastCard = (open as CardKey).id } }
    return pane
}

@Stable
class CardPane internal constructor(
    private val backStack: NavBackStack<NavKey>,
    private val cards: State<List<Card>?>,
    private val translations: State<CardTranslations>,
    private val language: State<CardLanguage>,
    private val onLanguageChange: (CardLanguage) -> Unit,
) {
    // The card's pane takes the card with it as it fades out: the card leaves the back stack before the pane hides,
    // so the list's detail placeholder shows the last card meanwhile (#48). Never cleared: the placeholder only shows
    // beside the list, and clearing it at the wrong frame would empty the pane mid-fade.
    internal var lastCard by mutableStateOf<String?>(null)

    val isOpen: Boolean get() = backStack.any { it is CardKey }

    fun open(id: String) {
        backStack.removeAll { it is CardKey }
        backStack.add(CardKey(id))
    }

    fun close() {
        backStack.removeLastOrNull()
    }

    // Movable, so it keeps its tilt and scroll.
    private val content =
        movableContentOf { id: String, layout: ListDetailLayout, onBack: () -> Unit, onAboutClick: () -> Unit ->
            val all = cards.value
            val card = all?.find { it.id == id }
            // A back stack restored after an update can name a card the data no longer has.
            if (all != null && card == null) LaunchedEffect(id) { onBack() }
            // The sheet shows beside the list even while the cards load.
            Box(if (layout.sideBySide) Modifier.sheet() else Modifier) {
                card?.let {
                    CardDetailScreen(
                        it,
                        translations.value,
                        language.value,
                        onLanguageChange,
                        layout,
                        onBack,
                        onAboutClick,
                    )
                }
            }
        }

    @Composable
    fun Detail(
        id: String,
        layout: ListDetailLayout,
        onAboutClick: () -> Unit,
    ) = content(id, layout, ::close, onAboutClick)

    @Composable
    fun DetailPlaceholder(layout: ListDetailLayout) {
        lastCard?.let { content(it, layout, {}, {}) }
    }
}

@Composable
fun CardStatusBarIcons(
    layout: ListDetailLayout,
    darkIcons: Boolean,
) {
    if (layout.cardSetsStatusBarIcons) StatusBarIcons(darkIcons)
}
