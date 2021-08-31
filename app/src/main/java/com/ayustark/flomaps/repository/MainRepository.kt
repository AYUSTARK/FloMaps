//
//Programmed by Ayustark
//
package com.ayustark.flomaps.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ayustark.flomaps.Api.ApiHelper
import com.ayustark.flomaps.Models.LoginModel
import com.ayustark.flomaps.utils.Resource

class MainRepository(
    private val apiHelper: ApiHelper
) {
    fun login(login: LoginModel, user: MutableLiveData<Resource<Boolean>>) {
        Log.e("LOGIN","MR Login")
        return apiHelper.login(login, user)
    }
}