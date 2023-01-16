package com.example.submisionintermediate.detail

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.submisionintermediate.databinding.ActivityDetailStoryBinding
import com.example.submisionintermediate.service.ListStoryItem

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY) as ListStoryItem

        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailStory)
        binding.tvDetailName.text = story.name
        binding.tvDetailCreated.text = story.createdAt
        binding.tvDetailDesc.text = story.description

    }


    companion object {
        const val EXTRA_STORY = "story"
    }

}