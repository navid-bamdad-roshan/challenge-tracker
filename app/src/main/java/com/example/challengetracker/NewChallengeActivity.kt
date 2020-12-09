package com.example.challengetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_new_challenge.*
import java.util.*
import kotlin.collections.ArrayList

class NewChallengeActivity : AppCompatActivity(), DatasetAssister {


    lateinit var myAdapter: ChallengeActivityAdapter
    var activityList = ArrayList<Pair<String, Float>>()

    var currActivity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_challenge)

        myAdapter = ChallengeActivityAdapter(activityList, this)

        rv_activities.layoutManager = LinearLayoutManager(this)
        rv_activities.adapter = myAdapter

        date_pick.minDate = Calendar.getInstance().timeInMillis


        button_AddActivity.setOnClickListener {
            addActivityToList("Activity $currActivity",1f)
            currActivity++
        }
        button_CreateChallenge.setOnClickListener {
            val activities = arrayListOf<ChallengeActivity>()
            activityList.forEach {
                val chalAct = ChallengeActivity(it.first, it.second)
                activities.add(chalAct)
            }

            if (activities.size > 0) {

                //Setting the date
                var month = (date_pick.month+1).toString()
                if(month.length==1)
                    month = "0$month"

                var day = (date_pick.dayOfMonth+1).toString()
                if(day.length==1)
                    day = "0$day"

                val date = "${date_pick.year}-${month}-${day}"

                Log.d("Help", date)

                //Creating the challenge
                val chal = Challenge(
                        if(et_ChallengeName.text.toString() != "") et_ChallengeName.text.toString()
                        else resources.getString(R.string.default_new_challenge_name),
                        date,
                        if(et_GoalPoints.text.toString() != "") et_GoalPoints.text.toString().toFloat()
                        else resources.getString(R.string.default_new_challenge_points).toFloat())

                //Assigning all activities for the challenge
                chal.activities = activities

                DataBaseHelper.addNewChallenge(chal) {}
                finish()
            }

            else {
                val toast = Toast.makeText(this, R.string.toast_no_activity, Toast.LENGTH_LONG)
                toast.show()
            }

        }
    }

    private fun addActivityToList(name: String, points: Float) {
        activityList.add(Pair(name, points))
        myAdapter.notifyDataSetChanged()
    }

    override fun removeActivityFromList(i: Int) {
        activityList.removeAt(i)
        myAdapter.notifyDataSetChanged()
    }

    override fun updateName(i: Int, value: String) {
        activityList[i] = Pair(value, activityList[i].second)
    }

    override fun updatePoints(i: Int, value: Float) {
        activityList[i] = Pair(activityList[i].first, value)
    }


}