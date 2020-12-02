package com.example.challengetracker

import android.R
import android.app.Application
import android.os.Build
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    var currentChallengeId = ""
    var username = ""
    val setSpinnerDefaultValue = MutableLiveData<Event<String>>()


    init {

        currentChallengeId = DataBaseHelper.getCurrentChallengeId()
        username = DataBaseHelper.getNickname()

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

    var adapter = ArrayAdapter(context, R.layout.simple_spinner_item, arrayListOf("Select a challenge"))

    var challenges = arrayListOf<Challenge>()

    lateinit var selectedChallenge: Challenge


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