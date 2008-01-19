/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * @author mchyzer
 *
 */
public class GrouperServiceUtils {
	
	/**
	 * web service format string
	 */
	private static final String WS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	/**
	 * convert a date to a string using the standard web service pattern
	 * Note that HH is 0-23
	 * @param date
	 * @return the string, or null if the date is null
	 */
	public static String dateToString(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
		return simpleDateFormat.format(date);
	}
	
	/**
	 * convert a string to a date using the standard web service pattern
	 * Note that HH is 0-23
	 * @param dateString
	 * @return the string, or null if the date was null
	 */
	public static Date stringToDate(String dateString) {
		if (dateString == null) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(WS_DATE_FORMAT);
		try {
			return simpleDateFormat.parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException("Cannot convert '" + dateString 
					+ "' to a date based on format: " + WS_DATE_FORMAT, e);
		}
	}
		
	/**
	 * assert like java 1.4 assert
	 * 
	 * @param isTrue
	 * @param reason
	 */
	public static void assertTrue(boolean isTrue, String reason) {
		if (!isTrue) {
			throw new RuntimeException(reason);
		}
	}

	/**
	 * print out various types of objects
	 * @param object
	 * @return the string value
	 */
	public static String toStringForLog(Object object) {
		if (object == null) {
			return "null";
		}
		//handle arrays
		if (object.getClass().isArray()) {
			StringBuilder result = new StringBuilder();
			int length = Array.getLength(object);
			for (int i=0;i<length;i++) {
				result.append("[").append(i).append("]: ").append(Array.get(object, i)).append("\n");
			}
			return result.toString();
			 
		}
		return object.toString();
	}

	/**
	 * split a string and trim each
	 * @param string
	 * @param separator
	 * @return the array
	 */
	@SuppressWarnings("unused")
	public static String[] splitTrim(String string, String separator) {
		String[] splitArray = StringUtils.split(string, separator);
		if (splitArray == null) {
			return null;
		}
		int index = 0;
		for (String stringInArray : splitArray) {
			splitArray[index++] = StringUtils.trimToNull(stringInArray);
		} 
		return splitArray;
	}
}
