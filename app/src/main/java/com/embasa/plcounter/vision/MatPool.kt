package com.embasa.plcounter.vision

import org.opencv.core.Mat

class MatPool : AutoCloseable {
    private val mats = ArrayList<Mat>()

    fun mat(): Mat = Mat().also { mats.add(it) }

    fun <T : Mat> track(mat: T): T {
        mats.add(mat)
        return mat
    }

    override fun close() {
        mats.forEach { it.release() }
        mats.clear()
    }
}