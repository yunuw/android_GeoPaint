package edu.uw.yw239.geopaint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
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

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference);
        }

    }

    @Override
    public Intent getSupportParentActivityIntent() {

        return getIntent();
    }
}
