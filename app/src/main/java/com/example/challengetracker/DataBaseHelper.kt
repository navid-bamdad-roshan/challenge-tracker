package com.example.challengetracker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object DataBaseHelper{

    init {}

    private lateinit var applicationContext: Context

    private lateinit var db: FirebaseFirestore

    private const val sharedPreferencesFileKey = "challenge_tracker"
    private const val curChallengeNameKey = "current_challenge_name"
    private const val curActivityNameKey = "current_activity_name"
    private const val curChallengeIdKey = "current_challenge_id"
    private const val curActivityIdKey = "current_activity_id"
    private const val nicknameKey = "nickname"



    private const val challengesCollectionName = "challenges"
    private const val challengeActivitiesCollectionName = "challengeActivities"
    private const val userActivitiesCollectionName = "userActivities"


    // To set the application context
    fun setAppContext(context: Context){
        applicationContext = context
        db = Firebase.firestore
    }

    // Set currently selected challenge name and id in shared preferences
    fun setCurrentChallenge(currentChallengeName:String, currentChallengeId: String){
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(curChallengeIdKey, currentChallengeId)
        editor.putString(curActivityNameKey, currentChallengeName)
        editor.commit()
    }

    // Get currently selected challenge name from shared preferences
    fun getCurrentChallengeName(): String{
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val currentChallenge = sharedPref.getString(curChallengeNameKey,"").toString()
        return(currentChallenge)
    }

    // Get currently selected challenge id from shared preferences
    fun getCurrentChallengeId(): String{
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val currentChallenge = sharedPref.getString(curChallengeIdKey,"").toString()
        return(currentChallenge)
    }





    // Set currently selected activity name and id in shared preferences
    fun setCurrentActivityType(currentActivityName:String, currentActivityId: String){
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(curActivityIdKey, currentActivityId)
        editor.putString(curActivityNameKey, currentActivityName)
        editor.commit()
    }

    // Get currently selected activity name from shared preferences
    fun getCurrentActivityName(): String{
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val currentActivity = sharedPref.getString(curActivityNameKey,"").toString()
        return(currentActivity)
    }

    // Get currently selected activity id from shared preferences
    fun getCurrentActivityId(): String{
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val currentActivity = sharedPref.getString(curActivityIdKey,"").toString()
        return(currentActivity)
    }



    // Set current nickname in shared preferences
    fun setNickname(nickname:String){
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(nicknameKey, nickname)
        editor.commit()
    }

    // Get current nickname from shared preferences
    fun getNickname():String{
        val sharedPref = applicationContext.getSharedPreferences(sharedPreferencesFileKey, Context.MODE_PRIVATE)
        val nickname = sharedPref.getString(nicknameKey,"").toString()
        return(nickname)
    }





    fun addNewChallenge(challenge: Challenge, insertionFinished: (challengeId: String)->Unit){
        val challengesCollection = db.collection(challengesCollectionName)
        val tempChallenge = hashMapOf(
            "name" to challenge.name,
            "deadline" to challenge.deadline.toString(),
            "goalPoints" to challenge.goalPoints.toString()
        )
        val adding = challengesCollection.add(tempChallenge).addOnSuccessListener { it ->
            var tempChallengeId = it.id
            val challengeActivityCollection = db.collection(challengeActivitiesCollectionName)
            challenge.activities.map {
                val tempChallengeActivity = hashMapOf(
                        "name" to it.name,
                        "challengeId" to tempChallengeId,
                        "pointPerKm" to it.pointPerKm
                )
                challengeActivityCollection.add(tempChallengeActivity)
            }
            insertionFinished(tempChallengeId)
        }
    }




    fun addNewUserActivity(userNickname: String, points:Float, challengeId:String, challengeActivityId:String, challengeActivityName:String, insertionFinished: (userActivityId: String)->Unit){
        val userActivityCollection = db.collection(userActivitiesCollectionName)
        val tempUserActivity = hashMapOf(
                "username" to userNickname,
                "points" to points,
                "challengeId" to challengeId,
                "challengeActivityId" to challengeActivityId,
                "challengeActivityName" to challengeActivityName
        )
        userActivityCollection.add(tempUserActivity).addOnSuccessListener {
            insertionFinished(it.id)
        }
    }


    fun getUserActivitiesByUsernameAndChallengeId(username: String, challengeId:String, getResult: (userActivities: ArrayList<UserActivity>)->Unit){
        val userActivitiesCollection = db.collection(userActivitiesCollectionName)
        userActivitiesCollection.whereEqualTo("username", username).whereEqualTo("challengeId", challengeId).get().addOnSuccessListener { userActivities ->
            val userActivitiesArray = arrayListOf<UserActivity>()
            userActivities.documents.map { userActivity ->

                val tempUsername = userActivity.data?.get("username").toString()
                val tempPoints = userActivity.data?.get("points").toString().toFloat()
                val tempId = userActivity.id
                val tempChallengeId = userActivity.data?.get("challengeId").toString()
                val tempChallengeActivityId = userActivity.data?.get("challengeActivityId").toString()
                val tempChallengeActivityName = userActivity.data?.get("challengeActivityName").toString()

                userActivitiesArray.add(UserActivity(username=tempUsername, points = tempPoints, id = tempId,
                        challengeId = tempChallengeId, challengeActivityId = tempChallengeActivityId,
                        challengeActivityName = tempChallengeActivityName )
                )
            }
            getResult(userActivitiesArray)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getChallengeById(challengeId: String, getResult: (challenge: Challenge)->Unit){
        val challengesCollection = db.collection(challengesCollectionName)
        challengesCollection.document(challengeId).get().addOnSuccessListener { it ->
            val date = LocalDate.parse(it.data?.get("deadline").toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val goalPoints = it.data?.get("goalPoints").toString().toFloat()
            var tempChallenge = Challenge(name = it.data?.get("name").toString(), deadline = date, goalPoints=goalPoints, id= it.id)

            // getting challenge activities
            val challengeActivityCollection = db.collection(challengeActivitiesCollectionName)
            challengeActivityCollection.whereEqualTo("challengeId", tempChallenge.id).get().addOnSuccessListener { activitiesResult->
                activitiesResult.documents.map {activity->
                    val tempName = activity.data?.get("name").toString()
                    val tempPointPerKm = activity.data?.get("pointPerKm").toString().toFloat()
                    val tempId = activity.id
                    tempChallenge.activities.add(ChallengeActivity(name = tempName, pointPerKm = tempPointPerKm, id = tempId))
                }
                getResult(tempChallenge)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllChallenges(getResult: (ArrayList<Challenge>) -> Unit){
        val challengesCollection = db.collection(challengesCollectionName)
        challengesCollection.get().addOnSuccessListener { allChallenges ->
            val challengesArray = arrayListOf<Challenge>()
            allChallenges.documents.mapIndexed { challengeIndex, challengeInstance ->
                val date = LocalDate.parse(
                    challengeInstance.data?.get("deadline").toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
                var tempChallenge = Challenge(
                    name = challengeInstance.data?.get("name").toString(),
                    deadline = date,
                    goalPoints = challengeInstance.data?.get("goalPoints").toString().toFloat(),
                    id = challengeInstance.id
                )
                challengesArray.add(tempChallenge)
            }


            // getting challenge activities
            val challengeActivityCollection = db.collection(challengeActivitiesCollectionName)
            challengeActivityCollection.get().addOnSuccessListener {challengeActivitiesResult ->

                val tempChallengeActivityArray = arrayListOf<ChallengeActivity>()

                challengeActivitiesResult.documents.map {
                    val tempName = it.data?.get("name").toString()
                    val tempPointPerKm = it.data?.get("pointPerKm").toString().toFloat()
                    val tempId = it.id
                    tempChallengeActivityArray.add(ChallengeActivity(
                        name = tempName,
                        pointPerKm = tempPointPerKm,
                        id = tempId
                    ))
                }

                challengesArray.mapIndexed { challengeIndex, challengeInstance ->
                    tempChallengeActivityArray.filter { it -> it.id == challengeInstance.id }.map {
                        challengesArray[challengeIndex].activities.add(it)
                    }
                }
                getResult(challengesArray)
            }
        }
    }




    fun getLeadingUsersByChallengeId(challengeId: String, getResult: (ArrayList<User>) -> Unit){
        val userActivitiesCollection = db.collection(userActivitiesCollectionName)
        userActivitiesCollection.whereEqualTo("challengeId", challengeId).get().addOnSuccessListener { userActivities ->
            val userActivitiesArray = arrayListOf<UserActivity>()
            userActivities.documents.map { userActivity ->
                val tempUsername = userActivity.data?.get("username").toString()
                val tempPoints = userActivity.data?.get("points").toString().toFloat()
                val tempId = userActivity.id
                val tempChallengeId = userActivity.data?.get("challengeId").toString()
                val tempChallengeActivityId = userActivity.data?.get("challengeActivityId").toString()
                val tempChallengeActivityName = userActivity.data?.get("challengeActivityName").toString()

                userActivitiesArray.add(UserActivity(username=tempUsername, points = tempPoints, id = tempId,
                        challengeId = tempChallengeId, challengeActivityId = tempChallengeActivityId,
                        challengeActivityName = tempChallengeActivityName )
                )

            }

            // finding the points of each username
            val points = hashMapOf<String, Float>()
            val users = arrayListOf<User>()
            userActivitiesArray.map { userActivity ->
                if(points.containsKey(userActivity.username)){
                    var tempPoint = points[userActivity.username]
                    tempPoint?.let {
                        points[userActivity.username] = tempPoint + userActivity.points
                    }
                }else{
                    points[userActivity.username] = userActivity.points
                }
            }
            points.map {
                users.add(User(username = it.key, points = it.value))
            }
            getResult(users)
        }
    }

}

data class ChallengeActivity(var name: String,
                             var pointPerKm: Float,
                             var id:String = ""){
}

data class User(var username: String, var points: Float){
}

data class Challenge(var name: String,
                     var deadline: LocalDate,
                     var goalPoints: Float,
                     var id: String = ""){
    var activities = arrayListOf<ChallengeActivity>()
}

data class UserActivity(var username:String,
                        var points:Float,
                        var id:String = "",
                        var challengeId: String="",
                        var challengeActivityId: String = "",
                        var challengeActivityName: String = "" ){

}

