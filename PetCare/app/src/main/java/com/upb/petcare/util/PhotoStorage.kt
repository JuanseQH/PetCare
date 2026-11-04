package com.upb.petcare.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Copia la foto elegida en la galería al almacenamiento interno de la app,
 * reducida a máximo 1024 px para no ocupar espacio de más.
 */
object PhotoStorage {
    private const val MAX_SIZE = 1024

    suspend fun save(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= MAX_SIZE && bounds.outHeight / (sample * 2) >= MAX_SIZE) sample *= 2

            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null

            // Respetar la orientación con la que se tomó la foto
            val rotation = resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val rotated = if (rotation == 0f) bitmap else
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(rotation) }, true)

            val dir = File(context.filesDir, "pets").apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { rotated.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            file.absolutePath
        }.getOrNull()
    }
}
