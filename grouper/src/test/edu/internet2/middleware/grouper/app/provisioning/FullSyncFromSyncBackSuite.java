package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.okta.GrouperOktaProvisionerTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * GRP-7048: standalone suite that runs only the "full sync users from the sync-back cache" tests.
 *
 * <p>The tests themselves live in their provisioner test classes (e.g. {@link GrouperOktaProvisionerTest});
 * this suite just collects the relevant ones so they can be run together. It is INTENTIONALLY NOT
 * referenced from {@code AllTests} -- run it directly ("Run As &gt; JUnit Test" on this class).
 *
 * <p>Like the other Okta provisioner tests, these need the mock-services Tomcat running at
 * {@code localhost:8080} (with the provisioning daemon off); otherwise they no-op via
 * {@code tomcatRunTests()} or fail with a connection error.
 *
 * <p>As the feature is wired into more provisioners (and eventually extended to groups and
 * memberships), add their corresponding test methods here.
 */
public class FullSyncFromSyncBackSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite("GRP-7048 full sync from sync-back cache");

    // Okta (users axis)
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncUsersFromSyncBackWarmCache"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncUsersFromSyncBackMissingFromCacheReRead"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncUsersFromSyncBackIncrementalKeepsCacheCurrent"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncUsersFromSyncBackIncrementalRemovalCacheCurrent"));

    // Okta (memberships axis)
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncMembershipsFromSyncBackWarmCache"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncMembershipsFromSyncBackAddAndRemove"));

    // Okta (groups axis)
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncGroupsFromSyncBackWarmCache"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncGroupsFromSyncBackAddAndRemove"));

    return suite;
  }

}
