package com.restart.spacestationtracker;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.restart.spacestationtracker.services.Alert;

public class Preferences extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_general);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            SettingsFragment settingsFragment = new SettingsFragment();
            fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS_FRAGMENT");
            fragmentTransaction.commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        private PreferenceScreen mPreferenceScreen;
        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_preferences);

            mPreferenceScreen = getPreferenceScreen();
            mContext = getActivity().getApplicationContext();

            // Enable the seekbar
            mPreferenceScreen.findPreference("refresh_Rate").setEnabled(true);

            // Onclick methods for each of the check boxes
            mPreferenceScreen.findPreference("advertisement").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean checked = preference.getSharedPreferences().getBoolean("advertisement", false);

                    if (!checked) {
                        Toast.makeText(mContext, R.string.startBannerAds, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });

            // Onclick methods for each of the check boxes
            mPreferenceScreen.findPreference("fullPage").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean checked = preference.getSharedPreferences().getBoolean("fullPage", false);

                    if (!checked) {
                        Toast.makeText(mContext, R.string.startFullAds, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });

            mPreferenceScreen.findPreference("notification_ISS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!isLocationPermissionGranted()) {
                        getLocationPermission();
                        return true;
                    } else {
                        boolean checked = preference.getSharedPreferences().getBoolean("notification_ISS", false);
                        iss_Service(checked);
                        return true;
                    }
                }
            });
        }

        /**
         * Start or stop the Alert.java service.
         *
         * @param checked Are we starting or destroying the service?
         */
        public void iss_Service(boolean checked) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            if (checked) {
                sharedPreferences.edit().putLong("time", 0).apply();
                Toast.makeText(mContext, R.string.startAlert, Toast.LENGTH_SHORT).show();
                mContext.startService(new Intent(mContext, Alert.class));
            } else {
                sharedPreferences.edit().putLong("time", 0).apply();
                Toast.makeText(mContext, R.string.stopAlert, Toast.LENGTH_SHORT).show();
                mContext.stopService(new Intent(mContext, Alert.class));
            }
        }

        /**
         * Get the permissions needed for the Alert.class
         */
        public void getLocationPermission() {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        /**
         * If permission was granted, send the user to the new activity.
         *
         * @param requestCode  For managing requests, in this case it's just 1
         * @param permissions  Would be nice to get internet and location
         * @param grantResults The ACCESS_FINE_LOCATION must to be granted
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
            CheckBoxPreference iss_Tracker = (CheckBoxPreference) mPreferenceScreen.findPreference("notification_ISS");

            if (grantResults.length > 0
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                iss_Tracker.setChecked(false);
            } else {
                iss_Tracker.setChecked(false);
            }
        }

        /**
         * Check to see if user has given us the permission to access their location.
         *
         * @return True or false
         */
        public boolean isLocationPermissionGranted() {
            return Build.VERSION.SDK_INT < 23 || (Build.VERSION.SDK_INT >= 23 && // Need the second Build.Version or it freaks out. Chill java
                    mContext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    mContext.checkSelfPermission(android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED);
        }
    }
}
