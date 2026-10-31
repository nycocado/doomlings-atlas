package dev.nycocado.atlas.card

// Written out apart from Ink, like the tokens in CardDataIntegrityTest, so renaming an ink in the code fails the data.
private val knownInks = setOf("red", "blue", "green", "purple", "colorless", "rainbow", "yellow", "wonder")

private val tokenWithBracket = Regex("""\{[^}]*[\[\]][^}]*}""")

private val mark = Regex("""\[(/?)([^\[\]]*)]|[\[\]]""")

private val openingMark = Regex("""\[([^\[\]/]*)]""")

// What is wrong with the ink marks of a text, empty when every span is a known ink, closed by its own mark, alone and
// clear of the tokens.
fun inkMarkProblems(text: String): List<String> =
    buildList {
        tokenWithBracket.findAll(text).forEach { add("span crossing ${it.value}") }
        var open: String? = null
        for (match in mark.findAll(text)) {
            val (slash, ink) = match.destructured
            when {
                match.value.length == 1 -> {
                    add("stray ${match.value}")
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
    }

// The inks of a text's spans, in order.
fun inks(text: String): List<String> = openingMark.findAll(text).map { it.groupValues[1] }.toList()
