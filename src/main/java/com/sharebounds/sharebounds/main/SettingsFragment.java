package com.sharebounds.sharebounds.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.sharebounds.sharebounds.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DESIGN_LIST = "design_colors_list";
    private static final String KEYBOARD_LIST = "keyboard_globe_list";
    private static final String[] BUTTON_NAMES = {"app_share_button", "app_rate_button", "app_tut_button",
            "app_tou_button", "app_priv_button", "app_info_button"};

    private static final String PRIV_URL = "WEBSITE";
    private static final String TOU_URL = "WEBSITE";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);

        setupListPreference(DESIGN_LIST);
        setupListPreference(KEYBOARD_LIST);
        setupPreferenceButtons();
    }

    private void setupListPreference(String key) {
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        ListPreference p = (ListPreference) findPreference(key);
        setListPreferenceSummary(p, sharedPreferences.getString(key, ""));
    }

    private void setListPreferenceSummary(ListPreference listPreference, String stringValue) {
        int index = listPreference.findIndexOfValue(stringValue);
        listPreference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
    }

    private void setupPreferenceButtons() {
        final SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        if (settingsActivity == null) return;

        for (int i = 0; i < BUTTON_NAMES.length; i++) {
            Preference.OnPreferenceClickListener onPreferenceClickListener = null;
            switch (i) {
                case 0:
                    onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            ShareUtils.startShareIntent(settingsActivity,
                                    getString(R.string.settings_share_app), true);
                            return true;
                        }
                    };
                    break;
                case 1:
                    onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            ShareUtils.startRateIntent(settingsActivity);
                            return true;
                        }
                    };
                    break;
                case 2:
                    onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                            bottomSheetFragment.show(settingsActivity.getSupportFragmentManager(),
                                    bottomSheetFragment.getTag());
                            return true;
                        }
                    };
                    break;
                case 3:
                    onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            settingsActivity.startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(TOU_URL)));
                            return true;
                        }
                    };
                    break;
                case 4:
                    onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            settingsActivity.startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(PRIV_URL)));
                            return true;
                        }
                    };
                    break;
                case 5:
                    onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            String title = getString(R.string.pref_title_info);
                            Intent ossIntent = new Intent(settingsActivity, OssLicensesMenuActivity.class);
                            ossIntent.putExtra("title", title);
                            startActivity(ossIntent);
                            return true;
                        }
                    };
                    break;
            }
            findPreference(BUTTON_NAMES[i]).setOnPreferenceClickListener(onPreferenceClickListener);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            if (key.equals(DESIGN_LIST)) {
                SettingsActivity settingsActivity = (SettingsActivity) getActivity();
                if (settingsActivity != null) {
                    settingsActivity.reloadActivity();
                }
            }
            else if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                setListPreferenceSummary(listPreference, sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
