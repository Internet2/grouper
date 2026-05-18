package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.adobe.GrouperAdobeProvisionerTest;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisionerTest;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxProvisionerTest;
import edu.internet2.middleware.grouper.app.datadog.DatadogProvisionerTest;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisionerTest;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterProvisionerTest;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisionerTest;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerGenericTableTest;
import edu.internet2.middleware.grouper.app.okta.GrouperOktaProvisionerTest;
import edu.internet2.middleware.grouper.app.remedyV2.RemedyProvisionerTest;
import edu.internet2.middleware.grouper.app.scim.ScimProvisionerGenericTableTest;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisionerTest;
import edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryProvisionerTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * DO NOT ADD THIS TO THE HARNESS, IT IS A DUPLICATE
 * </p>
 * Cross-protocol harness for the generic provisioner sync-back smoke tests. Each
 * supported protocol's test class exposes a pair of methods named
 * {@code test<Protocol>FullSyncPopulatesGenericTables} (selectAll on — exercises
 * the {@code retrieveAll*} capture hooks) and
 * {@code test<Protocol>FullSyncSelectByIdsPopulatesGenericTables} (selectAll off
 * — exercises the scoped {@code retrieve*} capture hooks). This class aggregates them
 * into a single {@link TestSuite} so the whole sync-back surface can be exercised in one
 * run — useful when iterating on the framework (write-shadow precision pass, flush
 * reorder, etc.).
 *
 * <p><strong>Not added to {@code AllAppTests} / {@code All<Protocol>ProvisionerTests}</strong>
 * by design — each protocol's per-suite aggregator already includes its own tests, so
 * adding this harness to the central aggregator would run them twice. Invoke this class
 * directly when you want the sweep.
 *
 * <p>Two ways to run:
 * <ul>
 *   <li>JUnit3-aware test runner (Eclipse "Run As → JUnit Test", or
 *       {@code junit.textui.TestRunner.run(SyncBackAllProtocolsTestHarness.suite())})</li>
 *   <li>Command line: {@code edu.internet2.middleware.grouper.AllTests
 *       provisioning.SyncBackAllProtocolsTestHarness} (matches the suffix-routing the
 *       existing AllTests harness uses).</li>
 * </ul>
 *
 * <p>The tests run end-to-end against the same mock targets each protocol's own test
 * class uses. They are gated by {@code tomcatRunTests()} where the per-protocol class
 * gates on it, so the harness inherits those gates.
 *
 * <p>This class is temporary — once write-shadow precision lands and the per-protocol
 * tests are sufficient on their own, this harness can be deleted.
 */
public class SyncBackAllProtocolsTestHarness extends TestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite(SyncBackAllProtocolsTestHarness.class.getName());

    // FreshService Requester
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncPopulatesGenericTables"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncSelectByIdsPopulatesGenericTables"));


    return suite;
  }

}
