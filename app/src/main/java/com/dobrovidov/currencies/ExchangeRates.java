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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.dobrovidov.currencies.parsers.ExchangeRatesParser;
import com.dobrovidov.currencies.parsers.YahooParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRates {
    private static ArrayList<String> approximates = new ArrayList<>();
    private static HashMap<String, Double> dynamics = new HashMap<>();
    private static HashMap<String, Double> rates = new HashMap<>();

    private static boolean calculateDynamics = true;

    private static Context dbContext;

    public static void initialize(Context context) {
        setContext(context);

        StorageOpenHelper helper = new StorageOpenHelper(dbContext);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT conv, rate, approx, dynamics FROM rates", null);
        while (c.moveToNext()) {
            String conv = c.getString(0);
            Double rate = c.getDouble(1);
            boolean approx = (c.getInt(2) > 0);
            Double dyn = c.getDouble(3);

            if (conv != null && conv.length() == 6 && (rate >= 0 || rate == -2.0 /*store error state*/)) {
                rates.put(conv, rate);
                if (approx) {
                    approximates.add(conv);
                }
                dynamics.put(conv, dyn);
            }
        }
        c.close();
        db.close();
    }

    public static void setContext(Context context) {
        dbContext = context;
        if (dbContext != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(dbContext);
            calculateDynamics = sharedPref.getBoolean("pref_dynamics", true);
        }
    }

    public static boolean shouldCalculateDynamics() {
        return calculateDynamics;
    }
    public static void setCalculateDynamics(boolean val) {
        calculateDynamics = val;
    }

    public static double getDynamics(Conversion conversion) {
        return getDynamics(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }
    public static double getDynamics(Currency from, Currency to) {
        return getDynamics(from.getCode(), to.getCode());
    }
    public static double getDynamics(String from, String to) {
        return getDynamics(from + to);
    }
    public static double getDynamics(String conversion) {
        Double result = dynamics.get(conversion);
        if (result == null) {
            return 0;
        }
        return result;
    }

    public static double getRate(Conversion conversion) {
        return getRate(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }
    public static double getRate(Currency from, Currency to) {
        return getRate(from.getCode(), to.getCode());
    }
    public static double getRate(String from, String to) {
        Double result = rates.get(from + to);
        if (result == null) {
            return -1;
        }
        return result;
    }

    public static CharSequence getFormattedRate(Conversion conversion) {
        return getFormattedRate(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }
    public static CharSequence getFormattedRate(Currency from, Currency to) {
        return getFormattedRate(from.getCode(), to.getCode());
    }
    public static CharSequence getFormattedRate(String from, String to) {
        double rate = getRate(from, to);
        CharSequence resultSequence = "";
        if (rate > 0) {
            String result = String.valueOf(rate);
            if (dbContext != null) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(dbContext);
                String precision = sharedPref.getString("pref_precision", "2");
                if (!precision.equals("0")) {
                    result = String.format("%."+precision+"f", rate);
                } else {
                    result = String.valueOf(Math.round(rate));
                }
                //check if specified precision is enough
                try {
                    while (Double.valueOf(result.replace(',', '.')) == 0.0d) {
                        int precisionInt = Integer.valueOf(precision);
                        if (precisionInt < 4) {
                            result = String.format("%." + (++precisionInt) + "f", rate);
                            precision = String.valueOf(precisionInt);
                        } else {
                            break;
                        }
                    }
                } finally { }
            }
            if (isApproximateRate(from, to)) {
                result = "~" + result;
            }
            resultSequence = result;

        } else if (rate == -2.0 && dbContext != null) {
            Spannable resultSpan = new SpannableString(dbContext.getString(R.string.na_string));
            resultSpan.setSpan(new ForegroundColorSpan(dbContext.getResources().getColor(R.color.currencyTitleErrorColor)), 0, resultSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            resultSequence = resultSpan;
        }
        return resultSequence;
    }

    public static void clearRate(Conversion conversion) {
        clearRate(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }
    public static void clearRate(Currency from, Currency to) {
        clearRate(from.getCode(), to.getCode());
    }
    public static void clearRate(String from, String to) {
        String key = from + to;
        if (rates.remove(key) != null) {
            //don't update DB if not necessary
            if (dbContext != null) {
                StorageOpenHelper helper = new StorageOpenHelper(dbContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                db.execSQL("DELETE FROM rates WHERE conv = '" + key + "'");
                db.close();
            }
        }
        dynamics.remove(key);
        approximates.remove(key);
    }

    public static void swapRate(Conversion conversion) {
        swapRate(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }
    public static void swapRate(Currency from, Currency to) {
        swapRate(from.getCode(), to.getCode());
    }
    public static void swapRate(String from, String to) {
        String key = from + to;
        if (rates.containsKey(key)) {
            double old_value = rates.get(key);

            double new_value = (double)Math.round((1 / old_value) * 10000d) / 10000d; //1 / old_value;
            String new_key = to + from;

            rates.remove(key);
            rates.put(new_key, new_value);

            approximates.add(new_key);

            double new_dyn = -dynamics.remove(key); //dynamics is simply opposite
            dynamics.put(new_key, new_dyn);

            if (dbContext != null) {
                StorageOpenHelper helper = new StorageOpenHelper(dbContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                db.beginTransaction();
                try {
                    db.delete("rates", "conv = ?", new String[]{key});

                    ContentValues values = new ContentValues();
                    values.put("conv", new_key);
                    values.put("rate", new_value);
                    values.put("approx", 1);
                    values.put("dynamics", new_dyn);

                    db.insert("rates", null, values);
                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }
                db.close();
            }
        }
    }

    public static boolean isApproximateRate(Conversion conversion) {
        return isApproximateRate(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }
    public static boolean isApproximateRate(Currency from, Currency to) {
        return isApproximateRate(from.getCode(), to.getCode());
    }
    public static boolean isApproximateRate(String from, String to) {
        return approximates.contains(from + to);
    }

    public static void update(OnRatesUpdatedStateListener listener) {
        update(listener, true);
    }
    public static void update(OnRatesUpdatedStateListener listener, boolean updateDynamics) {
        if (dbContext != null) {
            ArrayList<String> result = new ArrayList<>(5);

            StorageOpenHelper helper = new StorageOpenHelper(dbContext);
            SQLiteDatabase db = helper.getReadableDatabase();

            Cursor c = db.rawQuery("SELECT DISTINCT(left || right) FROM conversions", null);
            while (c.moveToNext()) {
                result.add(c.getString(0));
            }

            c.close();
            db.close();

            String[] conversions = result.toArray(new String[result.size()]);
            if (conversions.length > 0) {
                Log.d("UPDATE_SERVICE", "ExchangeRates.update(): conversion.length = " + conversions.length);
                //parser is fixed in this version, later chosen by user
                new UpdateRatesTask(new YahooParser(), listener, updateDynamics).execute(conversions);

            } else if (listener != null) {
                listener.onRatesUpdated();
            }
        }
    }

    private static class UpdateRatesTask extends AsyncTask<String, Void, Map<String, Double>> {
        private ExchangeRatesParser parser;
        private OnRatesUpdatedStateListener listener;
        private boolean updateDynamics = true;

        public UpdateRatesTask(ExchangeRatesParser p, OnRatesUpdatedStateListener l) {
            parser = p;
            listener = l;
        }
        public UpdateRatesTask(ExchangeRatesParser p, OnRatesUpdatedStateListener l, boolean updateDynamics) {
            this(p, l);
            this.updateDynamics = updateDynamics;
        }

        @Override
        protected final Map<String, Double> doInBackground(String... params) {
            Map<String, Double> result = null;
            try {
                result = parser.fetchBatch(params);
            } catch (ExchangeRatesParser.UnableToFetchRatesException e) {
                Log.d("UPDATE_SERVICE", "Error while updating rates", e);
                cancel(false);
            }
            return result;
        }

        @Override
        protected void onCancelled(Map<String, Double> result) {
            //assume error
            if (listener != null) {
                listener.onRatesUpdateError();
            }
        }

        @Override
        protected void onPostExecute(Map<String, Double> result) {
            if (rates != null && result != null) {
                Log.d("UPDATE_SERVICE", "ExchangeRates.update() AsyncTask[onPostExecute]: result.length = " + result.size());

                StringBuilder valuesBuilder = new StringBuilder();
                if (calculateDynamics) {
                    for (Map.Entry<String, Double> entry : result.entrySet()) {
                        Log.d("UPDATE_SERVICE", "ExchangeRates.update() AsyncTask[onPostExecute: with dynamics]: key = " + entry.getKey() + " value = " + entry.getValue());

                        double dyn = 0.;
                        Double old_rate = rates.get(entry.getKey());
                        if (old_rate != null) {
                            dyn = entry.getValue() - old_rate;
                        }
                        rates.put(entry.getKey(), entry.getValue());

                        if (updateDynamics) {
                            dynamics.put(entry.getKey(), dyn);
                        } else {
                            double prev_dyn = getDynamics(entry.getKey());
                            dyn = dyn + prev_dyn;
                            dynamics.put(entry.getKey(), dyn);
                        }
                        valuesBuilder.append("('").append(entry.getKey()).append("', ").append(entry.getValue()).append(", 0, ").append(dyn).append("),");
                    }
                } else {
                    for (Map.Entry<String, Double> entry : result.entrySet()) {
                        Log.d("UPDATE_SERVICE", "ExchangeRates.update() AsyncTask[onPostExecute]: key = " + entry.getKey() + " value = " + entry.getValue());

                        rates.put(entry.getKey(), entry.getValue());
                        valuesBuilder.append("('").append(entry.getKey()).append("', ").append(entry.getValue()).append(", 0, 0),");
                    }
                }
                approximates.clear(); //since we update all conversions there will be no approximates anymore

                if (dbContext != null && result.size() > 0) {
                    String values = valuesBuilder.substring(0, valuesBuilder.length() - 1);

                    StorageOpenHelper helper = new StorageOpenHelper(dbContext);
                    SQLiteDatabase db = helper.getWritableDatabase();

                    db.execSQL("INSERT OR REPLACE INTO rates (conv, rate, approx, dynamics) VALUES " + values);
                    db.close();
                }
            }
            if (listener != null) {
                listener.onRatesUpdated();
            }
        }

    }

    public interface OnRatesUpdatedStateListener {
        void onRatesUpdated();
        void onRatesUpdateError();
    }
}
