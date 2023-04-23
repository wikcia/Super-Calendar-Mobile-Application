package com.example.supercalendar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var addEventButton: ImageButton
    private lateinit var noEventsTextView: TextView


    private val eventsMap = mutableMapOf<String, MutableList<Event>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        addEventButton = findViewById(R.id.addEventButton)
        noEventsTextView = findViewById(R.id.no_events_text_view)
        noEventsTextView.visibility = View.GONE

        var dayInMillis: String
        var events: List<Event>

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            getSharedPref()

            val sharedPrefs = getSharedPreferences("MyEvents", Context.MODE_PRIVATE)

            // Create a calendar instance and set the selected date
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            // Get the selected date
            calendar.set(android.icu.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(android.icu.util.Calendar.MINUTE, 0)
            calendar.set(android.icu.util.Calendar.SECOND, 0)
            calendar.set(android.icu.util.Calendar.MILLISECOND, 0)

            dayInMillis = calendar.timeInMillis.toString()

            Log.d("MainActivity_${dayInMillis}", "event_time_${dayInMillis}")

            if (eventsExistForDate(dayInMillis)) {
                events = getEventsForDate(dayInMillis)
                if (events.isEmpty()) {
                    noEventsTextView.visibility = View.VISIBLE
                    eventsRecyclerView.visibility = View.GONE
                    noEventsTextView.text = "Brak wydarzeń"
                } else {
                    noEventsTextView.visibility = View.GONE
                    eventsRecyclerView.visibility = View.VISIBLE
                    eventsRecyclerView.layoutManager = LinearLayoutManager(this)
                    eventsRecyclerView.adapter = EventListAdapter(events, sharedPrefs)
                }
            } else {
                noEventsTextView.visibility = View.VISIBLE
                eventsRecyclerView.visibility = View.GONE
                noEventsTextView.text = "Brak wydarzeń"
            }

        }

        addEventButton.setOnClickListener {

            val intent = Intent(this, AddEvent::class.java)
            startActivity(intent)

        }
    }

    private fun getEventsForDate(selectedDate: String): List<Event> {

        val sharedPrefs = getSharedPreferences("MyEvents", Context.MODE_PRIVATE)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDate.substringAfter("time_").toLong()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eventDate = calendar.time.toString()


        val events: MutableList<Event> = if (eventsMap.containsKey(selectedDate)) {
            eventsMap[selectedDate]!!.toMutableList()
        } else {
            mutableListOf()
        }

        for (event in eventsMap) {
            Log.d("Map", event.toString())
        }
        val allEntries: Map<String, *> = sharedPrefs.all

        for ((key, value) in allEntries) {
            Log.d("SharedPreferences", "Key: $key Value: $value")
        }

        for ((key, value) in sharedPrefs.all) {
            if (key.startsWith("event_time_") && selectedDate == value) {
                val eventDate = key.substringAfter("event_time_")
                val title = sharedPrefs.getString("event_title_${eventDate}", "") ?: ""
                val description = sharedPrefs.getString("event_description_${eventDate}", "") ?: ""

                events.add(Event(title, description, calendar.time))
            }
        }

        eventsMap[eventDate] = events

        return events
    }

    private fun eventsExistForDate(selectedDate: String): Boolean {
        val events = getEventsForDate(selectedDate)
        return events.isNotEmpty()
    }

    private fun getSharedPref() {
        val sharedPreferences = getSharedPreferences("MyEvents", MODE_PRIVATE)
        val allEntries: Map<String, *> = sharedPreferences.all
        for ((key, value) in allEntries) {
            Log.d("SharedPreferences", "Key: $key")
        }

    }



}
