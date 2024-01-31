package com.dicoding.aplikasiku.storyyapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.aplikasiku.storyyapp.databinding.ItemStoryBinding
import com.dicoding.aplikasiku.storyyapp.activity.DetailStoryActivity
import com.dicoding.aplikasiku.storyyapp.utils.Constant
import com.bumptech.glide.Glide
import com.dicoding.aplikasiku.storyyapp.model.ListStories

class StoryAdapter :
    PagingDataAdapter<ListStories, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoryAdapter.StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null){
            holder.bind(data)
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStories) {
            with(binding) {
                val name = story.name
                tvItemName.text = buildString {
                    append(name)
                }

                val desc = story.description
                tvItemDescription.text = buildString {
                    append(desc)
                }

                tvDate.text = story.createdAt.timeStamp()

                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .into(ivItemPhoto)
            }

            itemView.setOnClickListener {
                val intent = Intent(it.context, DetailStoryActivity::class.java)
                intent.putExtra(Constant.BUNDLE_STORY, story)

                // Transition animation here
                // Transition when clicking on the story list to story details
                itemView.context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(itemView.context as Activity).toBundle())
            }
        }
    }

    fun String.timeStamp(): String = substring(0, 10)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStories>() {
            override fun areItemsTheSame(oldItem: ListStories, newItem: ListStories): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStories, newItem: ListStories): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}