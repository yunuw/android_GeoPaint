package edu.uw.yw239.geopaint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    public final static String PARENT_ACTIVITY_KEY = "parent_activity_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // "back" on Action Bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //the FM is the guy who moves fragments around
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference);

            setupPreferenceSummary();
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setupPreferenceSummary();
        }

        private void setupPreferenceSummary() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String penSize = prefs.getString(MapsActivity.PREF_PEN_SIZE, MapsActivity.DEFAULT_SIZE);
            EditTextPreference penSizePref = (EditTextPreference)findPreference(MapsActivity.PREF_PEN_SIZE);
            penSizePref.setSummary(penSize);

            String fileName = prefs.getString(MapsActivity.PREF_FILE_NAME, MapsActivity.DEFAULT_FILE_NAME);
            EditTextPreference fileNamePref = (EditTextPreference)findPreference(MapsActivity.PREF_FILE_NAME);
            fileNamePref.setSummary(fileName);

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public Intent getSupportParentActivityIntent() {

        return getIntent();
    }
}
