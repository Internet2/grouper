/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

/**
 * utility methods for grouper web services
 * 
 * @author mchyzer
 */
public final class GrouperWsUtils {

	/**
	 * no need to construct
	 */
	private GrouperWsUtils() {
		// no need to construct
	}

	/**
	 * parse a boolean as "T" or "F" or "TRUE" or "FALSE" case insensitive. If
	 * not specified, then use default. If malformed, then exception
	 * 
	 * @param input
	 * @param defaultValue
	 * @return the boolean
	 */
	public static boolean booleanValue(String input, boolean defaultValue) {
		if (StringUtils.isBlank(input)) {
			return defaultValue;
		}
		if (StringUtils.equalsIgnoreCase("T", input)
				|| (StringUtils.equals("true", input))) {
			return true;
		}
		if (StringUtils.equalsIgnoreCase("F", input)
				|| (StringUtils.equals("false", input))) {
			return false;
		}
		throw new RuntimeException("Invalid boolean: '" + input
				+ "', expecting (case insensitive): T or F or true or false");
	}

}
