package edu.internet2.middleware.grouper.ui.tags;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllUiTagsTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for edu.internet2.middleware.grouper.ui.tags");
		//$JUnit-BEGIN$
		suite.addTest(AllUiTagsTests.suite());
		//$JUnit-END$
		return suite;
	}

}
