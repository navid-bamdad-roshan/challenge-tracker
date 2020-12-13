package com.example.challengetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.challengeactivity_item_layout.view.*


class ChallengeActivityAdapter(private var listener : dbHelper) : RecyclerView.Adapter<ChallengeActivityHolder>() {

    interface dbHelper {
        fun removeActivityFromList(i : Int)
        fun updateName(i: Int, value: String)
        fun updatePoints(i: Int, value: Float)
    }

    var data = arrayListOf<ActivityEntity>()
        set(value) {
        field = value
        notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeActivityHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.challengeactivity_item_layout, parent, false)

        val viewHolder = ChallengeActivityHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ChallengeActivityHolder, position: Int) {
        val data = data[position] //The challenge?

        holder.item.et_challenge_activity_name.hint = data.name
        holder.item.et_challenge_activity_name.doOnTextChanged { text, _, _, _ ->
            listener.updateName(position,
                    if(text != "") text.toString()
                    else holder.item.et_challenge_activity_name.hint.toString())
        }

        holder.item.et_point_per_km.hint = data.points.toString()
        holder.item.et_point_per_km.doOnTextChanged { text, _, _, _ ->
            listener.updatePoints(position,
                    if(!text.isNullOrEmpty()) text.toString().toFloat()
                    else holder.item.et_point_per_km.hint.toString().toFloat())
        }

        holder.item.button_trash.setOnClickListener {
            removeActivityFromList(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun removeActivityFromList(i: Int) {
        listener.removeActivityFromList(i)
    }
}