/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * @author mchyzer
 *
 */
public class GrouperServiceUtils {
	
	/**
	 * get the boolean value for an object, cant be null or blank
	 * @param object
	 * @param defaultBoolean if object is null or empty
	 * @return the boolean
	 */
	public static boolean booleanValue(Object object) {
		//first handle blanks
		if (nullOrBlank(object) ) {
			throw new RuntimeException("Expecting something which can be converted to boolean, but is null or blank: '" + object + "'");
		}
		//its not blank, just convert
		if (object instanceof Boolean) {
			return (Boolean)object;
		}
		if (object instanceof String) {
			String string = (String)object;
			if (StringUtils.equalsIgnoreCase(string, "true") || StringUtils.equalsIgnoreCase(string, "t")) {
				return true;
			}
			if (StringUtils.equalsIgnoreCase(string, "false") || StringUtils.equalsIgnoreCase(string, "f")) {
				return false;
			}
			throw new RuntimeException("Invalid string to boolean conversion: '" 
					+ string + "' expecting true|false or t|f case insensitive");
			
		}
		throw new RuntimeException("Cant convert object to boolean: " + object.getClass());

	}
	
	/**
	 * is an object null or blank
	 * @param object
	 * @return
	 */
	public static boolean nullOrBlank(Object object) {
		//first handle blanks and nulls
		if (object == null) {
			return true;
		}
		if (object instanceof String && StringUtils.isBlank(((String)object))) {
			return true;
		}
		return false;
		
	}
	
	/**
	 * get the boolean value for an object
	 * @param object
	 * @param defaultBoolean if object is null or empty
	 * @return the boolean
	 */
	public static boolean booleanValue(Object object, boolean defaultBoolean) {
		if (nullOrBlank(object)) {
			return defaultBoolean;
		}
		return booleanValue(object);
	}
	
	/**
	 * make sure it is non null, if null, then give new set
	 * @param <T>
	 * @param set
	 * @return set non-null
	 */
	public static <T> Set<T> nonNull(Set<T> set) {
		return set == null ? new HashSet<T>() : set;
	}
	
	/**
	 * web service format string
	 */
	private static final String WS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	/**
	 * convert a date to a string using the standard web service pattern
	 * yyyy/MM/dd HH:mm:ss.SSS
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
	
	/**
	 * validate and organize params
	 * @param paramName0
	 * @param paramValue0
	 * @param paramName1
	 * @param paramValue1
	 * @return the array of two arrays (one of names, second of values)
	 */
	public static String[][] params(String paramName0, String paramValue0,
			String paramName1, String paramValue1) {
		String[] paramNames = null;
		String[] paramValues = null;
		if (!StringUtils.isBlank(paramName0)) {
			if (!StringUtils.isBlank(paramName1)) {
				paramNames = new String[]{ paramName0, paramName1};
				paramValues = new String[]{paramValue0,paramValue1};
			} else {
				paramNames = new String[]{paramName0};
				paramValues = new String[]{paramValue0};
			}
		}
		return new String[][]{paramNames, paramValues};
	}
	
}
