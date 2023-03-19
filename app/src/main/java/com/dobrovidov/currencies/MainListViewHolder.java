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

import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

public class MainListViewHolder extends RecyclerView.ViewHolder {
    private RecyclerView.Adapter adapter;

    private FrameLayout frame;
    private View layout;

    private boolean removedBySwipe = false;

    private static int layoutRightPaddingInPx = 0;
    private static int dpInPx = 0;
    static {
        dpInPx = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, Resources.getSystem().getDisplayMetrics());
    }

    public MainListViewHolder(RecyclerView.Adapter adapter, View v) {
        super(v);
        this.adapter = adapter;

        frame = (FrameLayout)v.findViewById(R.id.item);
        layout = v.findViewById(R.id.main_layout);
    }

    public static void setLayoutRightPadding(int px) {
        layoutRightPaddingInPx = px;
    }

    public void elevate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //itemDividerBottom.setVisibility(View.INVISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.bottomMargin = 0;
            layout.setLayoutParams(params);
            if (this instanceof MainListConversionEntryViewHolder) {
                layout.setPadding(0, dpInPx, layoutRightPaddingInPx, dpInPx);
            } else {
                layout.setPadding(0, dpInPx, 0, dpInPx);
            }

            ViewCompat.animate(frame)
                    .translationZ(15)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(null)
                    .setDuration(200)
                    .withLayer()
                    .start();

        } else {
            //itemDividerTop.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.topMargin = dpInPx;
            layout.setLayoutParams(params);
            if (this instanceof MainListConversionEntryViewHolder) {
                layout.setPadding(0, 0, layoutRightPaddingInPx, 0);
            } else {
                layout.setPadding(0, 0, 0, 0);
            }
        }
    }

    public void drop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //itemDividerBottom.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.bottomMargin = dpInPx;
            layout.setLayoutParams(params);
            if (this instanceof MainListConversionEntryViewHolder) {
                layout.setPadding(0, dpInPx, layoutRightPaddingInPx, 0);
            } else {
                layout.setPadding(0, dpInPx, 0, 0);
            }

            ViewCompat.animate(frame)
                    .translationZ(0)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(null)
                    .setDuration(100)
                    .withLayer()
                    .start();

        } else {
            //itemDividerTop.setVisibility(View.INVISIBLE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.topMargin = 0;
            layout.setLayoutParams(params);
            if (this instanceof MainListConversionEntryViewHolder) {
                layout.setPadding(0, dpInPx, layoutRightPaddingInPx, 0);
            } else {
                layout.setPadding(0, dpInPx, 0, 0);
            }
        }
    }

    protected RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public void setRemovedBySwipe(boolean val) {
        removedBySwipe = val;
    }
    public boolean isRemovedBySwipe() {
        return removedBySwipe;
    }
}
