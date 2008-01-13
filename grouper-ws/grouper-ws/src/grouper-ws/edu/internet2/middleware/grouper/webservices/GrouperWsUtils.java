/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import org.apache.commons.lang.StringUtils;

/**
 * utility methods for grouper web services
 * @author mchyzer
 */
public class GrouperWsUtils {
	
	public static boolean booleanValue(String input, boolean defaultValue) {
		if (StringUtils.isBlank(input)) {
			return defaultValue;
		}
		if (StringUtils.equalsIgnoreCase("T", input) ||
				(StringUtils.equals("true", input))) {
			return true;
		}
		if (StringUtils.equalsIgnoreCase("F", input) ||
				(StringUtils.equals("false", input))) {
			return false;
		}
		throw new RuntimeException("Invalid boolean: '" + input + "', expecting (case insensitive): T or F or true or false");
	}
	
}
