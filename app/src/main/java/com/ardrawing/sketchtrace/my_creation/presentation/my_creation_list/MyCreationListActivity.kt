package com.ardrawing.sketchtrace.my_creation.presentation.my_creation_list

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ardrawing.sketchtrace.databinding.ActivityMyCreationLsitBinding
import com.ardrawing.sketchtrace.my_creation.presentation.my_creation_details.MyCreationDetailsActivity
import com.ardrawing.sketchtrace.my_creation.presentation.my_creation_list.adapter.MyCreationListAdapter
import com.ardrawing.sketchtrace.util.ads.InterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.ardrawing.sketchtrace.util.LanguageChanger
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class MyCreationListActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    private val myCreationListViewModel: MyCreationListViewModel by viewModels()

    private lateinit var myCreationListState: MyCreationListState
    private lateinit var binding: ActivityMyCreationLsitBinding

    private var myCreationListAdapter: MyCreationListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityMyCreationLsitBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.back.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            myCreationListViewModel.myCreationState.collect {
                myCreationListState = it
                myCreationListAdapter?.notifyDataSetChanged()
                Log.d("tag_creation", "onCreate: ${myCreationListState.creationList.size}")
            }
        }

        myCreationListAdapter = MyCreationListAdapter(
            this, myCreationListState.creationList
        )

        myCreationListAdapter?.setClickListener(object :
            MyCreationListAdapter.ClickListener {
            override fun oClick(uri: String, isVideo: Boolean) {

                InterManager.showInterstitial(
                    this@MyCreationListActivity,
                    object : InterManager.OnAdClosedListener {
                        override fun onAdClosed() {
                            Intent(
                                this@MyCreationListActivity,
                                MyCreationDetailsActivity::class.java
                            ).also { intent ->
                                intent.putExtra("uri", uri)
                                intent.putExtra("isVideo", isVideo)
                                startActivity(intent)
                                finish()
                            }
                        }
                    })
            }
        })

        binding.recyclerView.layoutManager =
            GridLayoutManager(this@MyCreationListActivity, 2)
        binding.recyclerView.adapter = myCreationListAdapter

    }

}















