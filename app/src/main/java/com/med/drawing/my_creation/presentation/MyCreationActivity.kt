package com.med.drawing.my_creation.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.med.drawing.databinding.ActivityMyCreationBinding
import com.med.drawing.my_creation.presentation.adapter.MyCreationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class MyCreationActivity : AppCompatActivity() {

    private val myCreationViewModel: MyCreationViewModel by viewModels()

    private lateinit var myCreationState: MyCreationState
    private lateinit var binding: ActivityMyCreationBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCreationBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)


        lifecycleScope.launch {
            myCreationViewModel.myCreationState.collect {
                myCreationState = it

                if (myCreationState.creationList.isNotEmpty()) {
                    val myCreationAdapter = MyCreationAdapter(
                        this@MyCreationActivity, myCreationState.creationList
                    )
                    myCreationAdapter.setClickListener(object : MyCreationAdapter.ClickListener {
                        override fun oClick(imagePosition: Int) {

                        }
                    })

                    binding.recyclerView.layoutManager =
                        GridLayoutManager(this@MyCreationActivity, 2)
                    binding.recyclerView.adapter = myCreationAdapter
                }
            }
        }

    }

}















