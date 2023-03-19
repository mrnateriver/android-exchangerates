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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class MainListEntry {
    private int entryID = -1;
    private int subtypeID = -1;

    private MainListEntryType type = MainListEntryType.UNDEFINED;

    public MainListEntry(MainListEntryType type) {
        this.type = type;
    }

    public final int getBaseID() {
        return entryID;
    }
    public final int getSubtypeID() {
        return subtypeID;
    }

    public final void setBaseID(int id) {
        entryID = id;
    }
    public final void setSubtypeID(int id) {
        subtypeID = id;
    }

    public final MainListEntryType getType() {
        return type;
    }

    public abstract void bind(RecyclerView.ViewHolder holder);

    public static RecyclerView.ViewHolder createViewHolder(RecyclerView.Adapter adapter, ViewGroup parent, int viewType) {
        MainListEntryType type = MainListEntryType.values[viewType];
        if (type == MainListEntryType.CONVERSION) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversion_list_item, parent, false);
            return new MainListConversionEntryViewHolder(adapter, itemView);

        } else if (type == MainListEntryType.MESSAGE) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversion_list_message_item, parent, false);
            return new MainListMessageEntryViewHolder(adapter, itemView);

        } else {
            return null;
        }
    }
}
