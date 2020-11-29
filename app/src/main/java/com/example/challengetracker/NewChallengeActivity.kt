package com.example.challengetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_new_challenge.*

class NewChallengeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_challenge)


        button_CreateChallenge.setOnClickListener {
            val chal = Challenge(et_ChallengeName.text.toString(),
                                et_Date.text.toString(),
                                et_GoalPoints.text.toString().toFloat(),
                                "testerID")
            DataBaseHelper.addNewChallenge(chal) {}
            finish()
        }
    }
}