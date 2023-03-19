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

import java.util.ArrayList;

public class Conversions {
    private static ArrayList<String> cachedConversions = new ArrayList<>(10);

    public static void add(String code) {
        cachedConversions.add(code);
    }

    public static void remove(String code) {
        cachedConversions.remove(code);
    }

    public static String[] getAllCodes() {
        return cachedConversions.toArray(new String[cachedConversions.size()]);
    }

    public static boolean exists(String code) {
        for (String s : cachedConversions) {
            if (s.equals(code)) {
                return true;
            }
        }
        return false;
    }
    public static boolean exists(String left, String right) {
        return exists(left + right);
    }
    public static boolean exists(Conversion c) {
        return exists(c.toString());
    }
    public static boolean exists(Currency left, Currency right) {
        return exists(left.getCode() + right.getCode());
    }

    public static int count() {
        return cachedConversions.size();
    }

    public static void replace(String old, String new_code) {
        for (int i = 0; i < cachedConversions.size(); i++) {
            String conv = cachedConversions.get(i);
            if (conv.equals(old)) {
                cachedConversions.set(i, new_code);
                break;
            }
        }
    }
}
