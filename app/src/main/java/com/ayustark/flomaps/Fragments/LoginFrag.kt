package com.ayustark.flomaps.Fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ayustark.flomaps.Api.ApiHelper
import com.ayustark.flomaps.Api.ApiManager
import com.ayustark.flomaps.Models.LoginModel
import com.ayustark.flomaps.R
import com.ayustark.flomaps.databinding.FragmentLoginBinding
import com.ayustark.flomaps.utils.Resource
import com.ayustark.flomaps.utils.Status.*

class LoginFrag : Fragment() {
    private var binding: FragmentLoginBinding? = null
    private val bind get() = binding!!
    private lateinit var sharedP: SharedPreferences
    private var navigator: NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        sharedP = requireContext().getSharedPreferences("Login", MODE_PRIVATE)
        navigator = activity?.findNavController(R.id.navHost)
        if (sharedP.getBoolean("isLogged",false)){
            navigator?.navigate(R.id.loginToMapsFrag)
        }
        bind.btnLogin.setOnClickListener {
            if (bind.phoneLayout.editText?.text?.length == 10) {
                if (bind.otpLayout.editText?.text?.length == 6) {

                    login(
                        LoginModel(
                            bind.phoneLayout.editText?.text.toString(),
                            bind.otpLayout.editText?.text.toString()
                        )
                    )
                } else {
                    bind.otpLayout.error = "Incorrect OTP"
                }
            } else {
                bind.phoneLayout.error = "Enter 10 digit mobile number"
            }
        }
        return binding?.root
    }

    private fun login(loginModel: LoginModel) {
        if (loginModel.phoneNumber.isNotBlank() && loginModel.otp.isNotBlank()) {
            val user = MutableLiveData<Resource<Boolean>>()
            ApiHelper(ApiManager).login(loginModel, user)
            user.observe(viewLifecycleOwner, {
                when (it.status) {
                    SUCCESS -> {
                        if (it.data == true) {
                            showToast("Logged in Successfully")
                            sharedP.edit().putBoolean("isLogged",true).apply()
                            navigator?.navigate(R.id.loginToMapsFrag)
                        } else {
                            showToast("Invalid Credentials")
                        }
                    }
                    ERROR -> {
                        showToast(it.message.toString())
                    }
                    LOADING -> {

                    }
                }
            })
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}