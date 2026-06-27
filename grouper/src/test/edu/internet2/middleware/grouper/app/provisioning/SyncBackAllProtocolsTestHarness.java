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
 * Cross-protocol harness for the generic provisioner sync-back smoke tests plus
 * the per-protocol full-sync and incremental-sync coverage. Each supported
 * protocol's test class exposes a pair of sync-back methods named
 * {@code test<Protocol>FullSyncPopulatesGenericTables} (selectAll on — exercises
 * the {@code retrieveAll*} capture hooks) and
 * {@code test<Protocol>FullSyncSelectByIdsPopulatesGenericTables} (selectAll off
 * — exercises the scoped {@code retrieve*} capture hooks), as well as the
 * protocol's own broader {@code testFullSync*} / {@code testIncremental*} tests.
 * This class aggregates all of them into a single {@link TestSuite} so the
 * whole provisioning surface can be exercised in one run — useful when
 * iterating on the framework (write-shadow precision pass, flush reorder, etc.).
 *
 * <p>The suite leads with the fast raw-JSON capture unit pass -- every protocol's
 * {@code *ProvisioningTargetNativeSyncTest} -- by delegating to
 * {@link AllProvisioningTargetNativeSyncTests#suite()} (that harness can also be run on its
 * own, with no Tomcat). These have no {@code tomcatRunTests()} gate, so they run even with
 * Tomcat off; the heavier full-sync / incremental tests below do.
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

    // ---- fast no-Tomcat unit pass: every protocol's raw-JSON capture test, delegated to the
    // dedicated AllProvisioningTargetNativeSyncTests harness (same package) so the 12-class list
    // lives in one place. No tomcatRunTests() gate, so these run even with Tomcat off. (LDAP is not
    // JSON-based, so it has no such unit test.) ----
    suite.addTest(AllProvisioningTargetNativeSyncTests.suite());

    // ---- per-protocol sync-back INTEGRATION tests (Tomcat): full populate (selectAll on) +
    // selectByIds populate (selectAll off) + the full SCIM-parity CRUD converge matrix
    // (insert/update/delete + membership add/remove, data-change reflection, orphan capture,
    // broken-target-stays, load-flag isolation, incremental no-spurious-deletes) + each protocol's
    // own broader full/incremental tests. Capability-gated per protocol, so the set differs by
    // target (e.g. no membership-replace where unsupported; teams+roles split for datadog/truefoundry).

    // Adobe (no incremental-populate; membership write-track has full + incremental)
    suite.addTest(new GrouperAdobeProvisionerTest("testAdobeFullSyncPopulatesGenericTables"));
    suite.addTest(new GrouperAdobeProvisionerTest("testAdobeFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new GrouperAdobeProvisionerTest("testAdobeFullSyncObjectDeleteConverges"));
    suite.addTest(new GrouperAdobeProvisionerTest("testAdobeFullSyncGroupRenameConverges"));
    suite.addTest(new GrouperAdobeProvisionerTest("testAdobeFullSyncMembershipWriteTrackConverges"));
    suite.addTest(new GrouperAdobeProvisionerTest("testAdobeIncrementalMembershipWriteTrackConverges"));

    // Azure
    suite.addTest(new GrouperAzureProvisionerTest("testAzureFullSyncPopulatesGenericTables"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureGroupInsertConvergesNextRead"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureGroupDeleteConvergesNextRead"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureGroupUpdateConvergesNextRead"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureMembershipAddConvergesNextRead"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureMembershipRemoveConvergesNextRead"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureSelectAllFalseExcludesOrphans"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureUserDeleteBrokenTargetStaysInMirror"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureLoadGroupsFlagInIsolation"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureLoadEntitiesFlagInIsolation"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureLoadMembershipsFlagOff"));
    suite.addTest(new GrouperAzureProvisionerTest("testAzureIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new GrouperAzureProvisionerTest("testIncrementalSyncAzure"));

    // Box (the pilot)
    suite.addTest(new GrouperBoxProvisionerTest("testBoxFullSyncPopulatesGenericTables"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxGroupInsertConvergesNextRead"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxGroupDeleteConvergesNextRead"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxGroupUpdateConvergesNextRead"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxMembershipAddConvergesNextRead"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxMembershipRemoveConvergesNextRead"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxSelectAllFalseExcludesOrphans"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxUserDeleteBrokenTargetStaysInMirror"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxLoadGroupsFlagInIsolation"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxLoadEntitiesFlagInIsolation"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxLoadMembershipsFlagOff"));
    suite.addTest(new GrouperBoxProvisionerTest("testBoxIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new GrouperBoxProvisionerTest("testIncrementalSyncBox"));

    // Datadog (teams + roles)
    suite.addTest(new DatadogProvisionerTest("testDatadogFullSyncPopulatesGenericTables"));
    suite.addTest(new DatadogProvisionerTest("testDatadogFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new DatadogProvisionerTest("testDatadogTeamGroupInsertConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogRoleGroupInsertConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogTeamGroupDeleteConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogRoleGroupDeleteConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogTeamGroupUpdateConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogTeamMembershipAddConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogRoleMembershipAddConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogTeamMembershipRemoveConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogRoleMembershipRemoveConvergesNextRead"));
    suite.addTest(new DatadogProvisionerTest("testDatadogFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new DatadogProvisionerTest("testDatadogFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new DatadogProvisionerTest("testDatadogFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new DatadogProvisionerTest("testDatadogSelectAllFalseExcludesOrphans"));
    suite.addTest(new DatadogProvisionerTest("testDatadogUserDeleteBrokenTargetStaysInMirror"));
    suite.addTest(new DatadogProvisionerTest("testDatadogLoadGroupsFlagInIsolation"));
    suite.addTest(new DatadogProvisionerTest("testDatadogLoadEntitiesFlagInIsolation"));
    suite.addTest(new DatadogProvisionerTest("testDatadogLoadMembershipsFlagOff"));
    suite.addTest(new DatadogProvisionerTest("testDatadogIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new DatadogProvisionerTest("testIncrementalTeamCrudAndMemberships"));

    // Duo (user-centric membership; insert converges next-read, not same-run; no incremental sync-back)
    suite.addTest(new GrouperDuoProvisionerTest("testDuoFullSyncPopulatesGenericTables"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoGroupInsertConvergesNextRead"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoGroupDeleteConvergesNextRead"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoGroupUpdateConvergesNextRead"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoMembershipAddConvergesNextRead"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoMembershipRemoveConvergesNextRead"));
    suite.addTest(new GrouperDuoProvisionerTest("testDuoFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new GrouperDuoProvisionerTest("testIncrementalProvisionDuo"));

    // FreshService Requester
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncPopulatesGenericTables"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterGroupInsertConvergesNextRead"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterGroupDeleteConvergesNextRead"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterGroupUpdateConvergesNextRead"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterMembershipAddConvergesNextRead"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterMembershipRemoveConvergesNextRead"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterSelectAllFalseExcludesOrphans"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterUserDeleteBrokenTargetStaysInMirror"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterLoadGroupsFlagInIsolation"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterLoadEntitiesFlagInIsolation"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterLoadMembershipsFlagOff"));
    suite.addTest(new FreshRequesterProvisionerTest("testFreshRequesterIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new FreshRequesterProvisionerTest("testIncrementalProvisionGroupAndThenDeleteGroup"));

    // Google (multi-call group assembly; group-centric membership with roles)
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleFullSyncPopulatesGenericTables"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleGroupInsertConvergesNextRead"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleGroupDeleteConvergesNextRead"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleGroupUpdateConvergesNextRead"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleMembershipAddConvergesNextRead"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleMembershipRemoveConvergesNextRead"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleSelectAllFalseExcludesOrphans"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleLoadGroupsFlagInIsolation"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleLoadEntitiesFlagInIsolation"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleLoadMembershipsFlagOff"));
    suite.addTest(new GrouperGoogleProvisionerTest("testGoogleIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new GrouperGoogleProvisionerTest("testIncrementalSyncGoogle"));

    // LDAP (dedicated generic-table test class)
    suite.addTest(new LdapProvisionerGenericTableTest("testLdapFullSyncPopulatesGenericTables"));
    suite.addTest(new LdapProvisionerGenericTableTest("testLdapFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new LdapProvisionerGenericTableTest("testNativeAttributesGroupsJsonFormIncremental"));

    // Okta (nested /profile attributes)
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncPopulatesGenericTables"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaGroupInsertConvergesNextRead"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaGroupDeleteConvergesNextRead"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaGroupUpdateConvergesNextRead"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaMembershipAddConvergesNextRead"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaMembershipRemoveConvergesNextRead"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaSelectAllFalseExcludesOrphans"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaUserDeleteBrokenTargetStaysInMirror"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaLoadGroupsFlagInIsolation"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaLoadEntitiesFlagInIsolation"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaLoadMembershipsFlagOff"));
    suite.addTest(new GrouperOktaProvisionerTest("testOktaIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new GrouperOktaProvisionerTest("testIncrementalSyncOkta"));

    // Remedy (separate-object memberships; no incremental-populate in existing coverage)
    suite.addTest(new RemedyProvisionerTest("testRemedyFullSyncPopulatesGenericTables"));
    suite.addTest(new RemedyProvisionerTest("testRemedyFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new RemedyProvisionerTest("testRemedyMembershipAddConvergesNextRead"));
    suite.addTest(new RemedyProvisionerTest("testRemedyMembershipRemoveConvergesNextRead"));
    suite.addTest(new RemedyProvisionerTest("testRemedyBrokenTargetMembershipStaysInMirror"));
    suite.addTest(new RemedyProvisionerTest("testRemedyFullSyncReflectsDataChangesAcrossSyncs"));
    suite.addTest(new RemedyProvisionerTest("testRemedyFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new RemedyProvisionerTest("testRemedyFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new RemedyProvisionerTest("testRemedySelectAllFalseExcludesOrphans"));
    suite.addTest(new RemedyProvisionerTest("testRemedyLoadGroupsFlagInIsolation"));
    suite.addTest(new RemedyProvisionerTest("testRemedyLoadEntitiesFlagInIsolation"));
    suite.addTest(new RemedyProvisionerTest("testRemedyLoadMembershipsFlagOff"));
    suite.addTest(new RemedyProvisionerTest("testRemedyIncrementalSyncBackNoSpuriousDeletes"));

    // SCIM (dedicated generic-table test class)
    suite.addTest(new ScimProvisionerGenericTableTest("testFullProvisionPopulatesGenericTables"));
    suite.addTest(new ScimProvisionerGenericTableTest("testScimFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new ScimProvisionerGenericTableTest("testIncrementalMembershipAddConvergesSameCycle"));

    // TeamDynamix (PascalCase attrs)
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixFullSyncPopulatesGenericTables"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixGroupInsertConvergesNextRead"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixGroupUpdateConvergesNextRead"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixMembershipAddConvergesNextRead"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixMembershipRemoveConvergesNextRead"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixObjectDeleteStaysInMirror"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixFullSyncCapturesOrphanTargetEntities"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixFullSyncCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixSelectAllFalseExcludesOrphans"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixLoadGroupsFlagInIsolation"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixLoadEntitiesFlagInIsolation"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixLoadMembershipsFlagOff"));
    suite.addTest(new TeamDynamixProvisionerTest("testTeamDynamixIncrementalSyncBackNoSpuriousDeletes"));
    suite.addTest(new TeamDynamixProvisionerTest("testIncrementalProvisionTeamDynamix"));

    // TrueFoundry (teams + roles)
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundryFullSyncPopulatesGenericTables"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundryFullSyncSelectByIdsPopulatesGenericTables"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackTeamInsertConvergesNextRead"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackRoleInsertConvergesNextRead"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackTeamDeleteConvergesNextRead"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackTeamMembershipAddConvergesNextRead"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackTeamMembershipRemoveConvergesNextRead"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackRoleMembershipMoveConvergesNextRead"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackReflectsDataChangesAcrossSyncs"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackCapturesOrphanTargetEntities"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackCapturesMembershipsFromOrphanGroup"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackSelectAllFalseExcludesOrphans"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackUserStaysInMirrorWhenNotDeleted"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackLoadGroupsFlagInIsolation"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackLoadEntitiesFlagInIsolation"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackLoadMembershipsFlagOff"));
    suite.addTest(new TrueFoundryProvisionerTest("testTrueFoundrySyncBackIncrementalNoSpuriousDeletes"));
    suite.addTest(new TrueFoundryProvisionerTest("testIncrementalTeamCrudAndMemberships"));

    return suite;
  }

}
