package com.example.theherd

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.time.LocalDate
import java.time.YearMonth


class CalendarAdapter(
    private val context: Context,
    private val days: List<String>,
//    private val eventsMap: Map<String, MutableList<String>>,
    private val eventsMap: Map<String, MutableList<Event>>,
    private val currentMonth: YearMonth
) : BaseAdapter() {

    private var selectedPosition = -1

    override fun getCount(): Int = days.size

    override fun getItem(position: Int): Any = days[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_day, parent, false)

        val text = view.findViewById<TextView>(R.id.dayText)
        val dot = view.findViewById<View>(R.id.eventDot)

        val dayStr = days[position]

        text.text = dayStr

        if (dayStr.isNotEmpty()) {

            val key = "${currentMonth.year}-${currentMonth.monthValue}-%02d"
                .format(dayStr.toInt())

            val hasEvent = !eventsMap[key].isNullOrEmpty()

            dot.visibility = if (hasEvent) View.VISIBLE else View.GONE
        } else {
            dot.visibility = View.GONE
        }

        // highlight selected day
        if (position == selectedPosition) {
            view.setBackgroundColor(android.graphics.Color.BLUE)
        } else {
            view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        return view
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}