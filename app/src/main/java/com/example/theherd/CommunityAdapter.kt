package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityAdapter(
    private val communities: List<Community>,
    private val onClick: (Community) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.communityNameText)
        val descText: TextView = view.findViewById(R.id.communityDescText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_community, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val community = communities[position]
        val context = holder.itemView.context

        holder.nameText.text = community.name
        holder.descText.text = community.description

        val btnJoin = holder.itemView.findViewById<Button>(R.id.btnJoinCommunity)
        updateJoinButtonUI(btnJoin, community.isJoined)

        btnJoin.setOnClickListener {
            community.isJoined = !community.isJoined
            updateJoinButtonUI(btnJoin, community.isJoined)
            PreferencesManager.saveAllCommunities(context, ArrayList(communities))
            val currentJoinedNames = communities
                .filter { it.isJoined }
                .map { it.name }
                .toSet()
            PreferencesManager.saveJoinedClubs(context, currentJoinedNames)
        }

        holder.itemView.setOnClickListener {
            onClick(community)
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