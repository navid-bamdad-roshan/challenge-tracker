package com.example.challengetracker

import android.content.BroadcastReceiver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_new_challenge.*

class NewChallengeActivity : AppCompatActivity() {

    lateinit var myAdapter: ChallengeActivityAdapter
    val activityList = ArrayList<Pair<String, String>>()

    var currActivity = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_challenge)

        myAdapter = ChallengeActivityAdapter(activityList)

        rv_activities.layoutManager = LinearLayoutManager(this)
        rv_activities.adapter = myAdapter

        button_AddActivity.setOnClickListener {
            addActivityToList("Activity $currActivity","1")
            currActivity++
        }

        //TODO This challenge needs limiters to prevent wrong date from being written
        button_CreateChallenge.setOnClickListener {
            val chal = Challenge(et_ChallengeName.text.toString(),
                                et_Date.text.toString(),
                                et_GoalPoints.text.toString().toFloat())


            /*when opening the settings nickname, check if there is nickname in sharedpreferences
                if not, blank. Also be able to submit the... activity in the creation view*/

            DataBaseHelper.addNewChallenge(chal) {}

            finish()
        }
    }

    fun addActivityToList(name: String, points: String) {
        activityList.add(Pair(name, points))
        myAdapter.notifyDataSetChanged()
    }

    fun removeActivityFromList(name: String) { //TODO or catch position?

    }
}