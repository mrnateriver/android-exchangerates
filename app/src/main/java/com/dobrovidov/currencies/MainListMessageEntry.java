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
import android.support.v7.widget.RecyclerView;

public class MainListMessageEntry extends MainListEntry {
    private String imageResource;
    private String textResource;

    public MainListMessageEntry(String image, String text) {
        super(MainListEntryType.MESSAGE);

        imageResource = image;
        textResource = text;
    }

    public String getImageResource() {
        return imageResource;
    }

    public String getTextResource() {
        return textResource;
    }

    @Override
    public void bind(RecyclerView.ViewHolder holder) {
        if (holder instanceof MainListMessageEntryViewHolder) {
            MainListMessageEntryViewHolder hl = (MainListMessageEntryViewHolder)holder;

            Context context = hl.itemView.getContext();
            int textRes = context.getResources().getIdentifier(textResource, "string", context.getPackageName());
            int imageRes = context.getResources().getIdentifier(imageResource, "drawable", context.getPackageName());

            hl.setImage(imageRes);
            hl.setText(textRes);
        }
    }
}
