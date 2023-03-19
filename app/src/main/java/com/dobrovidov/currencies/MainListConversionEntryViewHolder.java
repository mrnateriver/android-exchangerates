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
import android.widget.ImageButton;
import android.widget.TextView;

public class MainListConversionEntryViewHolder extends MainListViewHolder implements View.OnClickListener {
    private TextView leftTitle;
    private TextView leftSubtitle;
    private TextView rightTitle;
    private TextView rightSubtitle;
    private TextView dynamicsIndicator;
    private ImageButton swapButton;

    public MainListConversionEntryViewHolder(RecyclerView.Adapter adapter, View v) {
        super(adapter, v);

        leftTitle = (TextView)v.findViewById(R.id.left_currency);
        leftSubtitle = (TextView)v.findViewById(R.id.left_currency_subtitle);
        rightTitle = (TextView)v.findViewById(R.id.right_currency);
        rightSubtitle = (TextView)v.findViewById(R.id.right_currency_subtitle);
        dynamicsIndicator = (TextView)v.findViewById(R.id.dynamics_indicator);

        swapButton = (ImageButton)v.findViewById(R.id.swap_button);
        swapButton.setOnClickListener(this);
    }

    public void setLeftTitle(CharSequence leftTitle) {
        this.leftTitle.setText(leftTitle);
    }

    public void setLeftSubtitle(String leftSubtitle) {
        this.leftSubtitle.setText(leftSubtitle);
    }

    public void setRightTitle(CharSequence rightTitle) {
        this.rightTitle.setText(rightTitle);
    }
    public void appendRightTitle(CharSequence rightTitle) {
        this.rightTitle.append(rightTitle);
    }

    public void setRightSubtitle(String rightSubtitle) {
        this.rightSubtitle.setText(rightSubtitle);
    }

    public void setLeftCurrency(Currency currency) {
        setLeftTitle("1 " + currency.getCode());
        setLeftSubtitle(currency.getTitle());
    }

    public void setRightCurrency(Currency currency) {
        setRightTitle(currency.getCode());
        setRightSubtitle(currency.getTitle());
    }

    public void setCurrencies(Currency left, Currency right) {
        setLeftCurrency(left);
        setRightCurrency(right);
    }

    public void setConversion(Conversion conversion) {
        setCurrencies(conversion.getLeftCurrency(), conversion.getRightCurrency());
    }

    public void setSwapButtonEnabled(boolean enabled) {
        swapButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    public void setPositiveDynamics() {
        dynamicsIndicator.setTextColor(itemView.getResources().getColor(R.color.positiveDynamicsColor));
        dynamicsIndicator.setText(R.string.up_arrow);

        dynamicsIndicator.setVisibility(View.VISIBLE);
    }
    public void setNegativeDynamics() {
        dynamicsIndicator.setTextColor(itemView.getResources().getColor(R.color.negativeDynamicsColor));
        dynamicsIndicator.setText(R.string.down_arrow);

        dynamicsIndicator.setVisibility(View.VISIBLE);
    }
    public void hideDynamics() {
        dynamicsIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        MainListEntries.swap(getAdapterPosition());
    }
}
