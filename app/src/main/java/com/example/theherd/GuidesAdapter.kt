package com.example.theherd

import Model.Guide
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class GuidesAdapter : ListAdapter<Guide, GuidesAdapter.GuideViewHolder>(GuideDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.guide_card, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GuideViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.tvGuideTitle)
        private val desc: TextView = view.findViewById(R.id.tvGuideDesc)
        private val badge: TextView = view.findViewById(R.id.tvStatusBadge)
        private val icon: ImageView = view.findViewById(R.id.ivGuideIcon)


        fun bind(guide: Guide) {
            title.text = guide.title
            desc.text = guide.description

            val iconResource = when (guide.category) {
                "Navigation" -> R.drawable.ic_navigation
                "Travel" -> R.drawable.ic_travel
                "Academic" -> R.drawable.ic_academic
                "Financial Aid" -> R.drawable.ic_financial_aid
                "Housing" -> R.drawable.ic_home
                "Clubs" -> R.drawable.ic_clubs
                "Health & Wellness" -> R.drawable.ic_health
                else -> R.drawable.ic_miscellaneous
            }

            icon.setImageResource(iconResource)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, GuideTemplateActivity::class.java)
                intent.putExtra("GUIDE_ID", guide.id)
                itemView.context.startActivity(intent)
            }

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