package com.example.challengetracker

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        //PreferenceManager.getDefaultSharedPreferences(this.activity)

        findPreference<SwitchPreference>("dark_mode")?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            _, newValue ->
            if (!(newValue as Boolean))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            this.activity?.recreate()
            true;

        }

        //TODO: Add a listener for the nickname as well
    }

}