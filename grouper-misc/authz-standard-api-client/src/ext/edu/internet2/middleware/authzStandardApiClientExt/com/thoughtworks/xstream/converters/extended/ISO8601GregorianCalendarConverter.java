/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright (C) 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 03. October 2005 by Joerg Schaible
 */
package edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.extended;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.ConversionException;
import edu.internet2.middleware.authzStandardApiClientExt.com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;


/**
 * A GregorianCalendarConverter conforming to the ISO8601 standard.
 * http://www.iso.ch/iso/en/CatalogueDetailPage.CatalogueDetail?CSNUMBER=26780
 * 
 * @author Mauro Talevi
 * @author J&ouml;rg Schaible
 * @since 1.1.3
 */
public class ISO8601GregorianCalendarConverter extends AbstractSingleValueConverter {
    private static final DateTimeFormatter[] formattersUTC = new DateTimeFormatter[]{
            ISODateTimeFormat.dateTime(), 
            ISODateTimeFormat.dateTimeNoMillis(),
            ISODateTimeFormat.basicDateTime(), 
            ISODateTimeFormat.basicOrdinalDateTime(),
            ISODateTimeFormat.basicOrdinalDateTimeNoMillis(), 
            ISODateTimeFormat.basicTime(),
            ISODateTimeFormat.basicTimeNoMillis(), 
            ISODateTimeFormat.basicTTime(),
            ISODateTimeFormat.basicTTimeNoMillis(), 
            ISODateTimeFormat.basicWeekDateTime(),
            ISODateTimeFormat.basicWeekDateTimeNoMillis(), 
            ISODateTimeFormat.ordinalDateTime(),
            ISODateTimeFormat.ordinalDateTimeNoMillis(), 
            ISODateTimeFormat.time(),
            ISODateTimeFormat.timeNoMillis(), 
            ISODateTimeFormat.tTime(),
            ISODateTimeFormat.tTimeNoMillis(), 
            ISODateTimeFormat.weekDateTime(),
            ISODateTimeFormat.weekDateTimeNoMillis(),};
    private static final DateTimeFormatter[] formattersNoUTC = new DateTimeFormatter[]{
            ISODateTimeFormat.basicDate(), 
            ISODateTimeFormat.basicOrdinalDate(),
            ISODateTimeFormat.basicWeekDate(), 
            ISODateTimeFormat.date(),
            ISODateTimeFormat.dateHour(), 
            ISODateTimeFormat.dateHourMinute(),
            ISODateTimeFormat.dateHourMinuteSecond(),
            ISODateTimeFormat.dateHourMinuteSecondFraction(),
            ISODateTimeFormat.dateHourMinuteSecondMillis(), 
            ISODateTimeFormat.hour(),
            ISODateTimeFormat.hourMinute(), 
            ISODateTimeFormat.hourMinuteSecond(),
            ISODateTimeFormat.hourMinuteSecondFraction(),
            ISODateTimeFormat.hourMinuteSecondMillis(), 
            ISODateTimeFormat.ordinalDate(),
            ISODateTimeFormat.weekDate(), 
            ISODateTimeFormat.year(), 
            ISODateTimeFormat.yearMonth(),
            ISODateTimeFormat.yearMonthDay(), 
            ISODateTimeFormat.weekyear(),
            ISODateTimeFormat.weekyearWeek(), 
            ISODateTimeFormat.weekyearWeekDay(),};

    public boolean canConvert(Class type) {
        return type.equals(GregorianCalendar.class);
    }

    public Object fromString(String str) {
        for (int i = 0; i < formattersUTC.length; i++) {
            DateTimeFormatter formatter = formattersUTC[i];
            try {
                DateTime dt = formatter.parseDateTime(str);
                Calendar calendar = dt.toCalendar(Locale.getDefault());
                calendar.setTimeZone(TimeZone.getDefault());
                return calendar;
            } catch (IllegalArgumentException e) {
                // try with next formatter
            }
        }
        String timeZoneID = TimeZone.getDefault().getID();
        for (int i = 0; i < formattersNoUTC.length; i++) {
            try {
                DateTimeFormatter formatter = formattersNoUTC[i].withZone(DateTimeZone.forID(timeZoneID));
                DateTime dt = formatter.parseDateTime(str);
                Calendar calendar = dt.toCalendar(Locale.getDefault());
                calendar.setTimeZone(TimeZone.getDefault());
                return calendar;
            } catch (IllegalArgumentException e) {
                // try with next formatter
            }
        }
        throw new ConversionException("Cannot parse date " + str);
    }

    public String toString(Object obj) {
        DateTime dt = new DateTime(obj);
        return dt.toString(formattersUTC[0]);
    }
}
