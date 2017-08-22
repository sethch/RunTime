package com.app.seth.runningapp.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class EditTextPreference extends android.preference.EditTextPreference {
    @SuppressWarnings("unused")
    public EditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("unused")
    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public EditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String summary = sharedPreferences.getString("pref_display_name", "");
        return String.format(summary, getText());
    }
}