package com.example.submisionintermediate.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.submisionintermediate.R
import com.example.submisionintermediate.ViewModelFactory
import com.example.submisionintermediate.adapter.LoadingStateAdapter
import com.example.submisionintermediate.adapter.StoryAdapter
import com.example.submisionintermediate.auth.AuthActivity
import com.example.submisionintermediate.database.StoryDatabase
import com.example.submisionintermediate.databinding.ActivityMainBinding
import com.example.submisionintermediate.detail.DetailStoryActivity
import com.example.submisionintermediate.login.LoginActivity
import com.example.submisionintermediate.maps.MapsActivity
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.service.ListStoryItem
import com.example.submisionintermediate.story.AddStoryActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = obtainViewModel(this)
        val db = StoryDatabase.getDatabase(this)


        if (onLine(this)){
            mainViewModel.getUser().observe(this) { user ->
                Log.e("token", LoginActivity.token)
                if (user.token?.isEmpty() == true) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                LoginActivity.token = user.token.toString()
                showLoading()
                setStoriesData()
                hideLoading()

            }
        }else{
            val dao = db.storyDao()
            lifecycleScope.launch{
                dao.getAllStoryOffline().collect {
                    listStories.addAll(it)
                }
            }
        }

        binding.ivAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }

        binding.fabMapStory.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    fun onLine(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun obtainViewModel(activity: AppCompatActivity): MainViewModel {
        val pref = UserPreference.getInstance(dataStore)
        return ViewModelProvider(activity, ViewModelFactory(pref, this))[MainViewModel::class.java]
    }

    private fun setStoriesData(){
        storiesAdapter = StoryAdapter {
            moveIntent(it)
        }

        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = storiesAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                storiesAdapter.retry()
            }
        )
        mainViewModel.listStories.observe(this) {
            storiesAdapter.submitData(lifecycle, it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        showLoading()
        setStoriesData()
        hideLoading()
        if (AddStoryActivity.upload){
            listStories.clear()
            mainViewModel.getStoryPaging().observe(this){
                storiesAdapter.submitData(lifecycle, it)
            }
            storiesAdapter.notifyDataSetChanged()
        }
        AddStoryActivity.upload = false
    }

    private fun moveIntent(story: ListStoryItem){
        val data = getStoryByName(story)

        val moveWithObjectIntent = Intent(this@MainActivity, DetailStoryActivity::class.java)
        moveWithObjectIntent.putExtra(DetailStoryActivity.EXTRA_STORY, data)
        startActivity(moveWithObjectIntent)
    }

    private fun getStoryByName(data: ListStoryItem): ListStoryItem? {
        var selectedStory: ListStoryItem? = null

        for(story in listStories) {
            if(story.name == data.name && story.photoUrl == data.photoUrl
                && story.description == data.description)
                selectedStory = story
        }

        return selectedStory
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu1 -> {
                mainViewModel.logout()
                return true
            }
            R.id.menu2 -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                return true
            }
            else -> return true
        }
    }

    private fun showLoading() {
        binding.apply {
            rvStories.visibility = View.GONE
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.showShimmer(true)
        }
    }

    private fun hideLoading() {
        binding.apply {
            shimmerLayout.stopShimmer()
            rvStories.visibility = View.VISIBLE
            shimmerLayout.visibility = View.GONE
        }
    }

    companion object {
        var listStories : ArrayList<ListStoryItem> = ArrayList()
        lateinit var mainViewModel: MainViewModel
        lateinit var storiesAdapter: StoryAdapter
    }

}