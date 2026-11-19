package dev.nycocado.atlas.card

// Written out apart from Ink, like the tokens in CardDataIntegrityTest, so renaming an ink in the code fails the data.
private val knownInks = setOf("red", "blue", "green", "purple", "colorless", "rainbow", "yellow", "wonder")

private const val SMALL = "small"

private val tokenWithBracket = Regex("""\{[^}]*[\[\]][^}]*}""")

private val mark = Regex("""\[(/?)([^\[\]]*)]|[\[\]]""")

private val openingMark = Regex("""\[([^\[\]/]*)]""")

private val smallSpan = Regex("""\[small]([\s\S]*?)\[/small]""")

// What is wrong with the ink and small marks of a text, empty when every ink span is a known ink, closed by its own
// mark, alone and clear of the tokens, and every small span is closed, not inside another and not crossing an ink span.
// A small span may hold ink spans.
fun inkMarkProblems(text: String): List<String> =
    buildList {
        tokenWithBracket.findAll(text).forEach { add("span crossing ${it.value}") }
        var open: String? = null
        var small = false
        for (match in mark.findAll(text)) {
            val (slash, ink) = match.destructured
            when {
                match.value.length == 1 -> {
                    add("stray ${match.value}")
                }

                ink == SMALL -> {
                    open?.let { add("[$slash$SMALL] inside [$it]") }
                    when {
                        slash.isEmpty() && small -> add("[$SMALL] inside [$SMALL]")
                        slash.isNotEmpty() && !small -> add("stray [/$SMALL]")
                    }
                    small = slash.isEmpty()
                }

                slash.isEmpty() -> {
                    if (ink !in knownInks) add("unknown ink [$ink]")
                    if (open != null) add("[$ink] inside [$open]")
                    open = ink
                }

                open == null -> {
                    add("stray [/$ink]")
                }

                else -> {
                    if (ink != open) add("[$open] closed by [/$ink]")
                    open = null
                }
            }
        }
        open?.let { add("unclosed [$it]") }
        if (small) add("unclosed [$SMALL]")
    }

// The inks of a text's spans, in order.
fun inks(text: String): List<String> =
    openingMark
        .findAll(text)
        .map { it.groupValues[1] }
        .filter { it != SMALL }
        .toList()

private val inkOrToken = Regex("""\[([^\[\]/]*)]|\{[^}]*}""")

// A small span's place: its line, whether it starts and ends that line, and the inks and tokens it holds, in order.
data class SmallSpan(
    val line: Int,
    val startsLine: Boolean,
    val endsLine: Boolean,
    val marks: List<String>,
)

// The text's small spans in order, so a span lost, added or moved to other words shows.
fun smallSpans(text: String): List<SmallSpan> =
    smallSpan
        .findAll(text)
        .map { span ->
            val before = text.substring(0, span.range.first)
            SmallSpan(
                line = before.count { it == '\n' },
                startsLine = before.substringAfterLast('\n').isBlank(),
                endsLine = text.substring(span.range.last + 1).substringBefore('\n').isBlank(),
                marks = inkOrToken.findAll(span.groupValues[1]).map { it.value }.toList(),
            )
        }.toList()
