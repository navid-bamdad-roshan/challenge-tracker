package com.example.challengetracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        //PreferenceManager.getDefaultSharedPreferences(this.activity)

        /*when opening the settings nickname, check if there is nickname in sharedpreferences
                if not, blank. Also be able to submit the... activity in the creation view*/

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
        val editText = findPreference<EditTextPreference>("edit_text_preference")

        if (editText != null) {
            val nick = DataBaseHelper.getNickname()
            checkNickName(nick, editText)

            editText.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                _, newVal ->
                DataBaseHelper.setNickname(newVal.toString()) //TODO NOTE, SPACEBAR NAMES GET SET

                checkNickName(newVal.toString(), editText)
                true
            }
        }

    }

    private fun checkNickName(value: String, editText:  EditTextPreference) {
        if(value.trim() == "")
            editText.summary = "No nickname assigned"
        else
            editText.summary = "Current Nickname: $value"
    }

}