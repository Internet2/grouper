/**
 * 
 */
package edu.internet2.middleware.grouper.webservices;

import java.lang.reflect.Array;

/**
 * @author mchyzer
 *
 */
public class GrouperServiceUtils {
	
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
}
