package com.aaronstacy.thetext.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.aaronstacy.thetext.R;

public final class SettingsFragment extends PreferenceFragmentCompat {
  public static final String TAG = SettingsFragment.class.getSimpleName();

  @Override public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.preferences);
  }

  public static Fragment newInstance() {
    return new SettingsFragment();
  }
}
