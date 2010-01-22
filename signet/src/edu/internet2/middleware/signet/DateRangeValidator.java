/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/DateRangeValidator.java,v 1.2 2007-07-31 16:51:20 ddonn Exp $

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

import java.util.Date;
import edu.internet2.middleware.signet.ui.Constants;

/**
 * DateRangeValidator validates the given date range
 * 
 */
public class DateRangeValidator
{
	protected Date		setDate;
	protected Date		effDate;
	protected Date		expDate;
	protected boolean	status;
	protected String	errMsgKey;
	protected String	errField;

	/** default constructor */
	protected DateRangeValidator()
	{
		setDate = null;
		status = false;
		errMsgKey = null;
		errField = null;
	}

	/** A date range checker - assumes effDate is a valid start date, checks that
	 * expDate is later than effDate. Call getStatus() to determine status.
	 * Call getErrMsg() to get the error message. Call getErrField() for
	 * the field (effDate or expDate) that contained the error.
	 * @param effDate The effective (start) date. The only check is whether
	 * effDate is null.
	 * @param expDate The expiration date, must be later than effDate.
	 */
	public DateRangeValidator(Date effDate, Date expDate)
	{
		this();
		this.setDate = null;
		this.effDate = effDate;
		this.expDate = expDate;
		status = isValidDateRange(this.effDate, this.expDate);
	}

	/** A date range checker - checks that effDate is a less than or equal to
	 * setDate, checks that
	 * expDate is later than setDate. Call getStatus() to determine status.
	 * Call getErrMsg() to get the error message. Call getErrField() for
	 * the field (effDate or expDate) that contained the error.
	 * @param setDate The date the effDate and expDate must bracket (
	 * effDate <= setDate < expDate)
	 * @param effDate The effective (start) date. The only check is whether
	 * effDate is null.
	 * @param expDate The expiration date, must be later than effDate.
	 */
	public DateRangeValidator(Date setDate, Date effDate, Date expDate)
	{
		this();
		this.setDate = setDate;
		this.effDate = effDate;
		this.expDate = expDate;
		status = isValidDateRange(this.setDate, this.effDate, this.expDate);
	}

	public boolean getStatus()
	{
		return (status);
	}

	public String getErrMsgKey()
	{
		return (errMsgKey);
	}

	public String getErrField()
	{
		return (errField);
	}

	/**
	 * Test whether effectiveDate is before the expirationDate
	 * @param effectiveDate Required Date
	 * @param expirationDate May be null (i.e. 'until revoked') or a Date
	 * @return False if expirationDate is on or before effectiveDate
	 */
	protected boolean isValidDateRange(Date effectiveDate, Date expirationDate)
	{
		boolean retval = false; // assume invalid date range
		if (null != effectiveDate)
		{
			if (null != expirationDate) // null == 'until revoked'
			{
				if ( !(retval = (effectiveDate.compareTo(expirationDate) < 0))) // eff before exp
				{
					errMsgKey = Constants.DATE_RANGE_ERROR_KEY;
					errField = Constants.EXPIRATION_DATE_PREFIX;
				}
			}
			else
				retval = true;
		}
		else
		{
			errMsgKey = Constants.DATE_FORMAT_ERROR_KEY;
			errField = Constants.EFFECTIVE_DATE_PREFIX;
		}

		return (retval);
	}

	/**
	 * Test whether effectiveDate is less than or equal to setDate and less than expirationDate
	 * @param setDate A date to test against effectiveDate; typical value would be "today"
	 * @param effectiveDate Required Date
	 * @param expirationDate May be null (i.e. 'until revoked') or a Date
	 * @return False if expirationDate is on or before effectiveDate or
	 * effective Date is before setDate
	 */
	protected boolean isValidDateRange(Date setDate, Date effectiveDate, Date expirationDate)
	{
		boolean retval = isValidDateRange(effectiveDate, expirationDate);
		if (retval)
		{
			if (retval = (null != setDate))
				retval = (0 <= effectiveDate.compareTo(setDate)); // today, or in future

			if ( !retval)
			{
				errMsgKey = Constants.DATE_RANGE_ERROR_KEY;
				errField = Constants.EFFECTIVE_DATE_PREFIX;
			}
		}

		return (retval);
	}

//	/**
//	 * Compare a Date to an "earliest possible" date
//	 * @param effDate The Date to test
//	 * @param earliestDate The Date to test against, if null, uses "today, midnight"
//	 * @return False if effDate is earlier than earliestDate, or if effDate is null,
//	 * otherwise returns true
//	 */
//	public boolean isValidEarliestDate(Date effDate, DateOnly earliestDate)
//	{
//		boolean retval = false; // assume failure
//
//		if (null != effDate)
//		{
//			if ( !(effDate instanceof DateOnly)) // truncate to midnight
//				effDate = new DateOnly(effDate.getTime());
//			if (null == earliestDate)
//				earliestDate = new DateOnly();
//			retval = (0 <= effDate.compareTo(earliestDate));
//		}
//
//		return (retval);
//	}
//
}
