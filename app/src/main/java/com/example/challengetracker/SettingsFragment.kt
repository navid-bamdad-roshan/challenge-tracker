package com.example.challengetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<SwitchPreference>("dark_mode")?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            _, newValue ->
            if (!(newValue as Boolean))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            this.activity?.recreate()
            true
        }

        val editText = findPreference<EditTextPreference>("edit_text_preference")

        if (editText != null) {
            val nick = DataBaseHelper.getNickname()
            checkNickName(nick, editText)

            editText.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                _, newVal ->
                val success = checkNickName(newVal.toString(), editText)
                if (success)
                    DataBaseHelper.setNickname(newVal.toString())

                true
            }
        }
    }

    private fun checkNickName(value: String, editText:  EditTextPreference): Boolean {
        return if(value.trim() == "") {
            editText.summary = "No nickname assigned"
            false
        } else {
            editText.summary = "Current Nickname: $value"
            true
        }
    }
}