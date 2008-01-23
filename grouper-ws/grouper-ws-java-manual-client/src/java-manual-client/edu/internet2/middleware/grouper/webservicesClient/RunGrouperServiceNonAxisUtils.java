package edu.internet2.middleware.grouper.webservicesClient;

public class RunGrouperServiceNonAxisUtils {

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

}
