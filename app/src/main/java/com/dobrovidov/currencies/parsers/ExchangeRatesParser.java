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
 
package com.dobrovidov.currencies.parsers;

import com.dobrovidov.currencies.Conversion;

import java.util.Collection;
import java.util.Map;

public abstract class ExchangeRatesParser {
    public abstract Map<String, Double> fetchBatch(String... conversions) throws UnableToFetchRatesException;

    public Map<String, Double> fetchBatch(String[] left_codes, String[] right_codes) throws UnableToFetchRatesException {
        String[] args = new String[left_codes.length];
        for (int i = 0; i < left_codes.length; i++) {
            args[i] = left_codes[i] + right_codes[i];
        }
        return fetchBatch(args);
    }
    public final Map<String, Double> fetchBatch(Conversion... conversions) throws UnableToFetchRatesException {
        String[] args = new String[conversions.length];
        for (int i = 0; i < conversions.length; i++) {
            args[i] = conversions[i].toString();
        }
        return this.fetchBatch(args);
    }
    public final Map<String, Double> fetchBatch(Collection<? extends Conversion> conversions) throws UnableToFetchRatesException {
        return this.fetchBatch(conversions.toArray(new Conversion[1]));
    }
    public Map<String, Double> fetchBatch(Collection<? extends String> left_codes, Collection<? extends String> right_codes) throws UnableToFetchRatesException {
        return this.fetchBatch(left_codes.toArray(new String[1]), right_codes.toArray(new String[1]));
    }
    public final Map<String, Double> fetchBatch(String left_code, String[] right_codes) throws UnableToFetchRatesException {
        return this.fetchBatch(new String[] {left_code}, right_codes);
    }
    public final Map<String, Double> fetchBatch(String left_code, Collection<? extends String> right_codes) throws UnableToFetchRatesException {
        return this.fetchBatch(new String[] {left_code}, right_codes.toArray(new String[1]));
    }

    public static class UnableToFetchRatesException extends Exception {
        public UnableToFetchRatesException(Throwable throwable) {
            super(throwable);
        }
    }
}
