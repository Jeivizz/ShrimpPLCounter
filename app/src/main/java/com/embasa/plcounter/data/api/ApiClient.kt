package com.embasa.plcounter.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    @Volatile
    var baseUrl: String = "http://192.168.0.4:8000/"
}

object ApiClient {
    fun create(baseUrl: String = ApiConfig.baseUrl): CountingApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)   // upload em rede fraca
            .readTimeout(90, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountingApi::class.java)
    }
}