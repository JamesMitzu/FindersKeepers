package com.example.finderskeepersapplication.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object ImageAnalysisHelper {

    suspend fun labelImage(context: Context, uri: Uri): List<String> {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.65f)
                .build()
            val labeler = ImageLabeling.getClient(options)
            val labels = labeler.process(image).await()
            labels.sortedByDescending { it.confidence }.map { it.text }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun recognizeText(context: Context, uri: Uri): String {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            ""
        }
    }

    fun chooseBestName(labels: List<String>, ocrText: String): String? {
        val ocrLines = ocrText.lines()
            .map { it.trim() }
            .filter { line -> line.length in 3..25 && line.any { it.isLetter() } }
        val prominentOcr = ocrLines.firstOrNull()

        val genericLabels = setOf(
            "material", "font", "rectangle", "space", "gas", "line", "parallel", "circle",
            "graphics", "symmetry", "pattern", "room", "floor", "wall", "ceiling", "building",
            "furniture", "wood", "table", "surface", "indoor", "shadow", "light", "design",
            "woodwork", "steel", "grey", "black", "white", "yellow", "red", "blue", "green",
            "brown", "purple", "pink", "orange", "silver", "gold", "color", "style", "shape",
            "square", "texture", "technology", "gadget", "object", "item", "thing"
        )
        val relevantLabels = labels.filter { it.lowercase().trim() !in genericLabels }

        val topLabel = relevantLabels.firstOrNull() ?: labels.firstOrNull()

        return when {
            !prominentOcr.isNullOrBlank() -> prominentOcr
            !topLabel.isNullOrBlank() -> topLabel
            else -> null
        }
    }
}
