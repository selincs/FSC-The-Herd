package com.example.theherd

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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

        //isRequest == Is this GUI entry a Friend or a Friend Request?
        val isRequest = !friend.isFriend


        holder.tvName.text = friend.name
        holder.tvStatus.text = friend.statusText

        val dotColor = if (friend.isOnline) R.color.colorAccent else R.color.textSecondary
        holder.statusDot.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, dotColor)
        )

        //Sets background color of the friend request entry, only for Requests atm. Can differentiate mentors here.
        val bgView = holder.itemView.findViewById<View>(R.id.requestBackground)
        if (isRequest) {
            val color = if (friend.isIncoming) {
                Color.parseColor("#1A4CAF50") // subtle green
            } else {
                Color.parseColor("#1A2196F3") // subtle blue
            }
            bgView.setBackgroundColor(color)
        } else {
            bgView.setBackgroundColor(Color.TRANSPARENT)
        }

        if (!isRequest) {
            // FRIEND (Remove or Message buttons)
            holder.btnAdd.visibility = View.GONE
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnMessage.visibility = View.VISIBLE

        } else if (friend.isIncoming) {
            // INCOMING FRIEND REQUEST (Accept or Reject buttons)
            holder.btnAdd.visibility = View.VISIBLE   // Accept
            holder.btnRemove.visibility = View.VISIBLE // Reject
            holder.btnMessage.visibility = View.GONE

        } else {
            // OUTGOING FRIEND REQUEST (Enable Cancel button)
            holder.btnAdd.visibility = View.GONE      // No accept button on outgoing requests
            holder.btnRemove.visibility = View.VISIBLE // Cancel request allowed
            holder.btnRemove.setImageResource(R.drawable.ic_delete) //Change btnRemove icon to trash can
            holder.btnMessage.visibility = View.GONE    //no messaging without friendship
        }

        fun openProfile() {
            val intent = Intent(context, FriendProfileActivity::class.java).apply {
                putExtra("FRIEND_NAME", friend.name)
                putExtra("FRIEND_ID", friend.id)
                putExtra("IS_FRIEND", friend.isFriend)
                putExtra("USERNAME", "@${friend.name.replace(" ", "_").lowercase()}")
                putExtra("GRAD_YEAR", "2026")
                putExtra("IS_INCOMING", friend.isIncoming)
                putExtra("IS_PENDING", !friend.isIncoming)
            }
            context.startActivity(intent)
        }

        holder.root.setOnClickListener { openProfile() }
        holder.profileImage.setOnClickListener { openProfile() }

        //TODO: What is btnMessage? Made some edits to make it message button, unimplemented
        holder.btnMessage.setOnClickListener {
            Toast.makeText(context, "Message your friend!", Toast.LENGTH_SHORT).show()
            //commented until messageactivity impl
//            val intent = Intent(context, MessageActivity::class.java)
//            intent.putExtra("FRIEND_NAME", friend.name)
//            context.startActivity(intent)
        }

        //Button Remove logic
        holder.btnRemove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            when {
                !isRequest -> {
                    // if !isRequest, the user is already a FRIEND -> show delete confirmation dialog
                    showDeleteConfirmation(context, friend, pos)
                }

                friend.isIncoming -> {
                    // INCOMING REQUEST -> reject (no confirmation)
                    FriendsRepository.rejectFriendRequest(friend.id) { success ->
                        if (success) {
                            Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show()
                            friendsList.removeAt(pos)
                            notifyItemRemoved(pos)
                        } else {
                            Toast.makeText(context, "Failed to reject request", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                else -> {
                    // OUTGOING REQUEST -> cancel (no confirmation)
                    FriendsRepository.cancelFriendRequest(friend.id) { success ->
                        if (success) {
                            Toast.makeText(context, "Request cancelled", Toast.LENGTH_SHORT).show()
                            friendsList.removeAt(pos)
                            notifyItemRemoved(pos)
                        } else {
                            Toast.makeText(context, "Failed to cancel request", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        holder.btnAdd.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            if (isRequest && friend.isIncoming) {
                FriendsRepository.acceptFriendRequest(friend.id) { success ->
                    if (success) {
                        Toast.makeText(context, "Friend added!", Toast.LENGTH_SHORT).show()
                        friendsList.removeAt(pos)
                        notifyItemRemoved(pos)
                    } else {
                        Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = friendsList.size

    private fun showDeleteConfirmation(context: android.content.Context, friend: Friend, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove ${friend.name} from your herd?")
            .setPositiveButton("Remove") { _, _ ->

                FriendsRepository.removeFriend(friend.id) { success ->
                    if (success) {
                        if (position != RecyclerView.NO_POSITION && position < friendsList.size) {
                            friendsList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, friendsList.size)
                        }

                        Toast.makeText(context, "${friend.name} removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to remove friend", Toast.LENGTH_SHORT)
                            .show()
                    }
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