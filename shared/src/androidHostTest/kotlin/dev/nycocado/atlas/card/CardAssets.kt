package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import java.io.File

// The real assets/doomlings, where each set folder holds its cards.json and translations (pt.json, es.json).
object CardAssets {
    val dir = File(System.getProperty("cards.dir")!!)

    private val setFolders = dir.listFiles { file -> file.resolve("cards.json").isFile }.orEmpty().sorted()

    // Every set's cards.json joined into one array, as PackageCards joins them.
    val cardsJson =
        JsonArray(
            setFolders.flatMap { Json.parseToJsonElement(it.resolve("cards.json").readText()).jsonArray },
        ).toString()

    val translationFiles =
        setFolders.flatMap { folder ->
            folder.listFiles { file -> file.extension == "json" && file.name != "cards.json" }.orEmpty().sorted()
        }
}
