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

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class YahooParser extends ExchangeRatesParser {
    @Override
    public Map<String, Double> fetchBatch(String... conversions) throws UnableToFetchRatesException {
        HashMap<String, Double> result = new HashMap<>(conversions.length);
        if (conversions.length > 0) {
            //form query arguments first
            String query = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(";
            StringBuilder builder = new StringBuilder(query);
            for (int i = 0; i < conversions.length; i++) {
                builder.append("%22").append(conversions[i]).append("%22");
                if (i < conversions.length - 1) {
                    builder.append(",");
                }
            }
            query = builder.append(")&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys").toString();

            try {
                InputStream is = null;
                try {
                    URL url = new URL(query);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                        is = conn.getInputStream();

                        //we use DOM here rather than SAX for simplicity, speed is not much relevant here
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                        Document dom = documentBuilder.parse(is);

                        NodeList items = dom.getElementsByTagName("rate");
                        for (int i = 0; i < items.getLength(); i++){
                            Element item = (Element)items.item(i);

                            String id = item.getAttribute("id");
                            Double rate = -2.0;

                            NodeList children = item.getElementsByTagName("Rate");
                            if (children.getLength() > 0) {
                                Element e = (Element)children.item(0);

                                try {
                                    rate = Double.parseDouble(e.getTextContent());
                                } catch (NumberFormatException formatException) {
                                    rate = -2.0;
                                }
                            }

                            result.put(id, rate);
                        }
                    } else {
                        throw new ConnectException();
                    }

                } catch (Exception e) {
                    throw new UnableToFetchRatesException(e);

                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } catch (IOException e) {
                throw new UnableToFetchRatesException(e);
            }
        }
        return result;
    }
}
