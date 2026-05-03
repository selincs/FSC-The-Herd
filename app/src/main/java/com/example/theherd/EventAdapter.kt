package com.example.theherd

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.ImageButton
import android.view.ViewGroup
import android.view.LayoutInflater
class EventAdapter(
    private val events: MutableList<Pair<String, String>>,
    private val onEdit: (Pair<String, String>) -> Unit,
    private val onRsvp: (Pair<String, String>) -> Unit,
    private val onSend: (Pair<String, String>) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.eventDate)
        val name: TextView = view.findViewById(R.id.eventName)
        val edit: ImageButton = view.findViewById(R.id.editBtn)
        val rsvp: ImageButton = view.findViewById(R.id.rsvpBtn)
        val send: ImageButton = view.findViewById(R.id.sendBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {

        val (date, event) = events[position]

        holder.name.text = event
        holder.date.text = formatDate(date)

        holder.edit.setOnClickListener { onEdit(events[position]) }
        holder.rsvp.setOnClickListener { onRsvp(events[position]) }
        holder.send.setOnClickListener { onSend(events[position]) }
    }
    private fun formatDate(raw: String): String {
        val parts = raw.split("-")
        val year = parts[0]
        val month = parts[1].toInt()
        val day = parts[2]

        val monthName = java.time.Month.of(month)
            .name.lowercase()
            .replaceFirstChar { it.uppercase() }

        return "$monthName $day, $year"
    }

    override fun getItemCount(): Int = events.size
}