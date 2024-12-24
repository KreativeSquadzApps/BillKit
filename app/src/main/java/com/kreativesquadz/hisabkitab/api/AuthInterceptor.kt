package com.kreativesquadz.hisabkitab.api

import com.kreativesquadz.hisabkitab.Config
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.token
        if (token != null) {
            val originalRequest = chain.request()
            val builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("X-API-KEY", Config.API_Key)
            val newRequest = builder.build()
            return chain.proceed(newRequest)
        } else {
            return chain.proceed(chain.request())
        }
    }

    interface TokenProvider {
        val token: String?
    }
}

