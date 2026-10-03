package com.embasa.plcounter.data.local

import org.opencv.android.OpenCVLoader

object OpenCvInitializer {
    @Volatile
    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (!loaded) {
                check(OpenCVLoader.initLocal()) { "Não foi possível carregar o OpenCV" }
                loaded = true
            }
        }
    }
}