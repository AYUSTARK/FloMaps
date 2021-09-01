//
//Programmed by Ayustark
//
package com.ayustark.flomaps.data.Api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiManager {
    private const val loginLink = "https://flo-app-api.herokuapp.com/"
    val apiService:ApiService

    init {
        val retro = Retrofit.Builder()
            .baseUrl(loginLink)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        apiService = retro.create(ApiService::class.java)
    }
}