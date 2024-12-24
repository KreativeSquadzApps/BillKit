package com.kreativesquadz.hisabkitab.utils.Glide

import com.kreativesquadz.hisabkitab.Config
import okhttp3.OkHttpClient
import okhttp3.Request

object GlideUtils {

    // Create a custom OkHttpClient with an Interceptor to add the API Key header
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest: Request = chain.request()
                val requestWithHeaders: Request = originalRequest.newBuilder()
                    .header("X-API-KEY", Config.API_Key) // Add your API key here
                    .build()
                chain.proceed(requestWithHeaders)
            }
            .build()
    }
}

