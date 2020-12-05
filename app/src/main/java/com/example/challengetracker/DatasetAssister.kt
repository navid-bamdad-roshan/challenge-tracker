package com.example.challengetracker

interface DatasetAssister {
    fun removeActivityFromList(i : Int)
    fun updateName(i: Int, value: String)
    fun updatePoints(i: Int, value: Float)
}