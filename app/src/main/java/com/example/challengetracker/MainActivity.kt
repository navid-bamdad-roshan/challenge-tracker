package com.example.challengetracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RecipeViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSettings()

        DataBaseHelper.setAppContext(this.applicationContext)


        // Getting viewModel instance
        viewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)


        // Set challenge spinner adapter
        spinner_challenges.adapter = viewModel.adapter


        viewModel.currentChallengeId = DataBaseHelper.getCurrentChallengeId()
        viewModel.username = DataBaseHelper.getNickname()



        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            DataBaseHelper.getAllChallenges() {
                viewModel.challenges = it
                it.map {
                    viewModel.adapter.add(it.name)
                }

                // Select current challenge as default for challenges spinner
                if (viewModel.currentChallengeId != ""){
                    val scope2 = CoroutineScope(Dispatchers.Main)
                    scope2.launch {
                        val index = viewModel.challenges.indexOfFirst { it.id == viewModel.currentChallengeId }
                        spinner_challenges.setSelection(index + 1)
                    }
                }

            }
        }



        spinner_challenges.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position>0){
                    viewModel.selectedChallenge = viewModel.challenges[position-1]
                    getUserActivities()
                }else{
                    clearView()
                }

            }

        }



        btn_create_new_challenge.setOnClickListener {
            intent = Intent(this, NewChallengeActivity::class.java)
            startActivity(intent)
        }


        btn_start_activity.setOnClickListener {
            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }


    }

    private fun setChallengeToView(){
        tv_user_points_value.text = viewModel.userPoints.toString()
        tv_goal_points_value.text = viewModel.selectedChallenge.goalPoints.toString()
        tv_deadline_value.text = viewModel.selectedChallenge.deadline
        tv_leading_point_value.text = viewModel.leadingPoint.toString()
    }

    private fun clearView(){
        tv_user_points_value.text = ""
        tv_goal_points_value.text = ""
        tv_deadline_value.text = ""
        tv_leading_point_value.text = ""
    }

    // Getting user activities and the leading points
    private fun getUserActivities() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            DataBaseHelper.getUserActivitiesByUsernameAndChallengeId(
                viewModel.username,
                viewModel.selectedChallenge.id
            ) {
                viewModel.userPoints = 0F
                it.map {
                    viewModel.userPoints.plus(it.points)
                }
                DataBaseHelper.getLeadingUsersByChallengeId(viewModel.selectedChallenge.id) {
                    if (it.size > 0) {
                        var maxPoint = it.maxBy { it.points }?.points
                        maxPoint?.let {
                            viewModel.leadingPoint = maxPoint
                        }
                    }
                    setChallengeToView()
                }
            }
        }
    }


    private fun setupSettings() {
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment())
        val darkMode = getDefaultSharedPreferences(this).getBoolean("dark_mode", false)
        if (!darkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }







}