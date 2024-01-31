package com.dicoding.aplikasiku.storyyapp.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.aplikasiku.storyyapp.databinding.ActivityDetailStoryBinding
import com.dicoding.aplikasiku.storyyapp.utils.Constant
import com.bumptech.glide.Glide
import com.dicoding.aplikasiku.storyyapp.model.ListStories

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var story: ListStories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Detail Story"
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        story = intent.getParcelableExtra(Constant.BUNDLE_STORY)!!
        setData()
    }

    private fun setData() {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description

        Glide.with(this)
            .load(story.photoUrl)
            .centerCrop()
            .skipMemoryCache(true)
            .into(binding.ivDetailPhoto)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }
}