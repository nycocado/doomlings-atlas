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
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.CardLookup
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.carddetail.CardDetailScreen
import dev.nycocado.atlas.leave
import dev.nycocado.atlas.ui.theme.StatusBarIcons
import kotlinx.serialization.Serializable

@Serializable
data class CardKey(
    val id: String,
    // How many cards sit under this one, each opened from a name cited in the one below.
    val depth: Int = 0,
) : NavKey

// The back stack is the only record of the open card, so it is restored with it.
@Composable
fun rememberCardPane(
    backStack: NavBackStack<NavKey>,
    library: State<CardLibrary>,
    translations: State<CardTranslations>,
    language: State<CardLanguage>,
    onLanguageChange: (CardLanguage) -> Unit,
): CardPane {
    val pane = remember(backStack) { CardPane(backStack, library, translations, language, onLanguageChange) }
    backStack.lastOrNull { it is CardKey }?.let { open -> SideEffect { pane.lastCard = open as CardKey } }
    return pane
}

@Stable
class CardPane internal constructor(
    private val backStack: NavBackStack<NavKey>,
    private val library: State<CardLibrary>,
    private val translations: State<CardTranslations>,
    private val language: State<CardLanguage>,
    private val onLanguageChange: (CardLanguage) -> Unit,
) {
    // The card's pane takes the card with it as it fades out: the card leaves the back stack before the pane hides,
    // so the list's detail placeholder shows the last card meanwhile (#48). Never cleared: the placeholder only shows
    // beside the list, and clearing it at the wrong frame would empty the pane mid-fade.
    internal var lastCard by mutableStateOf<CardKey?>(null)

    val isOpen: Boolean get() = backStack.any { it is CardKey }

    fun open(id: String) {
        backStack.removeAll { it is CardKey }
        backStack.add(CardKey(id))
    }

    // On top of the open card, which back returns to.
    fun openCited(id: String) {
        backStack.add(CardKey(id, backStack.count { it is CardKey }))
    }

    fun close(key: CardKey) = backStack.leave(key)

    // Movable, so it keeps its tilt and scroll. One per depth, as a cited card and its citer are on screen together.
    private val contents = mutableMapOf<Int, @Composable (String, ListDetailLayout, Boolean, () -> Unit) -> Unit>()

    private fun contentAt(depth: Int) =
        contents.getOrPut(depth) {
            movableContentOf { id: String, layout: ListDetailLayout, interactive: Boolean, onAboutClick: () -> Unit ->
                val key = CardKey(id, depth)
                val lookup = library.value.lookup(id)
                if (lookup == CardLookup.Missing && interactive) LaunchedEffect(id) { close(key) }
                // The sheet shows beside the list even while the cards load.
                Box(if (layout.sideBySide) Modifier.sheet() else Modifier) {
                    if (lookup is CardLookup.Found) {
                        CardDetailScreen(
                            lookup.card,
                            translations.value,
                            language.value,
                            onLanguageChange,
                            layout,
                            library.value,
                            onBack = if (interactive) ({ close(key) }) else ({}),
                            onAboutClick = onAboutClick,
                            onCardClick = if (interactive) ::openCited else ({}),
                        )
                    }
                }
            }
        }

    @Composable
    fun Detail(
        key: CardKey,
        layout: ListDetailLayout,
        onAboutClick: () -> Unit,
    ) = contentAt(key.depth)(key.id, layout, true, onAboutClick)

    @Composable
    fun DetailPlaceholder(layout: ListDetailLayout) {
        lastCard?.let { contentAt(it.depth)(it.id, layout, false, {}) }
    }
}

@Composable
fun CardStatusBarIcons(
    layout: ListDetailLayout,
    darkIcons: Boolean,
) {
    if (layout.cardSetsStatusBarIcons) StatusBarIcons(darkIcons)
}
