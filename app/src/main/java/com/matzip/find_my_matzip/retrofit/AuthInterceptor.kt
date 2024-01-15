package com.matzip.find_my_matzip.retrofit

import com.matzip.find_my_matzip.utiles.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

//모든 api요청에 자동으로 토큰 세팅
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requeset = chain.request().newBuilder()
            .addHeader("Authorization", SharedPreferencesManager.getString("token","") ?: "")
            .build()

        return chain.proceed(requeset)
    }
}