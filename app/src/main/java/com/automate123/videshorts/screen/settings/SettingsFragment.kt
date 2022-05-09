package com.automate123.videshorts.screen.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.automate123.videshorts.R
import com.automate123.videshorts.data.Preferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var preferences: Preferences

    private lateinit var duration: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        duration = preferenceManager.findPreference(Preferences::duration.name)!!
        duration.setOnPreferenceChangeListener { _, newValue ->
            preferences.duration = newValue.toString()
            updatePrefs()
            true
        }
        updatePrefs()
    }

    private fun updatePrefs() {
        val seconds = preferences.duration.toInt()
        duration.summary = resources.getQuantityString(R.plurals.seconds, seconds, seconds)
    }
}