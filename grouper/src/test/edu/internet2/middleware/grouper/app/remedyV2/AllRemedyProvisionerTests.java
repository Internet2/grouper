package edu.internet2.middleware.grouper.app.remedyV2;

import edu.internet2.middleware.grouper.app.scim.GrouperGithubProvisionerTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllRemedyProvisionerTests extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllRemedyProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(RemedyProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
