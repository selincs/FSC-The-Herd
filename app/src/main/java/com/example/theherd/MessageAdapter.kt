package com.example.theherd

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(
    private val messageList: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_THEM = 2
        private const val VIEW_TYPE_EVENT_INVITE = 3
    }

    override fun getItemViewType(position: Int): Int {

        val msg = messageList[position]

        // ----------------------------------------
        // EVENT INVITE MESSAGE
        // ----------------------------------------
        if (msg.type == "event_invite") {
            return VIEW_TYPE_EVENT_INVITE
        }

        // ----------------------------------------
        // NORMAL TEXT MESSAGE
        // ----------------------------------------
        return if (msg.senderId == SessionManager.requireUserId()) {
            VIEW_TYPE_ME
        } else {
            VIEW_TYPE_THEM
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return when (viewType) {

            VIEW_TYPE_ME -> {

                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)

                SentViewHolder(view)
            }

            VIEW_TYPE_THEM -> {

                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)

                ReceivedViewHolder(view)
            }

            VIEW_TYPE_EVENT_INVITE -> {

                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_event_invite, parent, false)

                EventInviteViewHolder(view)
            }

            else -> {

                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)

                ReceivedViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        val message = messageList[position]

        when (holder) {

            is SentViewHolder -> holder.bind(message)

            is ReceivedViewHolder -> holder.bind(message)

            is EventInviteViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messageList.size

    // =========================================================
    // SENT TEXT MESSAGE VIEW HOLDER
    // =========================================================
    class SentViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val text: TextView =
            view.findViewById(R.id.tvMessageText)

        fun bind(msg: Message) {

            text.text = msg.text
        }
    }

    // =========================================================
    // RECEIVED TEXT MESSAGE VIEW HOLDER
    // =========================================================
    class ReceivedViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val text: TextView =
            view.findViewById(R.id.tvMessageText)

        fun bind(msg: Message) {
            text.text = msg.text
        }
    }

    // =========================================================
    // EVENT INVITE VIEW HOLDER
    // =========================================================
    class EventInviteViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val tvEventName: TextView =
            view.findViewById(R.id.tvInviteEventName)

        private val tvEventTime: TextView =
            view.findViewById(R.id.tvInviteEventTime)

        private val tvEventLocation: TextView =
            view.findViewById(R.id.tvInviteEventLocation)

        private val btnViewEvent: Button =
            view.findViewById(R.id.btnViewEvent)

        private val btnRsvp: Button =
            view.findViewById(R.id.btnRsvpInvite)

        fun bind(msg: Message) {

            // ----------------------------------------
            // Populate event info
            // ----------------------------------------
            tvEventName.text = msg.eventName ?: "Unknown Event"

            tvEventTime.text =
                "🕒 ${msg.eventTime ?: "Unknown Time"}"

            tvEventLocation.text =
                "📍 ${msg.eventLocation ?: "Unknown Location"}"

            // ----------------------------------------
            // VIEW EVENT BUTTON
            // ----------------------------------------
            btnViewEvent.setOnClickListener {

                Toast.makeText(
                    itemView.context,
                    "Opening Event...",
                    Toast.LENGTH_SHORT
                ).show()

                /*
                FUTURE:
                Open TopicDetailActivity

                Example:
                val intent = Intent(
                    itemView.context,
                    TopicDetailActivity::class.java
                )

                intent.putExtra("EVENT_ID", msg.eventId)
                intent.putExtra("TOPIC_ID", msg.topicId)

                itemView.context.startActivity(intent)
                 */
            }

            // ----------------------------------------
            // INITIAL RSVP BUTTON STATE
            // ----------------------------------------
            if (
                msg.topicId != null &&
                msg.eventId != null
            ) {

                EventRepository.isUserRsvpd(
                    msg.topicId,
                    msg.eventId
                ) { isRsvpd ->

                    btnRsvp.text =
                        if (isRsvpd)
                            "Remove RSVP"
                        else
                            "RSVP"
                }
            }

            // ----------------------------------------
            // RSVP BUTTON
            // ----------------------------------------
            btnRsvp.setOnClickListener {

                val event = Event(
                    id = msg.eventId ?: "",
                    name = msg.eventName ?: "",
                    time = msg.eventTime ?: "",
                    location = msg.eventLocation ?: "",
                    date = msg.eventDate ?: "",
                    topicId = msg.topicId ?: ""
                )

                println("msgID=${msg.eventId}")
                println("EventName=${msg.eventName}")
                println("TopicID=${msg.topicId}")

                EventRepository.toggleRsvp(event) { success, isNowRsvpd ->
                    if (success) {
                        val text =
                            if (isNowRsvpd)
                                "RSVP successful"
                            else
                                "RSVP removed"

                        Toast.makeText(itemView.context,text,Toast.LENGTH_SHORT).show()

                        // UI update
                        btnRsvp.text =
                            if (isNowRsvpd) "Remove RSVP"
                            else "RSVP"

                    } else {
                        Toast.makeText(itemView.context,"Failed to RSVP",Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }
}