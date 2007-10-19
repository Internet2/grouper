/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/Util.java,v 1.2 2007-10-19 23:27:11 ddonn Exp $

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
package edu.internet2.middleware.signet.util.xml.adapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.SignetFactory;
import edu.internet2.middleware.signet.TreeNodeImpl;

/**
 * Util<p>
 * Utilities to support Signet XML Adapter and Binder classes.
 * @see edu.internet2.middleware.signet.util.xml.adapter
 * @see edu.internet2.middleware.signet.util.xml.binder
 */
public class Util
{
	protected static Log log = LogFactory.getLog(Util.class);


	public static XMLGregorianCalendar convertTimeToCal(Date time)
	{
		XMLGregorianCalendar retval = null;
		if (null == time)
			return (retval);
		try
		{
			DatatypeFactory dtf = DatatypeFactory.newInstance();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(time);
			retval = dtf.newXMLGregorianCalendar(cal);
		}
		catch (DatatypeConfigurationException e)
		{
			e.printStackTrace();
		}
	
		return (retval);
	}

	public static Date convertCalToTime(XMLGregorianCalendar cal)
	{
		Date retval = null;
		if (null != cal)
			retval = (cal.toGregorianCalendar().getTime());
	
		return (retval);
	}


	public static String convertDateToString(Date date)
	{
		String retval;
		if ((null == date) || (0L == date.getTime()))
			retval = null;
//			retval = DateFormat.getDateTimeInstance().format(new Date(Long.MAX_VALUE));
		else
			retval = DateFormat.getDateTimeInstance().format(date);
		return (retval);
	}

	public static Date convertStringToDate(String dateStr)
	{
//		Date retval = new Date(0L);
		Date retval = null;

		if ((null != dateStr) && (0 < dateStr.length()))
		{
			try { retval = DateFormat.getDateTimeInstance().parse(dateStr); }
			catch (ParseException pe) { log.error(pe); }
		}

//		if (0L == retval.getTime())
//			retval.setTime(Long.MAX_VALUE);

		return (retval);
	}


	public static String getScopePath(TreeNodeImpl treeNode)
	{
		StringBuffer retval = new StringBuffer();

		if (null != treeNode)
		{
			retval.append(treeNode.getTreeId());
			retval.append(SignetFactory.SCOPE_PART_DELIMITER);
			retval.append(treeNode.getId());
		}

		return (retval.toString());
	}

	public static String[] parseScopePath(String scopePath)
	{
		String[] retval;

		if (null != scopePath)
		{
			retval = scopePath.split(SignetFactory.SCOPE_PART_DELIMITER);
		}
		else
			retval = new String[0];

		return (retval);
	}

}
