package com.embasa.plcounter.camera

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max


object ImagePreparer {

    class Prepared(val bitmap: Bitmap, val jpegBytes: ByteArray)

    fun prepare(
        resolver: ContentResolver,
        uri: Uri,
        maxSide: Int = 2000,
        quality: Int = 90,
    ): Prepared {
        // 1. só as dimensões, sem carregar a imagem inteira
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { stream ->
            requireNotNull(stream) { "Não foi possível abrir a imagem" }
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Imagem inválida" }

        // 2. decodifica já reduzida por potência de 2 (economiza memória)
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri).use { stream ->
            requireNotNull(stream) { "Não foi possível abrir a imagem" }
            BitmapFactory.decodeStream(stream, null, options)
        } ?: throw IOException("Falha ao decodificar a imagem")

        // 3. rotação EXIF + ajuste final do tamanho
        val rotation = readRotation(resolver, uri)
        val longSide = max(decoded.width, decoded.height)
        val scale = if (longSide > maxSide) maxSide.toFloat() / longSide else 1f

        val finalBitmap = if (rotation == 0f && scale == 1f) {
            decoded
        } else {
            val matrix = Matrix().apply {
                postRotate(rotation)
                postScale(scale, scale)
            }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }

        val out = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Prepared(finalBitmap, out.toByteArray())
    }

    private fun readRotation(resolver: ContentResolver, uri: Uri): Float = try {
        resolver.openInputStream(uri)?.use { stream ->
            when (
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
    } catch (e: IOException) {
        0f
    }
}