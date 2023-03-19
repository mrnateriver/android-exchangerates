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

public class MainListConversionEntry extends MainListEntry {
    private Conversion conversion;
    private boolean swapAllowed = true;

    public MainListConversionEntry(Conversion conversion) {
        super(MainListEntryType.CONVERSION);
        this.conversion = conversion;
    }

    public MainListConversionEntry(Currency left, Currency right) {
        this(new Conversion(left, right));
    }

    public void enableSwap() {
        swapAllowed = true;
    }
    public void disableSwap() {
        swapAllowed = false;
    }

    public Conversion getConversion() {
        return conversion;
    }

    @Override
    public void bind(RecyclerView.ViewHolder holder) {
        if (holder instanceof MainListConversionEntryViewHolder) {
            MainListConversionEntryViewHolder hl = (MainListConversionEntryViewHolder)holder;

            hl.setConversion(conversion);

            hl.setRightTitle(ExchangeRates.getFormattedRate(conversion));
            hl.appendRightTitle(" " + conversion.getRightCurrency().getCode());

            if (swapAllowed) {
                hl.setSwapButtonEnabled(true);
            } else {
                hl.setSwapButtonEnabled(false);
            }

            if (ExchangeRates.shouldCalculateDynamics()) {
                double dyn = ExchangeRates.getDynamics(conversion);
                if (dyn > 0) {
                    hl.setPositiveDynamics();
                } else if (dyn < 0) {
                    hl.setNegativeDynamics();
                } else {
                    hl.hideDynamics();
                }
            } else {
                hl.hideDynamics();
            }
        }
    }

    public void swap() {
        conversion = new Conversion(conversion.getRightCurrency(), conversion.getLeftCurrency());
    }
}
