package com.example.challengetracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.challengeactivity_item_layout.view.*


class ChallengeActivityAdapter(val dataset: List<Pair<String, Float>>, val datRem : DatasetAssister) : RecyclerView.Adapter<ChallengeActivityHolder>() {

    val removalInterface = datRem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeActivityHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.challengeactivity_item_layout, parent, false)

        val viewHolder = ChallengeActivityHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ChallengeActivityHolder, position: Int) {
        val data = dataset[position] //The challenge?

        holder.item.et_challenge_activity_name.hint = data.first
        holder.item.et_challenge_activity_name.doOnTextChanged { text, _, _, _ ->
            datRem.updateName(position,
                    if(text != "") text.toString()
                    else holder.item.et_challenge_activity_name.hint.toString())
        }

        holder.item.et_point_per_km.hint = data.second.toString()
        holder.item.et_point_per_km.doOnTextChanged { text, _, _, _ ->
            datRem.updatePoints(position,
                    if(text != "") text.toString().toFloat()
                    else holder.item.et_point_per_km.hint.toString().toFloat())
        }

        holder.item.button_trash.setOnClickListener {
            removeActivityFromList(position)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun removeActivityFromList(i: Int) {
        removalInterface.removeActivityFromList(i)
    }
}