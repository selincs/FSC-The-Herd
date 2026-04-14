package com.example.theherd

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.time.LocalDate

class CalendarAdapter(
    private val context: Context,
    private val days: List<String>
) : BaseAdapter() {

    private var selectedPosition = -1

    override fun getCount(): Int = days.size

    override fun getItem(position: Int): Any = days[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_day, parent, false)

        val text = view.findViewById<TextView>(R.id.dayText)
        text.text = days[position]

        // highlight selected day
        if (position == selectedPosition) {
            text.setBackgroundColor(android.graphics.Color.BLUE)
            text.setTextColor(android.graphics.Color.WHITE)
        } else {
            text.setBackgroundColor(android.graphics.Color.LTGRAY)
            text.setTextColor(android.graphics.Color.BLACK)
        }

        view.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }

        return view
    }
}