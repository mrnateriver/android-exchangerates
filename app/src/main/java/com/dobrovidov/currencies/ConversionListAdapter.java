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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.support.v7.widget.RecyclerView;

public class ConversionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnItemRemovedListener removedListener;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    public ConversionListAdapter(OnItemRemovedListener listener) {
        removedListener = listener;
    }

    public void setFloatingActionButton(FloatingActionButton f) {
        fab = f;
    }

    public void addConversion(Conversion c) {
        MainListEntries.add(new MainListConversionEntry(c));
    }

    public void removeEntry(int position) {
        MainListEntries.remove(position);

        if (removedListener != null) {
            removedListener.onItemRemoved();
        }

        //now show FAB in case it was hidden and scrolling is no longer available
        if (recyclerView != null) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
            ScrollAwareFABBehavior fabBehavior = (ScrollAwareFABBehavior)lp.getBehavior();
            fabBehavior.animateIn(fab, (CoordinatorLayout)fab.getParent());
        }
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);

        MainListItemAnimator animator = new MainListItemAnimator(new OvershootInterpolator());
        animator.setAddDuration(300);
        rv.setItemAnimator(animator);

        new ItemTouchHelper(new MainListItemTouchHelperCallback()).attachToRecyclerView(rv);

        MainListViewHolder.setLayoutRightPadding(rv.getContext().getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));

        recyclerView = rv;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MainListEntry.createViewHolder(this, parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder hl, int position) {
        MainListEntries.get(position).bind(hl);
    }

    @Override
    public int getItemViewType(int position) {
        return MainListEntries.get(position).getType().ordinal();
    }

    @Override
    public int getItemCount() {
        return MainListEntries.count();
    }

    private void onItemMove(int from, int to) {
        MainListEntries.move(from, to);
        notifyItemMoved(from, to);
    }

    private void onItemMoveSerialize() {
        MainListEntries.serialize();
    }

    private class MainListItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private boolean anythingMoved = false;

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            anythingMoved = true;
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (viewHolder instanceof MainListViewHolder) {
                ((MainListViewHolder) viewHolder).setRemovedBySwipe(true);
            }
            removeEntry(viewHolder.getAdapterPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                if (viewHolder instanceof MainListViewHolder) {
                    ((MainListViewHolder) viewHolder).elevate();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (anythingMoved) {
                onItemMoveSerialize();
                anythingMoved = false;
            }
            if (viewHolder instanceof MainListViewHolder) {
                ((MainListViewHolder) viewHolder).drop();
            }
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    }

    public interface OnItemRemovedListener {
        void onItemRemoved();
    }
}
