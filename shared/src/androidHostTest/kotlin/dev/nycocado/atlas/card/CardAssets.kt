package dev.nycocado.atlas.card

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File

// The real assets/doomlings, where each set folder holds its cards.json and translations (pt.json, es.json).
object CardAssets {
    val dir = File(System.getProperty("cards.dir")!!)

    private val setFolders = dir.listFiles { file -> file.resolve("cards.json").isFile }.orEmpty().sorted()

    // Each set folder's cards, by folder name.
    val cardsByFolder: Map<String, List<JsonObject>> =
        setFolders.associate { folder ->
            folder.name to
                Json.parseToJsonElement(folder.resolve("cards.json").readText()).jsonArray.map { it.jsonObject }
        }

    // Every set's cards.json joined into one array, as PackageCards joins them.
    val cardsJson = JsonArray(cardsByFolder.values.flatten()).toString()

    val translationFiles =
        setFolders.flatMap { folder ->
            folder.listFiles { file -> file.extension == "json" && file.name != "cards.json" }.orEmpty().sorted()
        }

    // A JSON object's keys in file order, repeats included: parsing keeps only the last of two equal keys.
    fun keysOf(file: File): List<String> = Json.decodeFromString(KeysInOrder, file.readText())

    private object KeysInOrder : DeserializationStrategy<List<String>> {
        override val descriptor = MapSerializer(String.serializer(), JsonElement.serializer()).descriptor

        override fun deserialize(decoder: Decoder): List<String> =
            decoder.decodeStructure(descriptor) {
                buildList {
                    while (true) {
                        val index = decodeElementIndex(descriptor)
                        if (index == CompositeDecoder.DECODE_DONE) break
                        add(decodeStringElement(descriptor, index))
                        decodeSerializableElement(descriptor, decodeElementIndex(descriptor), JsonElement.serializer())
                    }
                }
            }
    }
}
