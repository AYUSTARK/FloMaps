//
//Programmed by Ayustark
//
package com.ayustark.flomaps.Api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ayustark.flomaps.Models.LoginModel
import com.ayustark.flomaps.utils.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiHelper(private val apiManager: ApiManager) {

    fun login(login: LoginModel, user: MutableLiveData<Resource<Boolean>>) {
        try {
            user.postValue(Resource.loading(false))
            apiManager.apiService.login(login).enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    Log.d("Response", "${response.body()?.get("validUser")}")
                    user.postValue(Resource.success(response.body()?.get("validUser")))
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    Log.e("Retro Failure", "${t.message}")
                    user.postValue(Resource.error(t.message.toString(), null))
                }

            })
        } catch (error: Exception) {
            Log.e("Try Error", "${error.message}")
            user.postValue(Resource.error(error.message.toString(), null))
        }
    }
}