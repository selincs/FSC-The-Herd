package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityAdapter(
    private val communities: List<Community>,
    private val onCommunityClick: (Community) -> Unit,
    private val onJoinCLick: (Community)  -> Unit
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.communityNameText)
        val descText: TextView = view.findViewById(R.id.communityDescText)
        val btnJoin: Button = view.findViewById(R.id.btnJoinCommunity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_community, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val community = communities[position]

        holder.nameText.text = community.name
        holder.descText.text = community.description

        updateJoinButtonUI(holder.btnJoin, community.isJoined)

        holder.btnJoin.setOnClickListener {
            onJoinCLick(community)
        }

        holder.itemView.setOnClickListener {
            onCommunityClick(community)
        }
    }

    override fun getItemCount() = communities.size

    private fun updateJoinButtonUI(button: Button, isJoined: Boolean) {
        if (isJoined) {
            button.text = "Joined"
            button.setBackgroundColor(android.graphics.Color.parseColor("#2F442F"))
            button.setTextColor(android.graphics.Color.WHITE)
        } else {
            button.text = "Join"
            button.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            button.setTextColor(android.graphics.Color.parseColor("#2F442F"))
        }
    }
}