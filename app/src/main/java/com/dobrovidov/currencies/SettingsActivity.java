/*
 * Copyright (c) 2016 Evgenii Dobrovidov
 * This file is part of "Exchange Rates".
 *
 * "Exchange Rates" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * "Exchange Rates" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Exchange Rates".  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dobrovidov.currencies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.dobrovidov.currencies.service.RatesUpdateService;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean prevAutoupdateSetting;
    private int prevAutoupdateIntervalSetting;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTheme(R.style.SettingsFragmentStyle);

        Intent launchIntent = getIntent();
        settingsFragment = new SettingsFragment();
        if (Intent.ACTION_MANAGE_NETWORK_USAGE.equals(launchIntent.getAction())) {
            settingsFragment.disablePrecisionPreference();
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsFragment.getPreferenceScreen()
                .getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (settingsFragment != null) {
            SharedPreferences prefs = settingsFragment.getPreferenceScreen().getSharedPreferences();

            prevAutoupdateSetting = prefs.getBoolean("pref_autoupdate", false);
            prevAutoupdateIntervalSetting = Integer.valueOf(prefs.getString("pref_autoupdate_interval", "0"));
        }

        settingsFragment.getPreferenceScreen()
                .getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                SharedPreferences prefs = settingsFragment.getPreferenceScreen().getSharedPreferences();
                if (!prefs.getBoolean("last_autoupdate_notified", true)) {
                    //if this flag is set when we PAUSE activity, that means it was set while it was active so we don't need to show snackbar on main activity resuming
                    prefs.edit().putBoolean("last_autoupdate_notified", true).apply();
                }

                finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_autoupdate") || key.equals("pref_autoupdate_interval")) {
            RatesUpdateService.update(this, prevAutoupdateSetting, prevAutoupdateIntervalSetting);

            prevAutoupdateSetting = sharedPreferences.getBoolean("pref_autoupdate", false);
            prevAutoupdateIntervalSetting = Integer.valueOf(sharedPreferences.getString("pref_autoupdate_interval", "0"));

        } else if (key.equals("pref_dynamics")) {
            ExchangeRates.setCalculateDynamics(sharedPreferences.getBoolean("pref_dynamics", true));
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        private boolean onlyNetwork = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            if (onlyNetwork) {
                ListPreference precisionList = (ListPreference)findPreference("pref_precision");
                getPreferenceScreen().removePreference(precisionList);
            }
        }

        public void disablePrecisionPreference() {
            onlyNetwork = true;
        }
    }
}
