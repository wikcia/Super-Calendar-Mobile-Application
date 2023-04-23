package com.example.supercalendar

import android.content.Context
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.gson.Gson

class AddEvent : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event_popup)


        // warning! change parent activity in AndroidManifest.xml
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up the add button click listener
        val addButton = findViewById<Button>(R.id.addButton)

        addButton.setOnClickListener {
            val titleEditText = findViewById<EditText>(R.id.titleEditText)
            val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
            val datePicker = findViewById<DatePicker>(R.id.datePicker)

            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()

            // Get the selected date from the date picker
            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            /**
             * This will set the time of the calendar instance to the start of the day,
             * and the event_time string will only have the time accuracy up to the day.
             */

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)


            val dayInMillis = calendar.timeInMillis.toString()


            val sharedPrefs = getSharedPreferences("MyEvents", Context.MODE_PRIVATE)

            val editor = sharedPrefs.edit()

            // Get the number of events already stored for this day
            val numOfEvents = sharedPrefs.getInt("num_of_events_${dayInMillis}", 0)


            // Check if there are any removed indices for this day
            val removedIndicesJson = sharedPrefs.getString("removed_indices_${dayInMillis}", "[]")
            var removedIndices = Gson().fromJson(removedIndicesJson, Array<Int>::class.java)

            val newIndex = if (removedIndices.isNotEmpty()) {
                // Use the smallest available index
                removedIndices.minOrNull()!!
            } else {
                // Use the next available index
                numOfEvents + 1
            }

            if (title != "") {
                editor.putString("event_title_${dayInMillis}_${newIndex}", title)
                editor.putString("event_description_${dayInMillis}_${newIndex}", description)
                editor.putString("event_time_${dayInMillis}_${newIndex}", dayInMillis)

                editor.putInt("num_of_events_${dayInMillis}", newIndex)

                // Remove the index from the list of removed indices if it was there
                if (removedIndices.contains(newIndex)) {
                    // uwaga - znowu pracujemy na kopii i przypisujemy ją do oryginalnej listy
                    removedIndices = removedIndices.toMutableList().apply {
                        remove(newIndex)
                    }.toTypedArray()
                    val newRemovedIndicesJson = Gson().toJson(removedIndices)
                    editor.putString("removed_indices_${dayInMillis}", newRemovedIndicesJson)
                }

                editor.apply()
            }

            finish()
        }
    }
}