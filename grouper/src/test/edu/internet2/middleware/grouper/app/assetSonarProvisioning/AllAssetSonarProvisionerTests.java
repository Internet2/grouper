package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit suite aggregator for the AssetSonar provisioner tests.
 */
public class AllAssetSonarProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllAssetSonarProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(AssetSonarProvisionerTest.class);
    suite.addTestSuite(AssetSonarProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
