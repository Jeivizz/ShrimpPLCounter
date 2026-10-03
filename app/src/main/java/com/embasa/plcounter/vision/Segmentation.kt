package com.embasa.plcounter.vision

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object Segmentation {

    fun segment(blurredGray: Mat, params: VisionParams, pool: MatPool): Mat {
        val binary = pool.mat()
        Imgproc.adaptiveThreshold(
            blurredGray,
            binary,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            params.thresholdBlock,
            params.thresholdC,
        )

        val k = params.openKernel.toDouble()
        val kernel = pool.track(Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(k, k)))

        val cleaned = pool.mat()
        Imgproc.morphologyEx(binary, cleaned, Imgproc.MORPH_OPEN, kernel)
        return cleaned
    }
}