package com.example.challengetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_new_challenge.*

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


        button_AddActivity.setOnClickListener {
            addActivityToList("Activity $currActivity",1f)
            currActivity++
        }
        button_CreateChallenge.setOnClickListener {
            val activities = arrayListOf<ChallengeActivity>()
            activityList.forEach {
                val chalAct = ChallengeActivity(it.first, it.second.toFloat())
                activities.add(chalAct)
            }

            if (activities.size > 0) {
                val chal = Challenge(
                        if(et_ChallengeName.text.toString() != "") et_ChallengeName.text.toString()
                        else resources.getString(R.string.default_new_challenge_name),
                        et_Date.text.toString(),                        //TODO Needs a better representation and default value
                        if(et_GoalPoints.text.toString() != "") et_GoalPoints.text.toString().toFloat()
                        else resources.getString(R.string.default_new_challenge_points).toFloat())

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