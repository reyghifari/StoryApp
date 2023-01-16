package com.example.submisionintermediate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.submisionintermediate.databinding.StoryRowItemBinding
import com.example.submisionintermediate.service.ListStoryItem

class StoryAdapter(private val detailListener: (data: ListStoryItem) -> Unit) :
    PagingDataAdapter<ListStoryItem, StoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            StoryRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
            holder.binding.cardView.setOnClickListener {
                detailListener.invoke(data)
            }
            imgID = holder.binding.cardView
        }
    }

    class MyViewHolder(val binding: StoryRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStoryItem) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(data.photoUrl)
                    .into(imgItemImage)
                tvName.text = data.name
                tvDescription.text = data.description
                tvCreatedTime.text = data.createdAt
            }
        }
    }

        companion object {
            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
                override fun areItemsTheSame(
                    oldItem: ListStoryItem,
                    newItem: ListStoryItem
                ): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(
                    oldItem: ListStoryItem,
                    newItem: ListStoryItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }
            }
            lateinit var imgID: CardView
        }
}
