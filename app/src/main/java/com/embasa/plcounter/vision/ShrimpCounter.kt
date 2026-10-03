package com.embasa.plcounter.vision

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import kotlin.math.min
import kotlin.math.roundToInt

class ShrimpCounter(private val params: VisionParams = VisionParams()) {

    class Output(
        val imageWidth: Int,
        val imageHeight: Int,
        val scale: Double,
        val detections: List<Component>,
    )

    fun countJpeg(jpegBytes: ByteArray): Output = MatPool().use { pool ->
        val encoded = pool.track(MatOfByte())
        encoded.fromArray(*jpegBytes)
        val decoded = pool.track(Imgcodecs.imdecode(encoded, Imgcodecs.IMREAD_COLOR))
        require(!decoded.empty()) { "Arquivo não é uma imagem válida" }
        run(decoded, pool)
    }

    fun count(bgr: Mat): Output = MatPool().use { pool -> run(bgr, pool) }

    private fun run(image: Mat, pool: MatPool): Output {
        val origH = image.rows()
        val origW = image.cols()

        // 1. limita o tamanho de entrada
        val preScale = min(1.0, params.maxInputSide.toDouble() / maxOf(origH, origW))
        var work = Preprocessing.resize(image, preScale, pool)

        // 2. normaliza a escala
        val scale = Preprocessing.estimateScale(work, params, pool)
        work = Preprocessing.resize(work, scale, pool)

        // 3. segmentação, detecção e filtro
        val mask = Segmentation.segment(Preprocessing.grayBlur(work, params, pool), params, pool)
        val detections = Filtering.filter(Detection.detect(mask, pool), params)

        // 4. volta para as coordenadas da imagem de entrada
        val total = preScale * scale
        val f = 1.0 / total
        val original = detections.map {
            Component(
                x = (it.x * f).roundToInt(),
                y = (it.y * f).roundToInt(),
                width = (it.width * f).roundToInt(),
                height = (it.height * f).roundToInt(),
                area = (it.area * f * f).roundToInt(),
                centerX = it.centerX * f,
                centerY = it.centerY * f,
            )
        }
        return Output(origW, origH, total, original)
    }
}