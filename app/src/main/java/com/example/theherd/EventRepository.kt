package com.example.theherd

object EventRepository {

    // key = "topicId_YYYY-MM-DD"
    private val events = mutableMapOf<String, MutableList<String>>()

    fun addEvent(topicId: String, dateKey: String, event: String) {
        val fullKey = "${topicId}_$dateKey"

        if (!events.containsKey(fullKey)) {
            events[fullKey] = mutableListOf()
        }
        events[fullKey]?.add(event)
    }

    fun getEventsForTopic(topicId: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        events.forEach { (key, eventList) ->
            if (key.startsWith(topicId)) {
                val date = key.removePrefix("${topicId}_")
                eventList.forEach { event ->
                    result.add(date to event)
                }
            }
        }

        return result
    }

    fun updateEvent(topicId: String, date: String, oldEvent: String, newEvent: String) {
        val key = "${topicId}_$date"
        val list = events[key] ?: return

        val index = list.indexOf(oldEvent)
        if (index != -1) {
            list[index] = newEvent
        }
    }

    fun getEventsForDay(topicId: String, dateKey: String): List<String> {
        return events["${topicId}_$dateKey"] ?: emptyList()
    }
}