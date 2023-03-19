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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StorageOpenHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "storage.db";

    public StorageOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE entries (id INTEGER PRIMARY KEY, type INTEGER, sort INTEGER DEFAULT 0, message_id INTEGER UNIQUE, conversion_id INTEGER UNIQUE)");
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY, text_res TEXT, image_res TEXT)");
        db.execSQL("CREATE TABLE conversions (id INTEGER PRIMARY KEY, left TEXT, right TEXT)");
        db.execSQL("CREATE TABLE rates (id INTEGER PRIMARY KEY, conv TEXT UNIQUE, rate REAL, approx INTEGER DEFAULT 0, dynamics REAL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //do nothing yet
    }


}
