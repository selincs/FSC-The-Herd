package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theherd.R
import Model.Guide

class GuidesAdapter(private val onClick: (Guide) -> Unit) :
    ListAdapter<Guide, GuidesAdapter.GuideViewHolder>(GuideDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.guide_card, parent, false)
        return GuideViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GuideViewHolder(view: View, val onClick: (Guide) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.tvGuideTitle)
        private val desc: TextView = view.findViewById(R.id.tvGuideDesc)
        private val badge: TextView = view.findViewById(R.id.tvStatusBadge)
        private val icon: ImageView = view.findViewById(R.id.ivGuideIcon)
        private var currentGuide: Guide? = null

        init {
            view.setOnClickListener {
                currentGuide?.let { onClick(it) }
            }
        }

        fun bind(guide: Guide) {
            currentGuide = guide
            title.text = guide.title
            desc.text = guide.description

            when {
                guide.isVerified -> {
                    badge.visibility = View.VISIBLE
                    badge.text = "✔ Verified"
                    badge.setBackgroundResource(R.drawable.bg_badge_verified)
                }
                guide.isUserSuggested -> {
                    badge.visibility = View.VISIBLE
                    badge.text = "👤 Community Suggested"
                    badge.setBackgroundResource(R.drawable.bg_user_suggested)
                }
                else -> {
                    badge.visibility = View.GONE
                }
            }

        }
    }
}


class GuideDiffCallback : DiffUtil.ItemCallback<Guide>() {
    override fun areItemsTheSame(oldItem: Guide, newItem: Guide): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Guide, newItem: Guide): Boolean = oldItem == newItem
}