package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendSelectAdapter(
    private val friends: List<Friend>,
    private val onInviteClicked: (Friend) -> Unit
) : RecyclerView.Adapter<FriendSelectAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgFriendAvatar)
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val tvUsername: TextView = view.findViewById(R.id.tvFriendUsername)
        val btnInvite: Button = view.findViewById(R.id.btnInvite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_select, parent, false)

        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]

        holder.tvName.text = friend.name
        holder.tvUsername.text = "@${friend.username}"

        //Avatars - set to default for now
//        holder.imgAvatar.setImageResource(R.drawable.avatar_1)
        holder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round)

        holder.btnInvite.setOnClickListener {
            onInviteClicked(friend)
        }
    }

    override fun getItemCount(): Int = friends.size
}