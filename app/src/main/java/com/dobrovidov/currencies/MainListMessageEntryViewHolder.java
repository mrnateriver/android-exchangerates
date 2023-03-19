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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainListMessageEntryViewHolder extends MainListViewHolder implements View.OnClickListener {
    private ImageView imageView;
    private TextView textView;
    private Button button;

    public MainListMessageEntryViewHolder(RecyclerView.Adapter adapter, View v) {
        super(adapter, v);

        imageView = (ImageView)v.findViewById(R.id.image_view);
        textView = (TextView)v.findViewById(R.id.text_view);

        button = (Button)v.findViewById(R.id.dismiss_button);
        button.setOnClickListener(this);
    }

    public void setImage(int resId) {
        imageView.setImageResource(resId);
    }

    public void setText(int resId) {
        textView.setText(resId);
    }

    @Override
    public void onClick(View v) {
        //TODO: callback
        final RecyclerView.Adapter ad = getAdapter();
        if (ad instanceof ConversionListAdapter) {
            ((ConversionListAdapter) ad).removeEntry(getAdapterPosition());
        }
    }
}
