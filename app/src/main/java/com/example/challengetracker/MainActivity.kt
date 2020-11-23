package com.example.challengetracker

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DataBaseHelper.setAppContext(this.applicationContext)

        btn_start_activity.setOnClickListener {
            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }



//        DataBaseHelper.addNewUserActivity("tempUser", 10F, "eCYl9TuShYqjQEfvfLiR", "LlaOn5YW4GhGQwXL0MJw"){
//            Log.i("logg", "insert successful: "+it)
//        }



//        DataBaseHelper.getUserActivitiesByUsernameAndChallengeId("tempUser", "eCYl9TuShYqjQEfvfLiR"){
//            Log.i("logg", "num: "+it.size.toString())
//            it.map {
//                Log.i("logg", "challenge activity name: "+it.challengeActivityName)
//            }
//        }


//        DataBaseHelper.getLeadingUsersByChallengeId("0yQPsdExBSAcdC3nCQUe"){
//            it.map {
//                Log.i("logg", it.username)
//                Log.i("logg", it.points.toString())
//
//            }
//        }



//        val date = LocalDate.parse("2021-12-03", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//
//        //Log.i("logg", date.toString())
//
//        var challenge = Challenge("test challenge 2", date)
//        challenge.activities.add(ChallengeActivity("challenge activity 1", 2F))
//        challenge.activities.add(ChallengeActivity("challenge activity 2", 3F))
//
//



//        // Insert new challenge
//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            DataBaseHelper.addNewChallenge(challenge, ::onInsertFinished)
//        }



//        Insert new user activity
//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            DataBaseHelper.addNewUserActivity("tempUser", 10F, "0yQPsdExBSAcdC3nCQUe", "TH9T3N5LwL7OUJU5Hvjy", ::onInsertFinished)
//        }


//        // Get getChallengeById
//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            DataBaseHelper.getChallengeById("eCYl9TuShYqjQEfvfLiR", ::onResult)
//        }

        //RlIWGWIdBSD6mrxGra4w


//        // Get all Challenges
//        val scope = CoroutineScope(Dispatchers.Default)
//        scope.launch {
//            DataBaseHelper.getAllChallenges {challenges ->
//                challenges.map {challenge ->
//                    Log.i("logg", "challenge id: "+challenge.id)
//                    Log.i("logg", "challenge name: "+challenge.name)
//                    Log.i("logg", "challenge num activities: "+ challenge.activities.size.toString())
//                    challenge.activities.map {
//                        Log.i("logg", "challenge activity name: "+it.name)
//                        Log.i("logg", "challenge activity point per km: "+it.pointPerKm)
//
//
//                    }
//                }
//            }
//        }


//        DataBaseHelper.getAllChallenges {challenges ->
//            challenges.map {challenge ->
//                Log.i("logg", "challenge id: "+challenge.id)
//                Log.i("logg", "challenge name: "+challenge.name)
//                Log.i("logg", "challenge num activities: "+ challenge.activities.size.toString())
//                challenge.activities.map {
//                    Log.i("logg", "challenge activity name: "+it.name)
//                    Log.i("logg", "challenge activity point per km: "+it.pointPerKm)
//
//
//                }
//            }
//        }




        //DataBaseHelper.setCurrentActivityType("activity test 2")
        //DataBaseHelper.setCurrentChallenge("challenge test")

        //val cc = DataBaseHelper.getCurrentChallenge()
        //val ca = DataBaseHelper.getCurrentActivityType()


        //Log.i("logg", "current challenge is:"+cc)
        //Log.i("logg", "current activity is:"+ca)
    }


//    fun onInsertFinished(id:String){
//        Log.i("logg", id.toString())
//    }
//
//
//    private fun onResult(challenge: Challenge){
//
//        Log.i("logg", "challenge id: "+challenge.id)
//        Log.i("logg", "challenge name: "+challenge.name)
//        Log.i("logg", "challenge num activities: "+ challenge.activities.size.toString())
//        challenge.activities.map {
//            Log.i("logg", "challenge activity name: "+it.name)
//            Log.i("logg", "challenge activity point per km: "+it.pointPerKm)
//
//
//        }


//    private fun onResult(challenges: ArrayList<Challenge>){
//
//        challenges.map {challenge ->
//        Log.i("logg", "challenge id: "+challenge.id)
//        Log.i("logg", "challenge name: "+challenge.name)
//        Log.i("logg", "challenge num activities: "+ challenge.activities.size.toString())
//        challenge.activities.map {
//            Log.i("logg", "challenge activity name: "+it.name)
//            Log.i("logg", "challenge activity point per km: "+it.pointPerKm)
//
//
//            }
//        }
//
//    }
}