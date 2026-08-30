package edu.internet2.middleware.grouper.app.github;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit suite for the GitHub provisioner tests.
 */
public class AllGithubProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllGithubProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GithubProvisionerTest.class);
    suite.addTestSuite(GithubProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
