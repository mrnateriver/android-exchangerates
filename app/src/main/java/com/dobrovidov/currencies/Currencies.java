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
import android.content.res.XmlResourceParser;
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Locale;

public class Currencies {
    private static ArrayList<Currency> cache;

    public static ArrayList<Currency> getAll(Context context) {
        if (cache == null) {
            cache = new ArrayList<>(10);
            try {
                //get local one to push it to the top
                java.util.Currency localeCur = java.util.Currency.getInstance(Locale.getDefault());
                String localeCurCode = localeCur.getCurrencyCode();
                Currency localCurrency = null;

                XmlResourceParser xrp = context.getResources().getXml(R.xml.currencies);

                while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {

                    if (xrp.getEventType() == XmlPullParser.START_TAG && xrp.getName().equals("Currency")) {
                        String title = xrp.getAttributeValue(null, "title");
                        String code = xrp.getAttributeValue(null, "code");
                        String sep = xrp.getAttributeValue(null, "separator");

                        Currency c = new Currency(title, code);
                        if (sep != null && (sep.equalsIgnoreCase("true") || sep.equals("1"))) {
                            c.setSeparator(true);
                        }
                        if (code.equals(localeCurCode)) {
                            localCurrency = c;
                        } else {
                            cache.add(c);
                        }
                    }

                    xrp.next();
                }

                if (localCurrency != null) {
                    cache.add(0, localCurrency);
                }

            } catch (Throwable e) {
                //dummy
            }
        }
        return cache;
    }

    public static ArrayList<Currency> getInitial(Context context) {
        ArrayList<Currency> result = new ArrayList<>(10);
        try {
            XmlResourceParser xrp = context.getResources().getXml(R.xml.currencies);

            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {

                if (xrp.getEventType() == XmlPullParser.START_TAG && xrp.getName().equals("Currency")) {
                    String init = xrp.getAttributeValue(null, "initial");
                    if (init != null && (init.equalsIgnoreCase("true") || init.equals("1"))) {
                        String title = xrp.getAttributeValue(null, "title");
                        String code = xrp.getAttributeValue(null, "code");
                        String sep = xrp.getAttributeValue(null, "separator");

                        Currency c = new Currency(title, code);
                        if (sep != null && (sep.equalsIgnoreCase("true") || sep.equals("1"))) {
                            c.setSeparator(true);
                        }
                        result.add(c);
                    }
                }

                xrp.next();
            }
        } catch (Throwable e) {
            //dummy
        }
        return result;
    }

    @Nullable public static Currency getByCode(String code, Context context) {
        if (cache == null) {
            getAll(context);
        }
        for (Currency c : cache) {
            if (c.getCode().equals(code)) {
                return c;
            }
        }
        return null;
    }

    @Nullable public static Currency getLocal(Context context) {
        java.util.Currency localeCur = java.util.Currency.getInstance(Locale.getDefault());
        return getByCode(localeCur.getCurrencyCode(), context);
    }

}
