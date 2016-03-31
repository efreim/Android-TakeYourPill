package pl.balazinski.jakub.takeyourpill.presentation.fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.text.InputFilter;
import android.text.Spanned;

import pl.balazinski.jakub.takeyourpill.R;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
