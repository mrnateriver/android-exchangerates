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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RatesUpdateAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = "fuckgoogle".hashCode();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d("UPDATE_SERVICE", "BROADCAST_RECEIVE: " + intent.toString());
        }

        Intent update = new Intent(context, RatesUpdateService.class);
        context.startService(update);
    }
}
