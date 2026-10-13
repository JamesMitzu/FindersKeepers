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

    data class LabelResult(
        val text: String,
        val confidence: Float
    )

    suspend fun labelImage(context: Context, uri: Uri): List<LabelResult> {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.55f)
                .build()
            val labeler = ImageLabeling.getClient(options)
            val labels = labeler.process(image).await()

            labels
                .sortedByDescending { it.confidence }
                .map { LabelResult(it.text, it.confidence) }
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

    fun chooseBestName(
        labels: List<LabelResult>,
        ocrText: String
    ): String? {

        val ocrCandidates = ocrText.lines()
            .map { it.trim() }
            .filter { line ->
                line.length in 3..32 &&
                        line.any { it.isLetter() } &&
                        !line.matches(Regex("(?i).*(www\\.|https?://|©|®|™|page\\s*\\d+|slide|isbn).*"))
            }
            .distinct()

        val goodOcr = ocrCandidates.firstOrNull { candidate ->
            val words = candidate.split(Regex("\\s+"))
            words.size in 1..4 &&
                    candidate.first().isUpperCase() &&
                    !candidate.all { it.isUpperCase() }
        }

        val genericLabels = setOf(
            "material", "font", "rectangle", "space", "gas", "line", "parallel", "circle",
            "graphics", "symmetry", "pattern", "room", "floor", "wall", "ceiling", "building",
            "furniture", "wood", "table", "surface", "indoor", "shadow", "light", "design",
            "woodwork", "steel", "grey", "gray", "black", "white", "yellow", "red", "blue",
            "green", "brown", "purple", "pink", "orange", "silver", "gold", "color", "style",
            "shape", "square", "texture", "technology", "gadget", "object", "item", "thing",
            "product", "packaging", "plastic", "metal", "glass", "paper", "cardboard",
            "background", "photograph", "image", "picture", "photo", "screenshot"
        )

        val relevantLabels = labels
            .filter { it.text.lowercase().trim() !in genericLabels }
            .sortedByDescending { it.confidence }

        val topRelevant = relevantLabels.firstOrNull()
        val topAny = labels.firstOrNull()

        return when {
            goodOcr != null -> goodOcr
            topRelevant != null && topRelevant.confidence >= 0.75f -> topRelevant.text
            ocrCandidates.isNotEmpty() -> ocrCandidates.first()
            topRelevant != null -> topRelevant.text
            topAny != null -> topAny.text
            else -> null
        }
    }

    suspend fun suggestItemName(context: Context, uri: Uri): String? {
        val labels = labelImage(context, uri)
        val ocr = recognizeText(context, uri)
        return chooseBestName(labels, ocr)
    }
}