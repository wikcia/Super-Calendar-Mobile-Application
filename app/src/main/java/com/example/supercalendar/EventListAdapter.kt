package com.example.supercalendar

import android.app.AlertDialog
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.util.*

data class Event(
    val title: String,
    val description: String,
    val date: Date? = null)

/**
 * Class that is responsible for creating customized adapter that holds events in the calendar
 * View Android Developer documentation for more
 */

class EventListAdapter(private var events: List<Event>, private val sharedPrefs: SharedPreferences):
    RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val deleteImageView: ImageView = itemView.findViewById(R.id.delete_event)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        // If the event should be shown, bind the view holder as normal
        holder.itemView.visibility = View.VISIBLE
        holder.titleTextView.text = event.title
        holder.descriptionTextView.text = event.description
        holder.dateTextView.text = event.date?.toString() ?: ""

        // Check if the event should be hidden
        if (event.title.isEmpty() && event.description.isEmpty() && event.date == null) {
            // Hide the holder and return
            holder.itemView.visibility = View.GONE
            return
        }

        // Set an OnClickListener for the delete icon
        holder.deleteImageView.setOnClickListener {
            val calendar = Calendar.getInstance()

            calendar.time = event.date

            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)


            calendar.set(year, month, dayOfMonth)

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val dayInMillis = calendar.timeInMillis.toString()

            //events.toMutableList()[position] = Event("","",null)
            //This code creates a new list that is a copy of the original list (using toMutableList()), !!!! This doesn't work !!!!
            // then it replaces the element at the specified position with a new Event object that
            // has empty strings for the title and description, and null for the date.
            //
            //However, this new list is not being stored anywhere,
            // so the original events list is not being updated. To fix this,
            // you can assign the new list to the events variable, like this:

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Usuń wydarzenie")
            builder.setMessage("Czy jesteś pewien że chcesz usunąć to wydarzenie?")

            builder.setPositiveButton("Usuń") { _, _ ->


                val removedIndex = position + 1
                events = events.toMutableList().apply {
                    set(position, Event("", "", null))
                }

                val editor = sharedPrefs.edit()

                editor.remove("event_time_${dayInMillis}_${removedIndex}".replace("\"", ""))
                editor.remove("event_title_${dayInMillis}_${removedIndex}".replace("\"", ""))
                editor.remove("event_description_${dayInMillis}_${removedIndex}".replace("\"", ""))

                // Add the removed index to the list of removed indices
                val removedIndicesJson = sharedPrefs.getString("removed_indices_${dayInMillis}", "[]")
                val removedIndices = Gson().fromJson(removedIndicesJson, Array<Int>::class.java).toMutableList()
                removedIndices.add(removedIndex)
                val newRemovedIndicesJson = Gson().toJson(removedIndices)
                editor.putString("removed_indices_${dayInMillis}", newRemovedIndicesJson)

                editor.apply()

                notifyDataSetChanged()
            }
                builder.setNegativeButton("Anuluj", null)
                builder.show()

            }
        }
        override fun getItemCount(): Int {
            return events.size
        }
    }


