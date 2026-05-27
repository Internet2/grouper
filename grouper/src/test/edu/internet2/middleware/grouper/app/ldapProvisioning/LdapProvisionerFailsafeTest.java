package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * GRP-7013: regression coverage for provisioner failsafe on the LDAP provisioner.
 * Mirrors the structure of {@link LdapProvisionerIncrementalTest}: stand the LDAP
 * container up, provision a baseline, then exercise the failsafe through the full
 * sync and the incremental (change-log) sync.  Verifies that an aggressive
 * removal trips ERROR_FAILSAFE and leaves the target untouched.
 *
 * The incremental case is the one that would catch a Griffin-style bug
 * (provisioner ignoring failsafe and pushing large removals via change-log).
 */
public class LdapProvisionerFailsafeTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new LdapProvisionerFailsafeTest("testFullSyncFailsafeMaxPercentRemove"));
  }

  @Override
  public String defaultConfigId() {
    return "ldapProvTest";
  }

  public LdapProvisionerFailsafeTest() {
    super();
  }

  public LdapProvisionerFailsafeTest(String name) {
    super(name);
  }

  private GrouperSession grouperSession = null;

  @Override
  protected void setUp() {
    super.setUp();
    try {
      this.grouperSession = GrouperSession.startRootSession();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    LdapProvisionerIncrementalTest.setupLdapAndSubjectSource();
  }

  @Override
  protected void tearDown() {
    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();
    SourceManager.getInstance().internal_removeSource("personLdapSource");
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }

  /**
   * Configure the LDAP provisioner with failsafe enabled and aggressive
   * per-group thresholds (1% of a group >= 5 members must remain).
   */
  private void configureProvisionerWithFailsafe() {
    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignExplicitFilters(true)
        // GRP-7012/7013: per-provisioner failsafe.  These four are the minimum
        // to turn it on (showFailsafe + failsafeUse) and to make it actually
        // fire on small test groups (minGroupSize) with a tight percentage.
        .addExtraConfig("showFailsafe", "true")
        .addExtraConfig("failsafeUse", "true")
        .addExtraConfig("failsafeMinGroupSize", "5")
        .addExtraConfig("failsafeMaxPercentRemove", "20")
        .addExtraConfig("failsafeSendEmail", "false"));
    // pick up the new config
    ConfigPropertiesCascadeBase.clearCache();
  }

  /**
   * Mark a folder provisionable and seed a group with the given subjects.
   */
  private Group seedGroup(String groupName, Subject... subjects) {
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("ldapProvTest");
    attributeValue.setTargetName("ldapProvTest");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    Group group = new GroupSave(this.grouperSession).assignName(groupName).save();
    for (Subject subject : subjects) {
      group.addMember(subject, false);
    }
    return group;
  }

  /**
   * Look up the single test group in LDAP and return the size of its member
   * attribute, or -1 if the group doesn't exist yet.
   */
  private int countLdapMembers(String groupCn) {
    List<LdapEntry> entries = LdapSessionUtils.ldapSession().list("personLdap",
        "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE,
        "(&(objectClass=groupOfNames)(cn=" + groupCn + "))",
        new String[] {"member"}, null);
    if (entries.isEmpty()) {
      return -1;
    }
    return entries.get(0).getAttribute("member").getStringValues().size();
  }

  /**
   * Full sync: provision a 10-member group, delete 5 from Grouper (50% of the
   * group, well above the 20% threshold), run full sync, expect ERROR_FAILSAFE
   * and the LDAP target unchanged.
   */
  public void testFullSyncFailsafeMaxPercentRemove() {

    configureProvisionerWithFailsafe();

    Subject[] subjects = new Subject[] {
        SubjectFinder.findById("jsmith", true),
        SubjectFinder.findById("banderson", true),
        SubjectFinder.findById("kwhite", true),
        SubjectFinder.findById("whenderson", true),
        SubjectFinder.findById("blopez", true),
        SubjectFinder.findById("hdavis", true),
        SubjectFinder.findById("bwilliams466", true),
        SubjectFinder.findById("ggrady", true),
        SubjectFinder.findById("mmorrison", true),
        SubjectFinder.findById("rroberts", true) };

    Group testGroup = seedGroup("test:testGroup", subjects);

    // baseline: full sync seeds 10 members in LDAP
    fullProvision();
    assertEquals(10, countLdapMembers("test:testGroup"));

    // remove 5 of 10 (50% > 20% threshold) - failsafe should trip
    for (int i = 0; i < 5; i++) {
      testGroup.deleteMember(subjects[i]);
    }

    String jobName = "OTHER_JOB_provisioner_full_ldapProvTest";
    try {
      fullProvision(defaultConfigId(), true);
    } catch (RuntimeException expected) {
      // failsafe throws OtherJobException -> wrapped by loader
    }

    Hib3GrouperLoaderLog log = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertNotNull("expected a loader log entry for " + jobName, log);
    // GRP-7013/7015 diagnostic: report the provisioner's failsafe summary AND
    // the current LDAP member count so we can tell whether the deletes were
    // blocked (count=10) or pushed (count=5) regardless of which provisioner
    // instance retrieveInternalLastProvisioner returns.
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner prov =
        edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.retrieveInternalLastProvisioner();
    String failsafeSummary = prov == null ? "<no provisioner>" : "" + prov.getDebugMap().get("failsafe");
    int ldapCount = countLdapMembers("test:testGroup");
    String diag = "failsafe summary: " + failsafeSummary + "; ldap member count: " + ldapCount;
    assertEquals(diag, "ERROR_FAILSAFE", log.getStatus());

    // LDAP target should be unchanged - all 10 still present
    assertEquals(diag, 10, ldapCount);
  }

  /**
   * Incremental sync: provision a 10-member group via full sync, then delete 5
   * members in Grouper and drive them through the change-log -> incremental
   * provisioner.  Expect ERROR_FAILSAFE and the LDAP target unchanged.  This is
   * the path most likely to regress (Griffin's report against 7.0.3).
   */
  public void testIncrementalFailsafeMaxPercentRemove() {

    configureProvisionerWithFailsafe();

    // Mirror LdapProvisionerIncrementalTest's pattern: run an initial
    // incrementalProvision to drain / register the consumer before any data
    // changes, then provision the baseline via incremental too so the
    // change-log -> consumer pipeline is the one exercised throughout.
    incrementalProvision();

    Subject[] subjects = new Subject[] {
        SubjectFinder.findById("jsmith", true),
        SubjectFinder.findById("banderson", true),
        SubjectFinder.findById("kwhite", true),
        SubjectFinder.findById("whenderson", true),
        SubjectFinder.findById("blopez", true),
        SubjectFinder.findById("hdavis", true),
        SubjectFinder.findById("bwilliams466", true),
        SubjectFinder.findById("ggrady", true),
        SubjectFinder.findById("mmorrison", true),
        SubjectFinder.findById("rroberts", true) };

    Group testGroup = seedGroup("test:testGroup", subjects);

    // baseline via incremental (so all events flow through the same path the
    // failsafe-tripping run will use)
    incrementalProvision();
    assertEquals(10, countLdapMembers("test:testGroup"));

    // now remove 5 of 10 and let the incremental path pick them up
    for (int i = 0; i < 5; i++) {
      testGroup.deleteMember(subjects[i]);
    }

    String jobName = "CHANGE_LOG_consumer_provisioner_incremental_ldapProvTest";
    try {
      incrementalProvision(defaultConfigId(), true, true, true);
    } catch (RuntimeException expected) {
      // failsafe throws OtherJobException
    }

    Hib3GrouperLoaderLog log = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertNotNull("expected a loader log entry for " + jobName, log);
    // GRP-7013/7015 diagnostic: report the provisioner's failsafe summary AND
    // the current LDAP member count so we can tell whether the deletes were
    // blocked (count=10) or pushed (count=5) regardless of which provisioner
    // instance retrieveInternalLastProvisioner returns.
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner prov =
        edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.retrieveInternalLastProvisioner();
    String failsafeSummary = prov == null ? "<no provisioner>" : "" + prov.getDebugMap().get("failsafe");
    int ldapCount = countLdapMembers("test:testGroup");
    String diag = "failsafe summary: " + failsafeSummary + "; ldap member count: " + ldapCount;
    assertEquals(diag, "ERROR_FAILSAFE", log.getStatus());

    // LDAP target should be unchanged - all 10 still present
    assertEquals(diag, 10, ldapCount);
  }

  /**
   * EntityAttributes variant of the full-sync failsafe test.  Memberships are
   * stored as multi-valued eduPersonEntitlement values on each user, not on a
   * group object.  GRP-7014's compare-counter fix lives in code shared between
   * groupAttributes and entityAttributes modes; this test catches a future
   * change that breaks one mode without the other.
   */
  public void testFullSyncFailsafeMaxPercentRemoveEntityAttributes() {

    LdapProvisionerTestUtils.configureLdapProvisioner(
        new LdapProvisionerTestConfigInput()
        .assignConfigId("eduPersonEntitlement")
        .assignTranslateFromGrouperProvisioningGroupField("extension")
        .assignMembershipStructureEntityAttributes(true)
        .assignGroupAttributeCount(1)
        .assignEntityAttributeCount(3)
        .assignExplicitFilters(true)
        .assignEntitlementMetadata(true)
        .addExtraConfig("showFailsafe", "true")
        .addExtraConfig("failsafeUse", "true")
        .addExtraConfig("failsafeMinGroupSize", "5")
        .addExtraConfig("failsafeMaxPercentRemove", "20")
        .addExtraConfig("failsafeSendEmail", "false"));
    ConfigPropertiesCascadeBase.clearCache();

    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("eduPersonEntitlement");
    attributeValue.setTargetName("eduPersonEntitlement");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    Subject[] subjects = new Subject[] {
        SubjectFinder.findById("jsmith", true),
        SubjectFinder.findById("banderson", true),
        SubjectFinder.findById("kwhite", true),
        SubjectFinder.findById("whenderson", true),
        SubjectFinder.findById("blopez", true),
        SubjectFinder.findById("hdavis", true),
        SubjectFinder.findById("bwilliams466", true),
        SubjectFinder.findById("ggrady", true),
        SubjectFinder.findById("mmorrison", true),
        SubjectFinder.findById("rroberts", true) };

    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    for (Subject subject : subjects) {
      testGroup.addMember(subject, false);
    }

    // baseline: each of the 10 users gets 'testGroup' as an eduPersonEntitlement value
    fullProvision("eduPersonEntitlement");
    for (Subject subject : subjects) {
      Set<String> values = LdapProvisioningEntityAttributesTest.retrieveLdapAttributes(subject.getId());
      assertTrue("expected testGroup entitlement for " + subject.getId() + ", got " + values,
          values.contains("testGroup"));
    }

    // remove 5 of 10 (50% > 20% threshold) - failsafe should trip
    for (int i = 0; i < 5; i++) {
      testGroup.deleteMember(subjects[i]);
    }

    String jobName = "OTHER_JOB_provisioner_full_eduPersonEntitlement";
    try {
      fullProvision("eduPersonEntitlement", true);
    } catch (RuntimeException expected) {
      // failsafe throws OtherJobException -> wrapped by loader
    }

    Hib3GrouperLoaderLog log = Hib3GrouperLoaderLog.retrieveMostRecentLog(jobName);
    assertNotNull("expected a loader log entry for " + jobName, log);
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner prov =
        edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner.retrieveInternalLastProvisioner();
    String failsafeSummary = prov == null ? "<no provisioner>" : "" + prov.getDebugMap().get("failsafe");
    assertEquals("failsafe summary: " + failsafeSummary, "ERROR_FAILSAFE", log.getStatus());

    // LDAP entity attributes should be unchanged - all 10 users still carry 'testGroup'
    for (Subject subject : subjects) {
      Set<String> values = LdapProvisioningEntityAttributesTest.retrieveLdapAttributes(subject.getId());
      assertTrue("expected testGroup entitlement still present for " + subject.getId()
          + " (failsafe should have blocked the deletes), got " + values,
          values.contains("testGroup"));
    }
  }

}
