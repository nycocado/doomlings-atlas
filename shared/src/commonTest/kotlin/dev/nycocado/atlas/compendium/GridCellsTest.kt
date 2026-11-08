package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardColor.Blue
import dev.nycocado.atlas.card.CardColor.Green
import dev.nycocado.atlas.card.CardColor.Red
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import kotlin.test.Test
import kotlin.test.assertEquals

class GridCellsTest {
    private fun trait(
        name: String,
        number: Int,
        color: CardColor,
    ) = Trait(
        "classic/traits/${name.lowercase()}-$number",
        "classic",
        "$name$number.webp",
        name,
        null,
        emptyList(),
        null,
        1,
        listOf(color),
        false,
        emptyList(),
        emptyList(),
    )

    private fun variants(
        name: String,
        count: Int,
        color: CardColor,
    ) = (1..count).map { trait(name, it, color) }

    // Kidney (red) and Swarm (green) are groups; Zest (blue) stays a single card.
    private val cards: List<NamedCard> =
        variants("Kidney", 3, Red) + variants("Swarm", 2, Green) + trait("Zest", 1, Blue)

    private fun cells(
        open: Set<String>,
        filter: CompendiumFilter = CompendiumFilter(),
    ) = CardLibrary.of(cards).let { it.groupByName(cards.filterCompendium(filter)).toGridCells(open) }

    private fun GridCell.describe() =
        when (this) {
            is GridCell.Single -> "card ${(card as NamedCard).name} @$index"
            is GridCell.Stack -> "stack $name x${variants.size} @$index"
            is GridCell.Collapse -> "collapse $name x$count"
            is GridCell.Variant -> "variant ${card.id} $position/$count"
        }

    @Test
    fun closedGroupsAreOneStackEach() {
        assertEquals(
            listOf("stack Kidney x3 @0", "stack Swarm x2 @1", "card Zest @2"),
            cells(emptySet()).map { it.describe() },
        )
    }

    @Test
    fun anOpenGroupIsACollapseCellFollowedByItsVariantsFromPositionOne() {
        assertEquals(
            listOf(
                "collapse Kidney x3",
                "variant classic/traits/kidney-1 1/3",
                "variant classic/traits/kidney-2 2/3",
                "variant classic/traits/kidney-3 3/3",
                "stack Swarm x2 @1",
                "card Zest @2",
            ),
            cells(setOf("Kidney")).map { it.describe() },
        )
    }

    @Test
    fun twoOpenGroupsEachRestartTheirPositions() {
        val described = cells(setOf("Kidney", "Swarm")).map { it.describe() }

        assertEquals(
            listOf(
                "collapse Kidney x3",
                "variant classic/traits/kidney-1 1/3",
                "variant classic/traits/kidney-2 2/3",
                "variant classic/traits/kidney-3 3/3",
                "collapse Swarm x2",
                "variant classic/traits/swarm-1 1/2",
                "variant classic/traits/swarm-2 2/2",
                "card Zest @2",
            ),
            described,
        )
    }

    @Test
    fun aFilterHidingAnOpenGroupAndThenShowingItAgainOpensItAgain() {
        val open = setOf("Kidney")

        val hidden = cells(open, CompendiumFilter(colors = setOf(Blue)))
        val shown = cells(open, CompendiumFilter())

        assertEquals(listOf("card Zest @0"), hidden.map { it.describe() })
        assertEquals(
            listOf(
                "collapse Kidney x3",
                "variant classic/traits/kidney-1 1/3",
                "variant classic/traits/kidney-2 2/3",
                "variant classic/traits/kidney-3 3/3",
                "stack Swarm x2 @1",
                "card Zest @2",
            ),
            shown.map { it.describe() },
        )
    }

    @Test
    fun anOpenSingleCardStaysACard() {
        assertEquals(
            "card Zest @2",
            cells(setOf("Zest")).last().describe(),
        )
    }
}
