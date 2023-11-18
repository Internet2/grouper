package edu.internet2.middleware.grouper.app.grouperTypes;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllGrouperTypesTests {

  
  /**
   * suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.grouperTypes");
    //$JUnit-BEGIN$
    suite.addTestSuite(GdgTypeGroupSaveTest.class);
    suite.addTestSuite(GdgTypeStemSaveTest.class);
    suite.addTestSuite(GdgTypeGroupFinderTest.class);
    suite.addTestSuite(GdgTypeStemFinderTest.class);
    suite.addTestSuite(GrouperObjectTypeConfigurationTest.class);
    suite.addTestSuite(GrouperObjectTypesDaemonLogicTest.class);
    //$JUnit-END$
    return suite;
  }
  
}
