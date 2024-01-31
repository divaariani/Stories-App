package com.dicoding.aplikasiku.storyyapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.aplikasiku.storyyapp.R
import com.dicoding.aplikasiku.storyyapp.adapter.StoryAdapter
import com.dicoding.aplikasiku.storyyapp.databinding.ActivityMainBinding
import com.dicoding.aplikasiku.storyyapp.utils.SessionManager
import androidx.activity.viewModels
import com.dicoding.aplikasiku.storyyapp.activity.vm.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels{
        MainViewModel.ViewModelFactory(this)
    }
    private lateinit var sharedPref: SessionManager
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = StoryAdapter()

        sharedPref = SessionManager(this)
        token = sharedPref.getToken

        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.setHasFixedSize(true)
        binding.rvStory.adapter = adapter
        binding.fab.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    AddStoryActivity::class.java
                )
            )
        }

        viewModel.story.observe(this){
            adapter.submitData(lifecycle, it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Log out?")
                    ?.setPositiveButton("Yes") { _, _ ->
                        sharedPref.clearData()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    ?.setNegativeButton("Cancel", null)
                val alert = alertDialog.create()
                alert.show()
                true
            }
            R.id.action_map -> {
                val i = Intent(this, MapsActivity::class.java)
                startActivity(i)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}