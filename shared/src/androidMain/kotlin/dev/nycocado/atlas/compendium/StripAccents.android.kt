package dev.nycocado.atlas.compendium

import java.text.Normalizer

private val marks = Regex("\\p{Mn}+")

// NFD splits each letter from its accent ("ã" becomes "a" + "~"), then the accents are removed.
internal actual fun stripAccents(text: String): String =
    marks.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")
