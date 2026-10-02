package com.embasa.plcounter.data.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface CountingApi {
    @Multipart
    @POST("api/v1/count")
    suspend fun count(
        @Part file: MultipartBody.Part,
        @Query("include_image") includeImage: Boolean = false,
    ): CountingResponseDto
}