package com.embasa.plcounter.vision

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.sqrt

object Preprocessing {

    fun resize(src: Mat, scale: Double, pool: MatPool): Mat {
        if (scale == 1.0) return src
        val dst = pool.mat()
        val interp = if (scale > 1) Imgproc.INTER_CUBIC else Imgproc.INTER_AREA
        Imgproc.resize(src, dst, Size(), scale, scale, interp)
        return dst
    }

    fun grayBlur(bgr: Mat, params: VisionParams, pool: MatPool): Mat {
        val gray = pool.mat()
        Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY)
        val k = params.blurKernel.toDouble()
        val blurred = pool.mat()
        Imgproc.GaussianBlur(gray, blurred, Size(k, k), 0.0)
        return blurred
    }


    fun estimateScale(bgr: Mat, params: VisionParams, pool: MatPool): Double {
        val mask = Segmentation.segment(grayBlur(bgr, params, pool), params, pool)
        val areas = Detection.detect(mask, pool)
            .map { it.area }
            .filter { it in params.scaleMinArea..params.scaleMaxArea }
        if (areas.size < 5) return 1.0
        val s = sqrt(params.targetMedianArea / median(areas))
        return s.coerceIn(params.scaleMin, params.scaleMax)
    }
}
internal fun median(values: List<Int>): Double {
    val s = values.sorted()
    val n = s.size
    return if (n % 2 == 1) s[n / 2].toDouble() else (s[n / 2 - 1] + s[n / 2]) / 2.0
}