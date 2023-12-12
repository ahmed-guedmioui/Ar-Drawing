package com.med.drawing.core.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.R
import com.med.drawing.util.AppDataResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {


    private val splashViewModel: SplashViewModel by viewModels()
    private lateinit var splashState: SplashState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            splashViewModel.splashState.collect { splashState = it }
        }

        lifecycleScope.launch {
            splashViewModel.appDataResultChannel.collect { result ->
                when (result) {
                    is AppDataResult.Error -> {
                        Toast.makeText(
                            this@SplashActivity, "Error", Toast.LENGTH_SHORT
                        ).show()
                    }

                    is AppDataResult.Loading -> {

                    }

                    is AppDataResult.Success -> {
                        Toast.makeText(
                            this@SplashActivity, "Success", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }


}





















