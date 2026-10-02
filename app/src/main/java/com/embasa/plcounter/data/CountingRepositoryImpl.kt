package com.embasa.plcounter.data

import com.embasa.plcounter.data.api.CountingApi
import com.embasa.plcounter.data.api.toDomain
import com.embasa.plcounter.domain.CountingError
import com.embasa.plcounter.domain.CountingRepository
import com.embasa.plcounter.domain.model.SampleResult
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class CountingRepositoryImpl(private val api: CountingApi) : CountingRepository {

    override suspend fun countSample(imageBytes: ByteArray, fileName: String): Result<SampleResult> =
        try {
            val body = imageBytes.toRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("file", fileName, body)
            Result.success(api.count(part).toDomain())
        } catch (e: CancellationException) {
            throw e   // nunca engolir cancelamento de coroutine
        } catch (e: HttpException) {
            Result.failure(
                when (e.code()) {
                    400 -> CountingError.InvalidImage()
                    413 -> CountingError.FileTooLarge()
                    else -> CountingError.Server(e.code())
                }
            )
        } catch (e: IOException) {
            Result.failure(CountingError.Network(e))
        }
}