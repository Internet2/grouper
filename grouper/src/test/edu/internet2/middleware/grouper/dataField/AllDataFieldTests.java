package edu.internet2.middleware.grouper.dataField;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllDataFieldTests {
  
  /**
   * suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.dataField");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperDataProviderHistoryTest.class);
    suite.addTestSuite(GrouperDataAliasTest.class);
    suite.addTestSuite(GrouperDataEngineTest.class);
    suite.addTestSuite(GrouperDataFieldAssignTest.class);
    suite.addTestSuite(GrouperDataFieldTest.class);
    suite.addTestSuite(GrouperDataProviderTest.class);
    suite.addTestSuite(GrouperDataRowAssignTest.class);
    suite.addTestSuite(GrouperDataRowFieldAssignTest.class);
    suite.addTestSuite(GrouperDataRowTest.class);
    //$JUnit-END$
    return suite;
  }

}
