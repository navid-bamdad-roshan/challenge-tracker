package com.example.challengetracker


import android.app.Application
import android.content.res.Resources
import android.os.Build
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    var currentChallengeId = ""
    var username = ""
    val setSpinnerDefaultValue = MutableLiveData<Event<String>>()
    var spinnerIsUsedOnce = false


    init {

        currentChallengeId = DataBaseHelper.getCurrentChallengeId()
        username = DataBaseHelper.getNickname()

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {

            DataBaseHelper.getAllChallenges() {
                challenges = it
                adapter.clear()
                adapter.add(context.getString(com.example.challengetracker.R.string.select_challenge))
                it.map {
                    adapter.add(it.name)
                }

                adapter.notifyDataSetChanged()

                // Select current challenge as default for challenges spinner
                if (currentChallengeId != ""){
                    // index of first match
                    val index = challenges.indexOfFirst { it.id == currentChallengeId }
                    if (index == -1){
                        currentChallengeId = ""
                        DataBaseHelper.setCurrentChallenge("","")
                        setSpinnerDefaultValue.value = Event("0")
                    }else{
                        selectedChallenge = challenges[index]
                        setSpinnerDefaultValue.value = Event(index.toString())
                        //spinner_challenges.setSelection(index + 1)
                    }

                }

            }
        }
    }


    fun updateChallengesAdapter(){
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {

            DataBaseHelper.getAllChallenges() {
                challenges = it
                it.map {
                    adapter.add(it.name)
                }

                adapter.notifyDataSetChanged()

                // Select current challenge as default for challenges spinner
                if (currentChallengeId != ""){


                    val index = challenges.indexOfFirst { it.id == currentChallengeId }
                    setSpinnerDefaultValue.value = Event(index.toString())
                    //spinner_challenges.setSelection(index + 1)
                }

            }
        }
    }


    private val context = getApplication<Application>().applicationContext

    var adapter = ArrayAdapter(context, R.layout.spinner_itm, arrayListOf(context.getString(com.example.challengetracker.R.string.select_challenge)))

    var challenges = arrayListOf<Challenge>()

    var selectedChallenge = Challenge("","",0F,"")


    var userPoints = 0F
    var leadingPoint = 0F
}




open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getCurrentChallengeIndex(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}