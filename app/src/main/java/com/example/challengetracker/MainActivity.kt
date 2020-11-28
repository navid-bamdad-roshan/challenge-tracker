package com.example.challengetracker

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
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



        // Select default challenge in spinner once the data is received from database
        viewModel.setSpinnerDefaultValue.observe(this, Observer { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                // plus one because first row is "select a challenge"
                spinner_challenges.setSelection(it.toInt().plus(1))
            }
        })





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



    private fun setChallengeDataToView(){
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
                    setChallengeDataToView()
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