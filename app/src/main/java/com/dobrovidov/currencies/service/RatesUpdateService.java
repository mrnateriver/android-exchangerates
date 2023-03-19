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
 
package com.dobrovidov.currencies.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dobrovidov.currencies.ExchangeRates;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RatesUpdateService extends IntentService {

    public RatesUpdateService() {
        super("RatesUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("UPDATE_SERVICE", "UPDATE_SERVICE: " + intent.toString());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs != null) {
            boolean only_wifi = prefs.getBoolean("pref_autoupdate_wifi_only", false);

            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo i = conMgr.getActiveNetworkInfo();
            if (!only_wifi || (i != null && i.getType() == ConnectivityManager.TYPE_WIFI)) {
                ExchangeRates.initialize(this); //we have to load all current rates in order to properly calculate dynamics

                boolean notified = prefs.getBoolean("last_autoupdate_notified", true);
                if (notified) {
                    ExchangeRates.update(null, true);
                } else {
                    ExchangeRates.update(null, false);
                }

                //it's bullshit that simple datetime formatting takes so many fucking lines of code, but there's no choice
                String update_datetime = "";
                Date dt = new Date();
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                if (df instanceof SimpleDateFormat) {
                    SimpleDateFormat sdf = (SimpleDateFormat)df;

                    String pattern = sdf.toPattern().replaceAll("y+","yyyy");
                    sdf.applyPattern(pattern);

                    update_datetime = sdf.format(dt);
                } else {
                    update_datetime = df.format(dt);
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("last_autoupdate_notified", false);
                editor.putString("last_autoupdate_date", update_datetime);
                editor.commit();
            }
        }
    }

    public static void initialize(Context context) {
        update(context, false, -1);
    }

    public static void update(Context context, boolean old_autoupdate, int old_interval) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            boolean autoupdate = prefs.getBoolean("pref_autoupdate", false);
            int interval = Integer.valueOf(prefs.getString("pref_autoupdate_interval", "0"));

            if (autoupdate != old_autoupdate || interval != old_interval) {
                Context appContext = context.getApplicationContext();

                Intent intent = new Intent(appContext, RatesUpdateAlarmReceiver.class);
                final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, RatesUpdateAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarm = (AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);
                if (!autoupdate || (interval != old_interval)) {
                    Log.d("UPDATE_SERVICE", "CANCEL INTENT: " + pIntent.toString());
                    alarm.cancel(pIntent);
                }
                if (autoupdate) {
                    Log.d("UPDATE_SERVICE", "SET INEXACT REPEATING: " + interval);
                    alarm.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + interval, interval, pIntent);
                }
            }
        }
    }
}
