package com.ayustark.flomaps.ui.main.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ayustark.flomaps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }

}