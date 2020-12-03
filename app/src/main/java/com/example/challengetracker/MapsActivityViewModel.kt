package com.example.challengetracker

import android.R
import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MapsActivityViewModel(application: Application) : AndroidViewModel(application) {

    var currentChallengeId = ""
    var currentChallengeName = ""
    var currenChallengeDate = ""
    var currentActivityPos = 0
    val setSpinnerDefaultValue = MutableLiveData<MapsEvent<String>>()


    init {
        currentChallengeId = DataBaseHelper.getCurrentChallengeId()
        currentChallengeName = DataBaseHelper.getCurrentChallengeName()
       // username = DataBaseHelper.getNickname()
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {

            DataBaseHelper.getChallengeById(currentChallengeId) { challenge ->
                currentChallengeName = challenge.name
                currenChallengeDate = challenge.deadline
                challenge.activities.map {
                    adapter.add(it)
                }
                adapter.notifyDataSetChanged()

                // Select current challenge as default for challenges spinner
             //   if (currentChallengeId != ""){
               //     val index = challenges.indexOfFirst { it.id == currentChallengeId }
                setSpinnerDefaultValue.value = MapsEvent(currentActivityPos.toString())
                    //spinner_challenges.setSelection(index + 1)
                //}

            }
        }
    }

    private val context = getApplication<Application>().applicationContext

    var adapter = ArrayAdapter(context,
            R.layout.simple_spinner_item,
            arrayListOf<ChallengeActivity>(ChallengeActivity(
                    "Select an activity", 0f, "")))
    }




 class MapsEvent<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getCurrentActivityIndex(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    fun peekContent(): T = content
}