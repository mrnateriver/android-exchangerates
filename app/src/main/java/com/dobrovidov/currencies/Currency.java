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

import android.os.Parcel;
import android.os.Parcelable;

public class Currency implements Parcelable {
    private final String title;
    private final String code;
    private boolean separator = false;

    private Currency(Parcel in) {
        title = in.readString();
        code = in.readString();
        separator = in.readByte() != 0;
    }

    public Currency(String t, String c) {
        title = t;
        code = c;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean val) {
        separator = val;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(code);
        dest.writeByte((byte)(separator ? 1 : 0));
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel source) {
            return new Currency(source);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };
}
