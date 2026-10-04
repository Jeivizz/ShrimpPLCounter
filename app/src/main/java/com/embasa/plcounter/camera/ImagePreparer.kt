package com.embasa.plcounter.camera

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


object ImagePreparer {

    class Prepared(val bitmap: Bitmap, val jpegBytes: ByteArray)

    fun prepare(
        resolver: ContentResolver,
        uri: Uri,
        maxSide: Int = 2000,
        quality: Int = 90,
        circularMask: Boolean = false,
    ): Prepared {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { stream ->
            requireNotNull(stream) { "Não foi possível abrir a imagem" }
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Imagem inválida" }

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri).use { stream ->
            requireNotNull(stream) { "Não foi possível abrir a imagem" }
            BitmapFactory.decodeStream(stream, null, options)
        } ?: throw IOException("Falha ao decodificar a imagem")


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

        // Foto da câmera: recorta o quadrado do círculo guia e deixa tudo fora dele preto.
        val result = if (circularMask) applyCircularMask(finalBitmap, CameraGuide.DIAMETER_FRACTION) else finalBitmap

        val out = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Prepared(result, out.toByteArray())
    }


    private fun applyCircularMask(src: Bitmap, fraction: Float): Bitmap {
        val side = (min(src.width, src.height) * fraction).roundToInt().coerceAtLeast(1)
        val left = (src.width - side) / 2
        val top = (src.height - side) / 2

        val out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.BLACK)
        val circle = Path().apply { addCircle(side / 2f, side / 2f, side / 2f, Path.Direction.CW) }
        canvas.clipPath(circle)
        canvas.drawBitmap(src, -left.toFloat(), -top.toFloat(), null)
        return out
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