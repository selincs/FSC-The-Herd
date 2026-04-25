package com.example.theherd

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class FriendsAdapter(
    private val friendsList: MutableList<Friend>,
    private val onRemoveClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val tvStatus: TextView = view.findViewById(R.id.tvFriendStatus)
        val statusDot: View = view.findViewById(R.id.onlineStatusDot)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveFriend)
        val btnAdd: ImageButton = view.findViewById(R.id.btnAddFriend)
        val btnMessage: ImageButton = view.findViewById(R.id.btnMessage)
        val profileImage: ImageView = view.findViewById(R.id.imgProfilePic)
        val root: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]
        val context = holder.itemView.context

        //Is this GUI entry a Friend item or a Friend Request?
        val isRequest = !friend.isFriend


        holder.tvName.text = friend.name
        holder.tvStatus.text = friend.statusText

        val dotColor = if (friend.isOnline) R.color.colorAccent else R.color.textSecondary
        holder.statusDot.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, dotColor)
        )
//
//        if (friend.statusText == "Pending Request") {
//            holder.btnMessage.visibility = View.GONE
//        } else {
//            holder.btnMessage.visibility = View.VISIBLE
//        }

        if (isRequest) {
            holder.btnAdd.visibility = View.VISIBLE     // Accept
            holder.btnRemove.visibility = View.VISIBLE  // Reject
            holder.btnMessage.visibility = View.GONE    // No messaging yet
        } else {
            holder.btnAdd.visibility = View.GONE        // Not needed
            holder.btnRemove.visibility = View.VISIBLE  // Remove friend
            holder.btnMessage.visibility = View.VISIBLE // Message
        }

        fun openProfile() {
            val intent = Intent(context, FriendProfileActivity::class.java).apply {
                putExtra("FRIEND_NAME", friend.name)
                putExtra("IS_FRIEND", friend.isFriend)
                putExtra("USERNAME", "@${friend.name.replace(" ", "_").lowercase()}")
                putExtra("GRAD_YEAR", "2026")
            }
            context.startActivity(intent)
        }

        holder.root.setOnClickListener { openProfile() }
        holder.profileImage.setOnClickListener { openProfile() }

        //TODO: What is btnMessage?
        holder.btnMessage.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("FRIEND_NAME", friend.name)
            context.startActivity(intent)
        }

        holder.btnRemove.setOnClickListener {
            //TODO: Remove Friend in Firestore logic here
            showDeleteConfirmation(context, friend, holder.bindingAdapterPosition)
        }
        holder.btnAdd.setOnClickListener {
            //TODO: Add Friend in Firestore logic here
         println("Add Friend button pressed")
        }
    }

    override fun getItemCount() = friendsList.size

    private fun showDeleteConfirmation(context: android.content.Context, friend: Friend, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove ${friend.name} from your herd?")
            .setPositiveButton("Remove") { _, _ ->
                if (position != RecyclerView.NO_POSITION && position < friendsList.size) {
                    val friendToRemove = friendsList[position]
                    onRemoveClick(friendToRemove)
                    friendsList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, friendsList.size)
                    Toast.makeText(context, "${friendToRemove.name} removed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun updateList(newList: List<Friend>) {
        friendsList.clear()
        friendsList.addAll(newList)
        notifyDataSetChanged()
    }
}