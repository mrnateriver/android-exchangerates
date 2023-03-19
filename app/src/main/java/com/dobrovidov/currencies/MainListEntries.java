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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class MainListEntries {
    private static RecyclerView listView;
    private static ArrayList<MainListEntry> entries;
    private static Context dbContext;

    public static void attachListView(RecyclerView view) {
        listView = view;
    }

    public static void initialize(Context context) {
        //only initialize on app start
        entries = new ArrayList<>();

        dbContext = context;

        StorageOpenHelper helper = new StorageOpenHelper(dbContext);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT *, " +
                                    "(SELECT id FROM conversions WHERE (conversions.right || conversions.left) = (conv.left || conv.right) AND conversions.id IN " +
                                        "(SELECT conversion_id FROM entries) LIMIT 1) AS swap " +
                                "FROM entries " +
                                "LEFT JOIN messages ON messages.id = entries.message_id " +
                                "LEFT JOIN conversions AS conv ON conv.id = entries.conversion_id " +
                                "ORDER BY sort ASC", null);
        while (c.moveToNext()) {
            MainListEntry entry = null;

            //id | type | sort | message_id | conversion_id | id | text_res | image_res | id | left | right | swap
            int type_value = c.getInt(1);
            if (MainListEntryType.withinRange(type_value)) {
                MainListEntryType type = MainListEntryType.values[type_value];

                if (type == MainListEntryType.MESSAGE) {
                    entry = new MainListMessageEntry(c.getString(7), c.getString(6));
                    entry.setSubtypeID(c.getInt(3));

                } else if (type == MainListEntryType.CONVERSION) {
                    Currency left = Currencies.getByCode(c.getString(9), dbContext);
                    Currency right = Currencies.getByCode(c.getString(10), dbContext);

                    if (left != null && right != null) {
                        entry = new MainListConversionEntry(left, right);
                        entry.setSubtypeID(c.getInt(4));

                        Conversions.add(left.getCode() + right.getCode());
                        if (c.getType(11) != Cursor.FIELD_TYPE_NULL) {
                            ((MainListConversionEntry)entry).disableSwap();
                        }
                    }
                }
            }

            if (entry != null) {
                entry.setBaseID(c.getInt(0));
                entries.add(entry);
            }
        }
        c.close();
        db.close();
    }

    public static ArrayList<MainListEntry> getAll() {
        return entries;
    }

    public static MainListEntry get(int index) {
        return entries.get(index);
    }

    public static void add(MainListEntry entry) {
        add(entry, true);
        if (dbContext != null) {
            StorageOpenHelper helper = new StorageOpenHelper(dbContext);
            SQLiteDatabase db = helper.getWritableDatabase();

            db.beginTransaction();
            try {
                ContentValues base_values = new ContentValues();
                base_values.put("type", entry.getType().ordinal());
                base_values.put("sort", entries.size() - 1);

                boolean success = true;
                if (entry instanceof MainListMessageEntry) {
                    MainListMessageEntry messageEntry = (MainListMessageEntry)entry;

                    ContentValues values = new ContentValues();
                    values.put("text_res", messageEntry.getTextResource());
                    values.put("image_res", messageEntry.getImageResource());

                    long rowid = db.insert("messages", null, values);
                    if (rowid >= 0) {
                        base_values.put("message_id", rowid);
                        entry.setSubtypeID((int)rowid);
                    } else {
                        success = false;
                    }

                } else if (entry instanceof MainListConversionEntry) {
                    Conversion conversion = ((MainListConversionEntry) entry).getConversion();

                    ContentValues values = new ContentValues();
                    values.put("left", conversion.getLeftCurrency().getCode());
                    values.put("right", conversion.getRightCurrency().getCode());

                    long rowid = db.insert("conversions", null, values);
                    if (rowid >= 0) {
                        base_values.put("conversion_id", rowid);
                        entry.setSubtypeID((int) rowid);
                    } else {
                        success = false;
                    }
                }
                if (success) {
                    long rowid = db.replace("entries", null, base_values);
                    entry.setBaseID((int)rowid);

                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }
            db.close();
        }
    }
    public static void add(MainListEntry entry, boolean dont_serialize) {
        entries.add(entry);
        if (entry instanceof MainListConversionEntry) {
            Conversion conversion = ((MainListConversionEntry) entry).getConversion();
            Conversions.add(conversion.toString());

            int index = 0;
            String opposite = conversion.getRightCurrency().getCode() + conversion.getLeftCurrency().getCode();
            for (MainListEntry e : entries) {
                if (e instanceof MainListConversionEntry) {
                    if (((MainListConversionEntry) e).getConversion().toString().equals(opposite)) {
                        //if opposite exists on addition, disable in current as well
                        ((MainListConversionEntry) entry).disableSwap();
                            ((MainListConversionEntry) e).disableSwap();

                        if (listView != null) {
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(index);
                            if (holder != null && holder instanceof MainListConversionEntryViewHolder) {
                                ((MainListConversionEntryViewHolder) holder).setSwapButtonEnabled(false);
                            }
                        }
                        break;
                    }
                }
                index++;
            }
        }
        if (listView != null) {
            listView.getAdapter().notifyItemInserted(entries.size() - 1);
        }
    }

    public static void swap(int index) {
        MainListEntry entry = get(index);
        if (entry != null && entry instanceof MainListConversionEntry) {
            MainListConversionEntry conversionEntry = (MainListConversionEntry)entry;

            Conversion old = conversionEntry.getConversion();
            Conversions.replace(old.toString(), old.getRightCurrency().getCode() + old.getLeftCurrency().getCode());

            ExchangeRates.swapRate(old);

            if (dbContext != null) {
                StorageOpenHelper helper = new StorageOpenHelper(dbContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put("left", old.getRightCurrency().getCode());
                    values.put("right", old.getLeftCurrency().getCode());

                    db.update("conversions", values, "id = ?", new String[]{String.valueOf(conversionEntry.getSubtypeID())});
                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }
                db.close();
            }

            conversionEntry.swap();
            listView.getAdapter().notifyItemChanged(index);
        }
    }

    public static MainListEntry remove(int index) {
        MainListEntry rem = entries.remove(index);

        if (listView != null) {
            listView.getAdapter().notifyItemRemoved(index);
        }

        Conversion clearRate = null;
        if (dbContext != null && rem != null) {
            StorageOpenHelper helper = new StorageOpenHelper(dbContext);
            SQLiteDatabase db = helper.getWritableDatabase();

            db.beginTransaction();
            try {
                String[] subtype_id_args = new String[] { String.valueOf(rem.getSubtypeID()) };
                if (rem.getType() == MainListEntryType.MESSAGE) {
                    db.delete("messages", "id = ?", subtype_id_args);
                    db.delete("entries", "message_id = ?", subtype_id_args);//remove by subtype ID in case several entries link to one subtype

                } else if (rem.getType() == MainListEntryType.CONVERSION) {
                    if (rem instanceof MainListConversionEntry) {
                        clearRate = ((MainListConversionEntry) rem).getConversion();
                        Conversions.remove(clearRate.toString());

                        int otherIndex = 0;
                        String opposite = clearRate.getRightCurrency().getCode() + clearRate.getLeftCurrency().getCode();
                        for (MainListEntry e : entries) {
                            if (e instanceof MainListConversionEntry) {
                                if (((MainListConversionEntry) e).getConversion().toString().equals(opposite)) {
                                    ((MainListConversionEntry) e).enableSwap();
                                    if (listView != null) {
                                        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(otherIndex);
                                        if (holder != null && holder instanceof MainListConversionEntryViewHolder) {
                                            ((MainListConversionEntryViewHolder) holder).setSwapButtonEnabled(true);
                                        }
                                    }
                                    break;
                                }
                            }
                            otherIndex++;
                        }
                    }

                    db.delete("conversions", "id = ?", subtype_id_args);
                    db.delete("entries", "conversion_id = ?", subtype_id_args);//remove by subtype ID in case several entries link to one subtype
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            db.close();
        }
        if (clearRate != null) {
            ExchangeRates.clearRate(clearRate);
        }

        return rem;
    }

    public static void move(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(entries, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(entries, i, i - 1);
            }
        }
    }

    public static int count() {
        return entries.size();
    }

    public static void serialize() {
        if (dbContext != null) {
            int sort = 0;
            StringBuilder valuesBuilder = new StringBuilder();
            for (MainListEntry entry : entries) {
                valuesBuilder.append("(" + entry.getBaseID() + ", " + entry.getType().ordinal() + ", " + (sort++) + ", ");
                if (entry.getType() == MainListEntryType.MESSAGE) {
                    valuesBuilder.append(entry.getSubtypeID() + ", NULL");
                } else if (entry.getType() == MainListEntryType.CONVERSION) {
                    valuesBuilder.append("NULL, " + entry.getSubtypeID());
                }
                valuesBuilder.append("),");
            }
            String values = valuesBuilder.substring(0, valuesBuilder.length() - 1);

            StorageOpenHelper helper = new StorageOpenHelper(dbContext);
            SQLiteDatabase db = helper.getWritableDatabase();

            db.beginTransaction();
            try {
                //we only need to serialize 'entries' table, because subtype tables are only altered by 'add' and 'remove' methods
                db.execSQL("DELETE FROM entries");
                db.execSQL("INSERT INTO entries (id, type, sort, message_id, conversion_id) VALUES " + values);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            db.close();
        }
    }
}
