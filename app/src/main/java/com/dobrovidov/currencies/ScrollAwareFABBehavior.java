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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import java.util.List;

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    private OvershootInterpolator interpolator = new OvershootInterpolator();
    private boolean mIsAnimatingOut = false;

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
            animateOut(child);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            animateIn(child, coordinatorLayout);
        }
    }

    public void animateOut(final FloatingActionButton button) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        int offset = button.getHeight() + marginBottom;

        //originalTranslation = button.getTranslationY();
        ViewCompat.animate(button)
                  .translationY(offset + 5 /*just in case*/)
                  .setInterpolator(interpolator)
                  .setListener(new ViewPropertyAnimatorListener() {
                      public void onAnimationStart(View view) {
                          mIsAnimatingOut = true;
                      }

                      public void onAnimationCancel(View view) {
                          mIsAnimatingOut = false;
                      }

                      public void onAnimationEnd(View view) {
                          mIsAnimatingOut = false;
                          view.setVisibility(View.GONE);
                      }
                  })
                  .setDuration(300)
                  .withLayer()
                  .start();
    }

    public void animateIn(final FloatingActionButton button, CoordinatorLayout parent) {
        button.setVisibility(View.VISIBLE);
        ViewCompat.animate(button)
                .translationY(getFabTranslationYForAnimationIn(parent, button))
                .setInterpolator(interpolator)
                .setListener(null)
                .setDuration(300)
                .withLayer()
                .start();
    }

    private float getFabTranslationYForAnimationIn(CoordinatorLayout parent, FloatingActionButton fab) {
        /*taken as-is from android.support.design.widget.FloatingActionButton.Behavior.updateFabTranslationForSnackbar*/
        float minOffset = 0;
        final List<View> dependencies = parent.getDependencies(fab);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            final View view = dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout) {
                minOffset = Math.min(minOffset, view.getTranslationY() - view.getHeight());
            }
        }
        return minOffset;
    }
}