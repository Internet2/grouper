package edu.internet2.middleware.grouper.webservicesClient;

import java.net.URL;

public class QuickText {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    URL url = QuickText.class.getResource("/grouperasdsad.ehcache.xml");
	    System.out.println(url);
	}

}
