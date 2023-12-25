package com.med.drawing.my_creation.presentation.my_creation_list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.med.drawing.databinding.ActivityMyCreationBinding
import com.med.drawing.my_creation.presentation.my_creation_list.adapter.MyCreationListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class MyCreationListActivity : AppCompatActivity() {

    private val myCreationListViewModel: MyCreationListViewModel by viewModels()

    private lateinit var myCreationListState: MyCreationListState
    private lateinit var binding: ActivityMyCreationBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCreationBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)


        lifecycleScope.launch {
            myCreationListViewModel.myCreationState.collect {
                myCreationListState = it

                if (myCreationListState.creationList.isNotEmpty()) {
                    val myCreationListAdapter = MyCreationListAdapter(
                        this@MyCreationListActivity, myCreationListState.creationList
                    )
                    myCreationListAdapter.setClickListener(object : MyCreationListAdapter.ClickListener {
                        override fun oClick(imagePosition: Int) {

                        }
                    })

                    binding.recyclerView.layoutManager =
                        GridLayoutManager(this@MyCreationListActivity, 2)
                    binding.recyclerView.adapter = myCreationListAdapter
                }
            }
        }

    }

}















