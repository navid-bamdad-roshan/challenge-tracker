package com.example.challengetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_new_challenge.*
import java.util.*
import kotlin.collections.ArrayList

class NewChallengeActivity : AppCompatActivity() {


    private lateinit var myAdapter: ChallengeActivityAdapter
    var activityList = ArrayList<ActivityEntity>()

    var currActivity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_challenge)

        myAdapter = ChallengeActivityAdapter(object : ChallengeActivityAdapter.dbHelper{
            override fun removeActivityFromList(i: Int) {
                activityList.removeAt(i)
                myAdapter.notifyDataSetChanged()
            }

            override fun updateName(i: Int, value: String) {
                activityList[i].name = value
            }

            override fun updatePoints(i: Int, value: Float) {
                activityList[i].points = value
            }
        })

        myAdapter.data = activityList

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
                val chalAct = ChallengeActivity(it.name, it.points)
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
        activityList.add(ActivityEntity(name, points))
        myAdapter.notifyDataSetChanged()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("challenge_name", et_ChallengeName.text.toString())
        outState.putString("goal_points", et_GoalPoints.text.toString())
        outState.putInt("currAc", currActivity)

        outState.putInt("year", date_pick.year)
        outState.putInt("month", date_pick.month)
        outState.putInt("day", date_pick.dayOfMonth)


        outState.putParcelableArrayList("dataAdapter", activityList)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        et_ChallengeName.setText(savedInstanceState.getString("challenge_name"),TextView.BufferType.EDITABLE)
        et_GoalPoints.setText(savedInstanceState.getString("goal_points"),TextView.BufferType.EDITABLE)
        currActivity = savedInstanceState.getInt("currAc")

        date_pick.init(savedInstanceState.getInt("year"),
                savedInstanceState.getInt("month"),
                savedInstanceState.getInt("day"),
                null)

        val acs = savedInstanceState.getParcelableArrayList<ActivityEntity>("dataAdapter")

        if(acs != null) {
            activityList = acs
            myAdapter.data = activityList
            myAdapter.notifyDataSetChanged()
        }
    }


}


