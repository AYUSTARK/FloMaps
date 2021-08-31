package com.ayustark.flomaps.viewmodel

import android.util.Log
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayustark.flomaps.Models.LoginModel
import com.ayustark.flomaps.repository.MainRepository
import com.ayustark.flomaps.utils.Resource
import kotlinx.coroutines.launch

class MainViewModel(private val mainRepository: MainRepository) :
    ViewModel(), Observable {

    private fun login(login: LoginModel, user: MutableLiveData<Resource<Boolean>>) {
        viewModelScope.launch {
            mainRepository.login(login, user)
        }
    }

    fun getLogin(login: LoginModel): LiveData<Resource<Boolean>> {
        val user = MutableLiveData<Resource<Boolean>>()
        Log.e("LOGIN", "MVM Login")
        user.postValue(Resource.loading(true))
        login(login, user)
        return user
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    }

}