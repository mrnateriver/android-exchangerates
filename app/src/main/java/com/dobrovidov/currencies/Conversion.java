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

public class Conversion {
    private final Currency leftCurrency;
    private final Currency rightCurrency;

    public Conversion(Currency left, Currency right) {
        leftCurrency = left;
        rightCurrency = right;
    }

    public Currency getLeftCurrency() {
        return leftCurrency;
    }

    public Currency getRightCurrency() {
        return rightCurrency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o.getClass() == String.class) {
            //special case for comparing with string representation of Conversion object
            return toString().equals(o);
        }
        if (o == null || getClass() != o.getClass()) return false;

        Conversion that = (Conversion) o;
        if (!leftCurrency.getCode().equals(that.leftCurrency.getCode())) {
            return false;
        }
        return rightCurrency.getCode().equals(that.rightCurrency.getCode());
    }

    @Override
    public int hashCode() {
        int result = leftCurrency.getCode().hashCode();
        result = 31 * result + rightCurrency.getCode().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return leftCurrency.getCode() + rightCurrency.getCode();
    }
}
