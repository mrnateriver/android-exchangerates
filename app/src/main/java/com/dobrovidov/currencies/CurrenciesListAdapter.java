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

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

public class CurrenciesListAdapter extends RecyclerView.Adapter<CurrenciesListAdapter.CurrencyViewHolder> {
    private RecyclerView recyclerView;
    private ArrayList<Currency> currencies;

    private boolean rightMode;

    private int selectedItem;
    private OnItemSelectedListener selectedListener;

    private int separatorDividerHeight = 2;
    private int normalDividerHeight = 1;

    public CurrenciesListAdapter() {
        rightMode = false;
        selectedItem = -1;
        currencies = new ArrayList<>();
    }
    public CurrenciesListAdapter(boolean right) {
        this();
        rightMode = right;
    }
    public CurrenciesListAdapter(Collection<? extends Currency> ccs) {
        this(false, ccs);
    }
    public CurrenciesListAdapter(boolean right, Collection<? extends Currency> ccs) {
        this(right);
        currencies.addAll(ccs);
    }

    public void addCurrency(Currency c) {
        currencies.add(c);
        notifyItemInserted(currencies.size() - 1);
    }

    public Currency getSelectedItem() {
        if (selectedItem >= 0 && selectedItem < currencies.size()) {
            return currencies.get(selectedItem);
        } else {
            return null;
        }
    }

    public int getSelectedItemIndex() {
        return selectedItem;
    }

    public void setSelectedItemIndex(int index) {
        if (recyclerView != null && selectedItem >= 0 && selectedItem < currencies.size()) {
            int old_item = selectedItem;
            selectedItem = index;

            notifyItemChanged(old_item);
            if (index >= 0 && index < currencies.size()) {
                notifyItemChanged(index);
            }

        } else if (recyclerView != null && index >= 0 && index < currencies.size()) {
            selectedItem = index;
            notifyItemChanged(index);

        } else {
            selectedItem = index;
        }
    }

    public void addOnItemSelectedListener(OnItemSelectedListener listener) {
        selectedListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
        recyclerView = rv;

        DisplayMetrics mt = rv.getResources().getDisplayMetrics();
        normalDividerHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mt);
        separatorDividerHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mt);
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (rightMode) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversion_list_item_single_right, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversion_list_item_single_left, parent, false);
        }
        return new CurrencyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, int position) {
        Currency c = currencies.get(position);

        holder.title.setText(c.getCode());
        holder.subtitle.setText(c.getTitle());
        if (position == selectedItem) {
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.GONE);
        }
        holder.setSeparated(c.isSeparator());
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Currency item);
    }

    private LayoutTransition createItemTransitions() {
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder((Object)null, PropertyValuesHolder.ofFloat("scaleX", 1, 0), PropertyValuesHolder.ofFloat("scaleY", 1, 0));
        scaleDown.setDuration(300);
        scaleDown.setStartDelay(0);
        scaleDown.setInterpolator(new OvershootInterpolator());

        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder((Object)null, PropertyValuesHolder.ofFloat("scaleX", 0, 1), PropertyValuesHolder.ofFloat("scaleY", 0, 1));
        scaleUp.setDuration(300);
        scaleUp.setStartDelay(300);
        scaleUp.setInterpolator(new OvershootInterpolator());

        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, scaleDown);

        return itemLayoutTransition;
    }

    public class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView subtitle;
        private TextView check;
        private View animatedLayout;

        public CurrencyViewHolder(View v) {
            super(v);

            title = (TextView)v.findViewById(R.id.currency_title);
            subtitle = (TextView)v.findViewById(R.id.currency_subtitle);
            check = (TextView)v.findViewById(R.id.currency_check);
            animatedLayout = v.findViewById(R.id.animated_layout);

            v.setOnClickListener(this);

            ViewGroup av = (ViewGroup)animatedLayout;
            av.setLayoutTransition(createItemTransitions());
        }

        @Override
        public void onClick(View v) {
            int oldItem = selectedItem;
            int newItem = recyclerView.getChildAdapterPosition(v);
            if (newItem != oldItem) {
                selectedItem = newItem;

                //we have to manually hide the check so that user sees animation, and if we can't
                //get VH for that item, that means it's offscreen, in which case we can simply update data set
                CurrencyViewHolder old = (CurrencyViewHolder)recyclerView.findViewHolderForAdapterPosition(oldItem);
                if (old != null && old.check != null) {
                    old.check.setVisibility(View.GONE);
                } else {
                    notifyItemChanged(oldItem);
                }

                check.setVisibility(View.VISIBLE);
                selectedListener.onItemSelected(currencies.get(selectedItem));
            }
        }

        public void setSeparated(boolean val) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) animatedLayout.getLayoutParams();
            if (val) {
                params.bottomMargin = separatorDividerHeight;
            } else {
                params.bottomMargin = normalDividerHeight;
            }
            animatedLayout.setLayoutParams(params);
        }
    }

}
