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
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class AddConversionActivity extends AppCompatActivity implements CurrenciesListAdapter.OnItemSelectedListener {
    private CurrenciesListAdapter leftAdapter;
    private CurrenciesListAdapter rightAdapter;
    private Snackbar messageSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_conversion);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayList<Currency> currencies = Currencies.getAll(this);

        //adapters
        leftAdapter = new CurrenciesListAdapter(currencies);
        leftAdapter.addOnItemSelectedListener(this);

        rightAdapter = new CurrenciesListAdapter(true, currencies);
        rightAdapter.addOnItemSelectedListener(this);

        if (savedInstanceState != null) {
            int left_selected = savedInstanceState.getInt("LEFT_SELECTED_CURRENCY");
            int right_selected = savedInstanceState.getInt("RIGHT_SELECTED_CURRENCY");

            leftAdapter.setSelectedItemIndex(left_selected);
            rightAdapter.setSelectedItemIndex(right_selected);
        }

        //left list
        RecyclerView left_list = (RecyclerView)findViewById(R.id.left_currency_list);
        left_list.setHasFixedSize(true);
        left_list.setLayoutManager(new LinearLayoutManager(this));

        left_list.setAdapter(leftAdapter);

        //right list
        RecyclerView right_list = (RecyclerView)findViewById(R.id.right_currency_list);
        right_list.setHasFixedSize(true);
        right_list.setLayoutManager(new LinearLayoutManager(this));

        right_list.setAdapter(rightAdapter);
    }

    protected Snackbar createSnackbar() {
        View root = findViewById(R.id.root_layout);

        SnackbarCallback cb = new SnackbarCallback();
        messageSnackbar = Snackbar.make(root, R.string.pair_exists_message, Snackbar.LENGTH_INDEFINITE).setCallback(cb);

        return messageSnackbar;
    }

    protected void showSnackbar() {
        if (messageSnackbar == null || !messageSnackbar.isShownOrQueued()) {
            if (messageSnackbar != null) {
                messageSnackbar.show();
            } else {
                createSnackbar().show();
            }
        }
    }

    protected void dismissSnackbar() {
        if (messageSnackbar != null && messageSnackbar.isShownOrQueued()) {
            messageSnackbar.dismiss();
            messageSnackbar = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_conversion_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_conversion:
                Currency left = leftAdapter.getSelectedItem();
                Currency right = rightAdapter.getSelectedItem();

                if (Conversions.exists(left, right)) {
                    showSnackbar();

                } else {
                    clearAutoupdateNotification();

                    Intent in = new Intent();
                    in.putExtra("left_currency", left);
                    in.putExtra("right_currency", right);

                    setResult(Activity.RESULT_OK, in);
                    finish();
                }
                break;

            //this is needed to override default UP action - UP recreates parent activity, while we
            //simply need to go back up the activity stack (this activity has no intent-filters so it won't be launched as a single instance)
            case android.R.id.home:
                clearAutoupdateNotification();

                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (leftAdapter.getSelectedItem() != null && rightAdapter.getSelectedItem() != null) {
            menu.findItem(R.id.action_create_conversion).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle st) {
        super.onSaveInstanceState(st);
        st.putInt("LEFT_SELECTED_CURRENCY", leftAdapter.getSelectedItemIndex());
        st.putInt("RIGHT_SELECTED_CURRENCY", rightAdapter.getSelectedItemIndex());
    }

    @Override
    public void onItemSelected(Currency item) {
        dismissSnackbar();
        invalidateOptionsMenu();
    }

    private void clearAutoupdateNotification() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("last_autoupdate_notified", true)) {
            //if this flag is set when we PAUSE activity, that means it was set while it was active so we don't need to show snackbar on main activity resuming
            prefs.edit().putBoolean("last_autoupdate_notified", true).apply();
        }
    }

    private class SnackbarCallback extends Snackbar.Callback {
        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            messageSnackbar = null;
        }
    }
}
