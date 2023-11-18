package edu.internet2.middleware.grouper.authentication;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllGrouperAuthenticationTests {
  
  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.authentication");
    //$JUnit-BEGIN$
    suite.addTestSuite(AuthenticationTest.class);
    suite.addTestSuite(GrouperPublicPrivateKeyJwtTest.class);
    suite.addTestSuite(GrouperTrustedJwtTest.class);
    //$JUnit-END$
    return suite;
  }

}
