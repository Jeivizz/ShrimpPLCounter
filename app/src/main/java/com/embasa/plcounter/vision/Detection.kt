package com.embasa.plcounter.vision

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object Detection {

    fun detect(mask: Mat, pool: MatPool): List<Component> {
        val labels = pool.mat()
        val stats = pool.mat()
        val centroids = pool.mat()
        val n = Imgproc.connectedComponentsWithStats(mask, labels, stats, centroids, 8, CvType.CV_32S)
        if (n <= 1) return emptyList()

        val s = IntArray(n * 5)
        stats.get(0, 0, s)
        val c = DoubleArray(n * 2)
        centroids.get(0, 0, c)

        return (1 until n).map { i ->
            Component(
                x = s[i * 5 + Imgproc.CC_STAT_LEFT],
                y = s[i * 5 + Imgproc.CC_STAT_TOP],
                width = s[i * 5 + Imgproc.CC_STAT_WIDTH],
                height = s[i * 5 + Imgproc.CC_STAT_HEIGHT],
                area = s[i * 5 + Imgproc.CC_STAT_AREA],
                centerX = c[i * 2],
                centerY = c[i * 2 + 1],
            )
        }
    }
}