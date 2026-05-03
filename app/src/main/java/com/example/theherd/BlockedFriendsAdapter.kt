package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BlockedFriendsAdapter(
    private val blockedList: MutableList<Friend>,
    private val onUnblockClick: (Friend) -> Unit
) : RecyclerView.Adapter<BlockedFriendsAdapter.BlockedViewHolder>() {

    class BlockedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val profileImage: ImageView = view.findViewById(R.id.imgProfilePic)
        val btnUnblock: Button = view.findViewById(R.id.btnUnblock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
        return BlockedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val friend = blockedList[position]
        holder.tvName.text = friend.name

        holder.btnUnblock.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                val friendToUnblock = blockedList[currentPos]
                onUnblockClick(friendToUnblock)
                blockedList.removeAt(currentPos)
                notifyItemRemoved(currentPos)
                notifyItemRangeChanged(currentPos, blockedList.size)
            }
        }
    }
    override fun getItemCount() = blockedList.size
}