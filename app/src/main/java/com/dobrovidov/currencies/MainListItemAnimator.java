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

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.animation.Interpolator;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class MainListItemAnimator extends SlideInLeftAnimator {
    public MainListItemAnimator(Interpolator interpolator) {
        super(interpolator);
    }

    @Override protected void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        if (holder instanceof MainListViewHolder) {
            if (((MainListViewHolder) holder).isRemovedBySwipe()) {
                //taken from FadeInAnimator
                ViewCompat.animate(holder.itemView)
                        .alpha(0)
                        .setDuration(getRemoveDuration())
                        .setInterpolator(mInterpolator)
                        .setListener(new DefaultRemoveVpaListener(holder))
                        .setStartDelay(getRemoveDelay(holder))
                        .start();
            } else {
                super.animateRemoveImpl(holder);
            }
        }
    }
}
