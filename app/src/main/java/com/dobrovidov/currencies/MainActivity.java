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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dobrovidov.currencies.service.RatesUpdateService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        ExchangeRates.OnRatesUpdatedStateListener, ConversionListAdapter.OnItemRemovedListener {

    private static final int ACTIVITY_SELECT_CURRENCIES_FOR_NEW_CONVERSION = 1;
    private static final int ACTIVITY_SETTINGS = 2;

    private static final int SNACKBAR_UPDATE_RATES = 1;
    private static final int SNACKBAR_UPDATE_ERROR = 2;
    private static final int SNACKBAR_UPDATE_DATE = 3;

    private CoordinatorLayout rootLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView conversionsList;
    private ConversionListAdapter conversionAdapter;
    private View emptyListText;

    private SnackbarManager snackbarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        ExchangeRates.initialize(this);
        MainListEntries.initialize(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_launch", true)) {
            RatesUpdateService.initialize(this);
            prefs.edit().putBoolean("first_launch", false).commit();

            //add initial messages and conversions
            MainListEntries.add(new MainListMessageEntry("conversion_drag_tutorial", "tutorial_drag"));

            ArrayList<Currency> initialCurrencies = Currencies.getInitial(this);
            if (initialCurrencies.size() > 0) {
                Currency rightCurrency = Currencies.getLocal(this);
                if (rightCurrency != null) {
                    for (Currency c : initialCurrencies) {
                        if (!c.getCode().equals(rightCurrency.getCode())) {
                            //don't add 1=1 conversion
                            MainListEntries.add(new MainListConversionEntry(c, rightCurrency));
                        }
                    }
                }
            }

            MainListEntries.add(new MainListMessageEntry("conversion_swipe_tutorial", "tutorial_swipe"));
        }

        //now build interface
        setContentView(R.layout.activity_main);

        rootLayout = (CoordinatorLayout) findViewById(R.id.root_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //snackbars
        snackbarManager = new SnackbarManager();

        //conversion list
        conversionsList = (RecyclerView) findViewById(R.id.conversion_list);
        conversionsList.setLayoutManager(new LinearLayoutManager(this));

        conversionAdapter = new ConversionListAdapter(this);
        conversionAdapter.setFloatingActionButton(fab);
        conversionsList.setAdapter(conversionAdapter);

        MainListEntries.attachListView(conversionsList);

        //empty list text
        emptyListText = findViewById(R.id.empty_conversion_list_text);

        //swipe layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_light);

        //empty list message
        if (MainListEntries.count() < 1) {
            swipeRefreshLayout.setEnabled(false);
            snackbarManager.showStaticNoAnimation(R.string.empty_conversion_list_message);
            emptyListText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("last_autoupdate_notified", true)) {
            //if this flag is set when we PAUSE activity, that means it was set while it was active so we don't need to show snackbar on resume
            prefs.edit().putBoolean("last_autoupdate_notified", true).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("last_autoupdate_notified", true)) {
            if (MainListEntries.count() > 0) {
                String date = prefs.getString("last_autoupdate_date", "");
                if (date.length() > 0) {
                    snackbarManager.show(SNACKBAR_UPDATE_DATE, getResources().getText(R.string.last_update_date_message) + " " + date, Snackbar.LENGTH_LONG);
                }
                conversionAdapter.notifyDataSetChanged();
            }
            prefs.edit().putBoolean("last_autoupdate_notified", true).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent in = new Intent(this, SettingsActivity.class);
                startActivityForResult(in, ACTIVITY_SETTINGS);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_SELECT_CURRENCIES_FOR_NEW_CONVERSION && resultCode == Activity.RESULT_OK) {
            final Currency left = data.getParcelableExtra("left_currency");
            final Currency right = data.getParcelableExtra("right_currency");
            //delay so that animation plays fully and properly
            conversionsList.postDelayed(new Runnable() {
                @Override
                public void run() {
                    conversionAdapter.addConversion(new Conversion(left, right));
                }
            }, 200);

            swipeRefreshLayout.setEnabled(true);
            emptyListText.setVisibility(View.GONE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    snackbarManager.show(SNACKBAR_UPDATE_RATES, R.string.update_rates_message, R.string.action_update_rates, Snackbar.LENGTH_INDEFINITE, new SnackbarActionListener());
                }
            }, 500);

        } else if (requestCode == ACTIVITY_SETTINGS) {
            //doesn't matter what result is, it's cheap enough to just invalidate list
            conversionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        Intent in = new Intent(this, AddConversionActivity.class);
        startActivityForResult(in, ACTIVITY_SELECT_CURRENCIES_FOR_NEW_CONVERSION);
    }

    @Override
    public void onRefresh() {
        snackbarManager.hide();
        ExchangeRates.update(this);
    }

    @Override
    public void onRatesUpdated() {
        swipeRefreshLayout.setRefreshing(false);
        conversionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRatesUpdateError() {
        //delay error report in case UPDATE_ERROR snackbar hasn't disappeared yet
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                snackbarManager.show(SNACKBAR_UPDATE_ERROR, R.string.update_error_message, R.string.action_retry, Snackbar.LENGTH_INDEFINITE, new SnackbarActionListener());
            }
        }, 500);
    }

    @Override
    public void onItemRemoved() {
        if (Conversions.count() < 1) {
            swipeRefreshLayout.setEnabled(false);
            snackbarManager.showStatic(R.string.empty_conversion_list_message);
        }
        if (MainListEntries.count() < 1) {
            emptyListText.setVisibility(View.VISIBLE);
        }
    }

    private class SnackbarActionListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            swipeRefreshLayout.setRefreshing(true);
            ExchangeRates.update(MainActivity.this);
        }
    }

    private class SnackbarManager {
        private Snackbar snackbar = null;
        private int snackbarType = -1;

        private View staticSnackbar = null;
        private TextView staticSnackbarText = null;
        private ViewPropertyAnimatorCompat staticSnackbarAnimation = null;

        public SnackbarManager() {
            staticSnackbar = MainActivity.this.findViewById(R.id.empty_conversion_list_snackbar);
            if (staticSnackbar != null) {
                staticSnackbarText = (TextView)staticSnackbar.findViewById(R.id.empty_conversion_list_message);
            }
        }

        public void hideStaticNoAnimation() {
            if (staticSnackbar != null && staticSnackbarText != null) {
                staticSnackbarText.setText("");

                staticSnackbar.setTranslationY(staticSnackbar.getHeight());
                staticSnackbarText.setAlpha(0f);
            }
        }

        public void showStaticNoAnimation(String text) {
            if (staticSnackbar != null && staticSnackbarText != null) {
                staticSnackbarText.setText(text);

                staticSnackbar.setTranslationY(0f);
                staticSnackbarText.setAlpha(1f);
            }
        }
        public void showStaticNoAnimation(int textRes) {
            showStaticNoAnimation(MainActivity.this.getResources().getString(textRes));
        }

        public void showStatic(final String text) {
            hideThen(new Runnable() {
                @Override
                public void run() {
                    if (staticSnackbar != null) {
                        staticSnackbarText.setText(text);
                        if (staticSnackbarAnimation != null) {
                            staticSnackbarAnimation.cancel();
                        }
                        staticSnackbarAnimation = ViewCompat.animate(staticSnackbar);
                        staticSnackbarAnimation.translationY(0f)
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .setDuration(250)
                                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(View view) {
                                        ViewCompat.animate(staticSnackbarText).alpha(1f).setDuration(180).setStartDelay(70).start();
                                    }

                                    @Override
                                    public void onAnimationEnd(View view) {
                                        staticSnackbarAnimation = null;
                                    }
                                })
                                .start();
                    }
                }
            });
        }
        public void showStatic(int textRes) {
            showStatic(MainActivity.this.getResources().getString(textRes));
        }

        public void hideStaticThen(final Runnable action) {
            if (isStaticShown()) {
                if (staticSnackbarAnimation != null) {
                    staticSnackbarAnimation.cancel();
                }

                staticSnackbarAnimation = ViewCompat.animate(staticSnackbar);
                staticSnackbarAnimation.translationY(staticSnackbar.getHeight())
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .setDuration(250)
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(View view) {
                                ViewCompat.animate(staticSnackbarText).alpha(0f).setDuration(180).setStartDelay(0).start();
                            }
                            @Override
                            public void onAnimationEnd(View view) {
                                action.run();
                                staticSnackbarAnimation = null;
                            }
                        })
                        .start();
            } else {
                action.run();
            }
        }

        public boolean isStaticShown() {
            return staticSnackbar != null && (staticSnackbar.getTranslationY() == 0 || staticSnackbarAnimation != null);
        }

        public void show(final int type, final String text, final int length) {
            hideStaticThen(new Runnable() {
                @Override
                public void run() {
                    if (snackbarType != type) {
                        snackbar = Snackbar.make(rootLayout, text, length)
                                .setCallback(new Callback(type));
                        snackbarType = type;

                        snackbar.show();
                    }
                }
            });
        }

        public void show(int type, int textRes, int length) {
            show(type, MainActivity.this.getResources().getString(textRes), length);
        }

        public void show(final int type, final String text, final String actionText, final int length, final View.OnClickListener cb) {
            hideStaticThen(new Runnable() {
                @Override
                public void run() {
                    if (snackbarType != type) {
                        snackbar = Snackbar.make(rootLayout, text, length)
                                .setAction(actionText, cb)
                                .setCallback(new Callback(type));
                        snackbarType = type;

                        snackbar.show();
                    }
                }
            });
        }
        public void show(int type, int textRes, int actionTextRes, int length, View.OnClickListener cb) {
            show(type, MainActivity.this.getResources().getString(textRes), MainActivity.this.getResources().getString(actionTextRes), length, cb);
        }
        public void show(int type, String text, int actionTextRes, int length, View.OnClickListener cb) {
            show(type, text, MainActivity.this.getResources().getString(actionTextRes), length, cb);
        }
        public void show(int type, int textRes, String actionText, int length, View.OnClickListener cb) {
            show(type, MainActivity.this.getResources().getString(textRes), actionText, length, cb);
        }

        public void hide() {
            if (snackbar != null) {
                snackbar.dismiss();
                snackbarType = -1;
            }
        }

        public void hideThen(Runnable action) {
            if (isShown()) {
                snackbarType = -1;
                snackbar.setCallback(new ActionCallback(action)).dismiss();
            } else {
                action.run();
            }
        }

        public boolean isShown() {
            return snackbar != null && snackbar.isShownOrQueued();
        }

        private class Callback extends Snackbar.Callback {
            private int type = -1;

            public Callback(int type) {
                this.type = type;
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                snackbar.setCallback(null);
                if (type == snackbarType) {
                    snackbarType = -1;
                }
            }
        }

        private class ActionCallback extends Snackbar.Callback {
            private Runnable action = null;

            public ActionCallback(Runnable a) {
                action = a;
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                snackbar.setCallback(null);
                action.run();
            }
        }
    }
}
