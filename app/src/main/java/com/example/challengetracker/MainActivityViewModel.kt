package com.example.challengetracker

import android.R
import android.app.Application
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    var adapter = ArrayAdapter(context, R.layout.simple_spinner_item, arrayListOf("Select a challenge"))

    var challenges = arrayListOf<Challenge>()

    lateinit var selectedChallenge: Challenge
    lateinit var currentChallengeId: String
    lateinit var username: String


    var userPoints = 0F
    var leadingPoint = 0F






}