/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/DateOnly.java,v 1.1 2007-07-18 17:24:39 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet;

import java.util.Calendar;
import java.util.Date;

/**
 * DateOnly - Wrapper class for java.util.Date. Provides a Date that is
 * normalized to midnight (i.e. truncates a Date's hours, minutes, seconds, millis
 * to 00:00:00.000).
 */
public class DateOnly extends Date
{
	/**
	 * Default Constructor overrides Date()
	 */
	public DateOnly()
	{
		super();
		normalizeToMidnight();
	}

	/**
	 * Constructor overrides Date(long)
	 * @param date
	 */
	public DateOnly(long date)
	{
		super(date);
		normalizeToMidnight();
	}

	/**
	 * Constructor overrides Date(int year, int month, int date, int hrs, int min, int sec)
	 * @param year
	 * @param month
	 * @param date
	 * @param hrs
	 * @param min
	 * @param sec
	 * @deprecated
	 */
	public DateOnly(int year, int month, int date, int hrs, int min, int sec)
	{
		super(year, month, date, hrs, min, sec);
		normalizeToMidnight();
	}

	/**
	 * Constructor overrides Date(int year, int month, int date, int hrs, int min)
	 * @param year
	 * @param month
	 * @param date
	 * @param hrs
	 * @param min
	 * @deprecated
	 */
	public DateOnly(int year, int month, int date, int hrs, int min)
	{
		super(year, month, date, hrs, min);
		normalizeToMidnight();
	}

	/**
	 * Constructor overrides Date(int year, int month, int date)
	 * @param year
	 * @param month
	 * @param date
	 * @deprecated
	 */
	public DateOnly(int year, int month, int date)
	{
		super(year, month, date);
		normalizeToMidnight();
	}

	/**
	 * Constructor overrides Date(String s)
	 * @param s
	 * @deprecated
	 */
	public DateOnly(String s)
	{
		super(s);
		normalizeToMidnight();
	}


	/**
	 * Overrides Date.setHours and always sets hours to 0 (midnight)
	 * @see java.util.Date#setHours(int)
	 */
	public void setHours(int hours)
	{
		setSomething(Calendar.HOUR_OF_DAY, hours);
	}

	/**
	 * Overrides Date.setMinutes and always sets minutes to 0 (on-the-hour)
	 * @see java.util.Date#setMinutes(int)
	 */
	public void setMinutes(int minutes)
	{
		setSomething(Calendar.MINUTE, minutes);
	}

	/**
	 * Overrides Date.setSeconds and always set seconds to 0 (on-the-minute)
	 * @see java.util.Date#setSeconds(int)
	 */
	public void setSeconds(int seconds)
	{
		setSomething(Calendar.SECOND, seconds);
	}


	/* (non-Javadoc)
	 * @see java.util.Date#setTime(long)
	 */
	public void setTime(long time)
	{
		super.setTime(time);
		normalizeToMidnight();
	}

	/**
	 * Normalize this Date to midnight, for use where Signet creates xxxDate,
	 * versus where xxxDatetime is used.
	 */
	public long normalizeToMidnight()
	{
		// Normalize a Date to midnight
		Calendar cal = Calendar.getInstance(); // get 'now'
		cal.setTimeInMillis(super.getTime());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		// calling Calendar.get... forces a Calendar re-calc after all 'sets' are completed
		long time = cal.getTimeInMillis();
		super.setTime(time); // don't call this.setTime()!!!
		return (super.getTime());
	}

	/**
	 * Normalize an arbitrary Date to midnight, for use where Signet creates xxxDate,
	 * versus where xxxDatetime is used.
	 * @param date The Date to normalize, if null, defaults to 'today'
	 */
	public Date normalizeDateToMidnight(Date date)
	{
		// Normalize a Date to midnight
		Calendar cal = Calendar.getInstance(); // get 'now'
		if (null != date) // use provided Date, otherwise time is 'today'
			cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		// calling 'get' forces a Calendar re-calc after all 'sets' are completed
		return (cal.getTime());
	}


	/**
	 * Used internally by the overriding methods for setting hours, mins, secs
	 * @param thingToSet One of Calendar.
	 * @param value
	 */
	protected void setSomething(int thingToSet, int value)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(super.getTime());
		cal.set(thingToSet, value);
		setTime(cal.getTimeInMillis());
	}

}
