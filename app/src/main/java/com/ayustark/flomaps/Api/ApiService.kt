//
//Programmed by Ayustark
//
package com.ayustark.flomaps.Api

import com.ayustark.flomaps.Models.LoginModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/login")
    fun login(@Body user:LoginModel): Call<Map<String, Boolean>>
}