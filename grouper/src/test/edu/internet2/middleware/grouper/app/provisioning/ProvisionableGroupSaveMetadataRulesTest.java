package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1 (and the ProvisionableGroupSave part of advisory M1).</p>
 *
 * <p>Part 1: the Datadog md_adminGroupName and TrueFoundry md_trueFoundryManagerGroupName group
 * metadata items name another Grouper group whose members the provisioner copies to the target
 * as root.  A delegated user (groupAllowedToAssign and ADMIN of the provisioned group) could name
 * any private group.  Now a non-wheel user needs READ on the named group, in the UI save (which
 * calls validateMetadataInputForFolder in the user's session) and in ProvisionableGroupSave.</p>
 *
 * <p>Part 2: ProvisionableGroupSave, which a non-wheel caller may use (GSH, and MCP group_save in
 * 7.x), ignored metadata.&lt;n&gt;.groupIdThatCanUpdate, canUpdate=false, canChange=false and the
 * option list of dropdown items.  Now the same rules as the UI apply to non-wheel callers.</p>
 *
 * <p>The test only uses public API that exists in every patched release, and refers to the
 * Datadog / TrueFoundry provisioners by class name (the Datadog / TrueFoundry cases are skipped
 * where a release does not have them).  Before the fix the "expect an error" saves succeed and
 * the test fails.</p>
 */
public class ProvisionableGroupSaveMetadataRulesTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ProvisionableGroupSaveMetadataRulesTest("testLockedItemsAndOptions"));
  }

  /**
   *
   */
  public ProvisionableGroupSaveMetadataRulesTest() {
    super();
  }

  /**
   * @param name
   */
  public ProvisionableGroupSaveMetadataRulesTest(String name) {
    super(name);
  }

  /** Datadog provisioner class */
  private static final String DATADOG_CLASS = "edu.internet2.middleware.grouper.app.datadog.DatadogProvisioner";

  /** TrueFoundry provisioner class */
  private static final String TRUEFOUNDRY_CLASS = "edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryProvisioner";

  /** LDAP provisioner class */
  private static final String LDAP_CLASS = "edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync";

  /** suffix that makes the config ids unique per run */
  private String suffix;

  /** config ids made by this test, removed in tearDown */
  private java.util.List<String> configIds = new java.util.ArrayList<String>();

  /** group SUBJ0 administers and provisions */
  private Group teamGroup;

  /** private group SUBJ0 cannot read, SUBJ1 is a member */
  private Group secretGroup;

  /** group allowed to update the locked item, SUBJ0 is not in it */
  private Group updatersGroup;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * groups, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.suffix = "" + (System.currentTimeMillis() % 1000000);

    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group assigners = new GroupSave().assignName("test:provisioning199:assigners").assignCreateParentStemsIfNotExist(true).save();
      assigners.addMember(SubjectTestHelper.SUBJ0, false);

      this.teamGroup = new GroupSave().assignName("test:provisioning199:teamGroup").assignCreateParentStemsIfNotExist(true).save();
      this.teamGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      this.teamGroup.addMember(SubjectTestHelper.SUBJ1, false);
      this.teamGroup.addMember(SubjectTestHelper.SUBJ2, false);

      this.secretGroup = new GroupSave().assignName("test:provisioning199:secret").assignCreateParentStemsIfNotExist(true).save();
      this.secretGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      this.secretGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      this.secretGroup.addMember(SubjectTestHelper.SUBJ1, false);

      this.updatersGroup = new GroupSave().assignName("test:provisioning199:updaters").assignCreateParentStemsIfNotExist(true).save();
      this.updatersGroup.addMember(SubjectTestHelper.SUBJ3, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove config overrides
   */
  @Override
  protected void tearDown() {
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    for (String configId : this.configIds) {
      loader.keySet().removeIf(key -> key.startsWith("provisioner." + configId + "."));
    }
    loader.keySet().removeIf(key -> key.startsWith("ldap.personLdap."));
    GrouperProvisioningSettings.getTargets(false);
    super.tearDown();
  }

  /**
   * common provisioner config, assigners may assign
   * @param configId
   * @param className
   * @return the config override map prefix
   */
  private String configureProvisioner(String configId, String className) {
    this.configIds.add(configId);
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    String p = "provisioner." + configId + ".";
    loader.put(p + "class", className);
    loader.put(p + "groupAllowedToAssign", "test:provisioning199:assigners");
    loader.put(p + "groupAllowedToView", "test:provisioning199:assigners");
    loader.put(p + "operateOnGrouperGroups", "true");
    loader.put(p + "operateOnGrouperEntities", "true");
    loader.put(p + "operateOnGrouperMemberships", "true");
    loader.put(p + "provisioningType", "membershipObjects");
    loader.put(p + "subjectSourcesToProvision", "jdbc");
    return p;
  }

  /**
   * whether a provisioner class is in this release
   * @param className
   * @return true if present
   */
  private static boolean hasClass(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * ProvisionableGroupSave on the team group in a session of the subject
   * @param subject
   * @param targetName
   * @param replaceAllSettings
   * @param metadata name, value pairs
   */
  private void saveAs(Subject subject, String targetName, boolean replaceAllSettings, String... metadata) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave()
          .assignTargetName(targetName).assignGroup(this.teamGroup).assignReplaceAllSettings(replaceAllSettings);
      for (int i = 0; i < metadata.length; i += 2) {
        provisionableGroupSave.assignMetadataString(metadata[i], metadata[i + 1]);
      }
      provisionableGroupSave.save();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * expect the save to fail
   * @param message
   * @param subject
   * @param targetName
   * @param replaceAllSettings
   * @param metadata
   */
  private void assertSaveFails(String message, Subject subject, String targetName, boolean replaceAllSettings, String... metadata) {
    try {
      saveAs(subject, targetName, replaceAllSettings, metadata);
    } catch (RuntimeException re) {
      return;
    }
    fail(message);
  }

  /**
   * stored metadata on the team group, read as root
   * @param targetName
   * @return the map, empty if nothing stored
   */
  private Map<String, Object> stored(String targetName) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(this.teamGroup, targetName);
      Map<String, Object> result = value == null ? null : value.getMetadataNameValues();
      return result == null ? new HashMap<String, Object>() : result;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // part 1: group reference items
  // ---------------------------------------------------------------------------------------------

  /**
   * Datadog md_adminGroupName needs READ on the named group for a non-wheel saver
   */
  public void testDatadogAdminGroupNeedsRead() {
    if (!hasClass(DATADOG_CLASS)) {
      return;
    }
    String targetName = "junitProv199dd_" + this.suffix;
    String p = configureProvisioner(targetName, DATADOG_CLASS);
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put(p + "datadogExternalSystemConfigId", "datadogJunit199");
    loader.put(p + "datadogAddTeamAdminMetadata", "true");
    GrouperProvisioningSettings.getTargets(false);

    groupReferenceChecks(targetName, "md_adminGroupName");
  }

  /**
   * TrueFoundry md_trueFoundryManagerGroupName needs READ on the named group for a non-wheel saver
   */
  public void testTrueFoundryManagerGroupNeedsRead() {
    if (!hasClass(TRUEFOUNDRY_CLASS)) {
      return;
    }
    String targetName = "junitProv199tf_" + this.suffix;
    String p = configureProvisioner(targetName, TRUEFOUNDRY_CLASS);
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put(p + "trueFoundryExternalSystemConfigId", "trueFoundryJunit199");
    loader.put(p + "trueFoundryAddTeamManagerMetadata", "true");
    GrouperProvisioningSettings.getTargets(false);

    groupReferenceChecks(targetName, "md_trueFoundryManagerGroupName");
  }

  /**
   * shared checks for a group reference item
   * @param targetName
   * @param metadataName
   */
  private void groupReferenceChecks(String targetName, String metadataName) {

    // UI path: the group save handler calls validateMetadataInputForFolder in the user's session
    Map<String, Object> input = new HashMap<String, Object>();
    input.put(metadataName, this.secretGroup.getName());
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(targetName);
      grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
      Map<String, String> errors = grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().validateMetadataInputForFolder(input);
      assertTrue("UI validation flags the unreadable group", errors != null && errors.containsKey(metadataName));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // API path, no READ: refused, nothing stored
    assertSaveFails("no READ on the named group", SubjectTestHelper.SUBJ0, targetName, true, metadataName, this.secretGroup.getName());
    assertNull(stored(targetName).get(metadataName));

    // a group that does not exist: refused the same way
    assertSaveFails("unknown group", SubjectTestHelper.SUBJ0, targetName, true, metadataName, "test:provisioning199:doesNotExist");

    // a group the user can read: fine
    saveAs(SubjectTestHelper.SUBJ0, targetName, true, metadataName, this.teamGroup.getName());
    assertEquals(this.teamGroup.getName(), stored(targetName).get(metadataName));

    // with READ on the secret group: fine
    grouperSession = GrouperSession.startRootSession();
    try {
      this.secretGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    saveAs(SubjectTestHelper.SUBJ0, targetName, true, metadataName, this.secretGroup.getName());
    assertEquals(this.secretGroup.getName(), stored(targetName).get(metadataName));

    // root may name any group
    saveAs(SubjectFinder.findRootSubject(), targetName, true, metadataName, "test:provisioning199:updaters");
    assertEquals("test:provisioning199:updaters", stored(targetName).get(metadataName));
  }

  // ---------------------------------------------------------------------------------------------
  // part 2: item settings in ProvisionableGroupSave
  // ---------------------------------------------------------------------------------------------

  /**
   * groupIdThatCanUpdate, canChange=false, canUpdate=false and dropdown options are enforced for
   * a non-wheel caller, and locked items keep their stored values
   */
  public void testLockedItemsAndOptions() {
    String targetName = "junitProv199ldap_" + this.suffix;
    String p = configureProvisioner(targetName, LDAP_CLASS);
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "configureMetadata", "true");
    loader.put(p + "numberOfMetadata", "4");
    loader.put(p + "metadata.0.name", "md_locked199");
    loader.put(p + "metadata.0.valueType", "string");
    loader.put(p + "metadata.0.showForGroup", "true");
    loader.put(p + "metadata.0.groupIdThatCanUpdate", this.updatersGroup.getId());
    loader.put(p + "metadata.1.name", "md_noChange199");
    loader.put(p + "metadata.1.valueType", "string");
    loader.put(p + "metadata.1.showForGroup", "true");
    loader.put(p + "metadata.1.canChange", "false");
    loader.put(p + "metadata.2.name", "md_noUpdate199");
    loader.put(p + "metadata.2.valueType", "string");
    loader.put(p + "metadata.2.showForGroup", "true");
    loader.put(p + "metadata.2.canUpdate", "false");
    loader.put(p + "metadata.3.name", "md_choice199");
    loader.put(p + "metadata.3.valueType", "string");
    loader.put(p + "metadata.3.formElementType", "dropdown");
    loader.put(p + "metadata.3.dropdownValues", "reader,member");
    loader.put(p + "metadata.3.showForGroup", "true");
    GrouperProvisioningSettings.getTargets(false);

    // groupIdThatCanUpdate: SUBJ0 is not in the updaters group
    assertSaveFails("groupIdThatCanUpdate", SubjectTestHelper.SUBJ0, targetName, true, "md_locked199", "x");

    // option list
    assertSaveFails("not an option", SubjectTestHelper.SUBJ0, targetName, true, "md_choice199", "owner");
    assertNull(stored(targetName).get("md_choice199"));

    // first save (insert): canChange / canUpdate items may be set
    saveAs(SubjectTestHelper.SUBJ0, targetName, true, "md_choice199", "member", "md_noChange199", "a", "md_noUpdate199", "b");
    assertEquals("member", stored(targetName).get("md_choice199"));
    assertEquals("a", stored(targetName).get("md_noChange199"));
    assertEquals("b", stored(targetName).get("md_noUpdate199"));

    // canChange=false with a stored value, canUpdate=false on an existing assignment
    assertSaveFails("canChange", SubjectTestHelper.SUBJ0, targetName, false, "md_noChange199", "c");
    assertSaveFails("canUpdate", SubjectTestHelper.SUBJ0, targetName, false, "md_noUpdate199", "d");
    assertEquals("a", stored(targetName).get("md_noChange199"));
    assertEquals("b", stored(targetName).get("md_noUpdate199"));

    // root sets the locked item
    saveAs(SubjectFinder.findRootSubject(), targetName, false, "md_locked199", "rootValue");
    assertEquals("rootValue", stored(targetName).get("md_locked199"));

    // a non-wheel replace-all save that leaves the locked items out keeps their stored values
    saveAs(SubjectTestHelper.SUBJ0, targetName, true, "md_choice199", "reader");
    assertEquals("reader", stored(targetName).get("md_choice199"));
    assertEquals("rootValue", stored(targetName).get("md_locked199"));
    assertEquals("a", stored(targetName).get("md_noChange199"));
    assertEquals("b", stored(targetName).get("md_noUpdate199"));

    // a member of the updaters group may change the locked item (also needs to be an assigner and ADMIN)
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.updatersGroup.addMember(SubjectTestHelper.SUBJ0, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    saveAs(SubjectTestHelper.SUBJ0, targetName, false, "md_locked199", "updaterValue");
    assertEquals("updaterValue", stored(targetName).get("md_locked199"));
  }
}
