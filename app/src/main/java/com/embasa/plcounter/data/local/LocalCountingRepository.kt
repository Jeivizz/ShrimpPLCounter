package com.embasa.plcounter.data.local

import com.embasa.plcounter.domain.CountingError
import com.embasa.plcounter.domain.CountingRepository
import com.embasa.plcounter.domain.model.Detection
import com.embasa.plcounter.domain.model.SampleResult
import com.embasa.plcounter.vision.ShrimpCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException


class LocalCountingRepository(
    private val counter: ShrimpCounter = ShrimpCounter(),
) : CountingRepository {

    override suspend fun countSample(imageBytes: ByteArray, fileName: String): Result<SampleResult> =
        withContext(Dispatchers.Default) {
            try {
                OpenCvInitializer.ensureLoaded()
                Result.success(counter.countJpeg(imageBytes).toDomain())
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                Result.failure(CountingError.InvalidImage())
            } catch (e: OutOfMemoryError) {
                Result.failure(CountingError.Processing(e))
            } catch (e: Exception) {
                Result.failure(CountingError.Processing(e))
            }
        }
}

private fun ShrimpCounter.Output.toDomain() = SampleResult(
    imageWidth = imageWidth,
    imageHeight = imageHeight,
    detections = detections.map {
        Detection(
            centerX = it.centerX.toFloat(),
            centerY = it.centerY.toFloat(),
            width = it.width,
            height = it.height,
        )
    },
)