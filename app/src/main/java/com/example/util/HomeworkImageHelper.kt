package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

object HomeworkImageHelper {

    private const val TAG = "HomeworkImageHelper"
    private const val TARGET_MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 85

    /**
     * Creates a temporary file in the app cache and returns a content URI for TakePicture.
     */
    fun createTempImageUri(context: Context): Uri {
        val cacheDir = File(context.cacheDir, "camera_photos")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val tempFile = File(cacheDir, "hw_capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Decodes, corrects rotation via EXIF, and scales the photo appropriately to preserve
     * handwriting, math formulas, and small text while avoiding memory exhaustion or upload bloat.
     */
    fun loadAndOptimizeImage(
        context: Context,
        uri: Uri,
        maxDimension: Int = TARGET_MAX_DIMENSION
    ): Bitmap? {
        try {
            // Step 1: Decode image dimensions without loading pixel data into memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) {
                Log.e(TAG, "Invalid image bounds: ${originalWidth}x${originalHeight}")
                return null
            }

            // Step 2: Compute power-of-two inSampleSize
            var sampleSize = 1
            val largestDim = max(originalWidth, originalHeight)
            while ((largestDim / sampleSize) > maxDimension * 1.5) {
                sampleSize *= 2
            }

            // Step 3: Decode bitmap using computed sampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // Step 4: Correct EXIF orientation
            val rotatedBitmap = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    applyExifRotation(sampledBitmap, orientation)
                } ?: sampledBitmap
            } catch (e: Exception) {
                Log.w(TAG, "Failed to read EXIF orientation: ${e.message}")
                sampledBitmap
            }

            // Step 5: High-quality scale down to target dimension if still slightly exceeding
            val currentMaxDim = max(rotatedBitmap.width, rotatedBitmap.height)
            return if (currentMaxDim > maxDimension) {
                val ratio = maxDimension.toFloat() / currentMaxDim.toFloat()
                val targetW = (rotatedBitmap.width * ratio).toInt()
                val targetH = (rotatedBitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(rotatedBitmap, targetW, targetH, true)
            } else {
                rotatedBitmap
            }
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OOM while loading image: ${e.message}")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URI: ${e.message}", e)
            return null
        }
    }

    /**
     * Checks if bitmap is readable or empty/unreadable.
     */
    fun validateImageReadability(bitmap: Bitmap): Pair<Boolean, String?> {
        if (bitmap.width < 120 || bitmap.height < 120) {
            return Pair(false, "The captured image resolution is too low to clearly read equations or handwriting. Please take a clearer photo.")
        }

        // Fast pixel check for completely blank / pitch-black / solid images
        val width = bitmap.width
        val height = bitmap.height
        val stepX = max(1, width / 20)
        val stepY = max(1, height / 20)

        var totalSamples = 0
        var darkCount = 0
        var whiteCount = 0

        for (x in 0 until width step stepX) {
            for (y in 0 until height step stepY) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

                if (luminance < 15) darkCount++
                if (luminance > 245) whiteCount++
                totalSamples++
            }
        }

        if (totalSamples > 0) {
            if (darkCount.toFloat() / totalSamples > 0.95f) {
                return Pair(false, "The captured photo is too dark or underexposed to read. Please ensure adequate lighting.")
            }
            if (whiteCount.toFloat() / totalSamples > 0.98f) {
                return Pair(false, "The captured photo is completely overexposed or washed out. Please retake the photo.")
            }
        }

        return Pair(true, null)
    }

    /**
     * Compresses the bitmap to JPEG at 85 quality and returns base64.
     */
    fun compressToBase64(bitmap: Bitmap, quality: Int = JPEG_QUALITY): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing bitmap to base64: ${e.message}")
            null
        }
    }

    private fun applyExifRotation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to rotate bitmap: ${e.message}")
            bitmap
        }
    }
}
