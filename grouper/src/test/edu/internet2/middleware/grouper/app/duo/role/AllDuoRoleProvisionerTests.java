package edu.internet2.middleware.grouper.app.duo.role;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllDuoRoleProvisionerTests extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllDuoRoleProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperDuoRoleProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
