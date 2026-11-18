package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * md_grouper_ldapGroupDnOverride is group metadata that a delegated assigner (groupAllowedToAssign)
 * can set, and the LDAP provisioner reads, writes and deletes that dn with Grouper's bind.  The save
 * is the gate (LdapSyncObjectMetadata, via LdapGroupDnOverrideGuard): a sysadmin (wheel or
 * GrouperSystem) may set any dn, anyone else only a dn under groupSearchBaseDn.  At run time the
 * stored value is used as is, and the bushy parent-folder walk never creates or deletes entries
 * outside the base.
 *
 * No LDAP server needed: the parent-folder methods are called with a null dao, so any attempt to
 * search, create or delete fails the test (NullPointerException) instead of returning.
 */
public class LdapGroupDnOverrideGuardTest extends GrouperTest {

  /** base for these tests */
  private static final String BASE = "ou=grouperGroups,dc=example,dc=edu";

  /** a dn outside the base */
  private static final String OUTSIDE_DN = "cn=DirectoryAdmins,ou=security,dc=example,dc=edu";

  /** a dn under the base */
  private static final String INSIDE_DN = "cn=myGroup,ou=grouperGroups,dc=example,dc=edu";

  /** wheel group made by this test */
  private static final String WHEEL_GROUP = "etc:wheel201";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LdapGroupDnOverrideGuardTest("testNonSysadminOutOfBaseRefusedAtSave"));
  }

  /**
   * @param name
   */
  public LdapGroupDnOverrideGuardTest(String name) {
    super(name);
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.use");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.group");
    super.tearDown();
  }

  // ------------------------------------------------------------------------------------------------
  // helpers

  /**
   * @param bushy
   * @param baseDn group search base dn
   * @return an ldap provisioner allowing the dn override
   */
  private static LdapSync ldapSync(boolean bushy, String baseDn) {
    LdapSync ldapSync = new LdapSync();
    ldapSync.setConfigId("ldapProv201");
    LdapSyncConfiguration configuration = (LdapSyncConfiguration) ldapSync.retrieveGrouperProvisioningConfiguration();
    configuration.setAllowLdapGroupDnOverride(true);
    configuration.setGroupSearchBaseDn(baseDn);
    configuration.setGroupDnType(bushy ? LdapSyncGroupDnType.bushy : LdapSyncGroupDnType.flat);
    return ldapSync;
  }

  /**
   * @param baseDn group search base dn
   * @return metadata of an ldap provisioner allowing the dn override
   */
  private static LdapSyncObjectMetadata metadata(String baseDn) {
    LdapSyncObjectMetadata metadata = new LdapSyncObjectMetadata();
    metadata.setGrouperProvisioner(ldapSync(false, baseDn));
    return metadata;
  }

  /**
   * Validate an override the way the save paths do, in a session of the given subject:
   * the group method (ProvisionableGroupSave) and the folder method (the UI group provisioning save).
   * @param subject saving subject, null for GrouperSystem
   * @param baseDn group search base dn
   * @param overrideDn md_grouper_ldapGroupDnOverride
   * @return the error of the group method, also asserting the folder method agrees
   */
  private static String saveError(Subject subject, String baseDn, String overrideDn) {
    GrouperSession grouperSession = subject == null ? GrouperSession.startRootSession() : GrouperSession.start(subject);
    try {
      LdapSyncObjectMetadata metadata = metadata(baseDn);
      Map<String, Object> input = new HashMap<String, Object>();
      input.put(LdapGroupDnOverrideGuard.MD_GROUP_DN_OVERRIDE, overrideDn);
      Map<String, String> groupErrors = metadata.validateMetadataInputForGroup(input);
      Map<String, String> folderErrors = metadata.validateMetadataInputForFolder(input);
      String groupError = groupErrors == null ? null : groupErrors.get(LdapGroupDnOverrideGuard.MD_GROUP_DN_OVERRIDE);
      String folderError = folderErrors == null ? null : folderErrors.get(LdapGroupDnOverrideGuard.MD_GROUP_DN_OVERRIDE);
      assertEquals("UI (folder validation) and API (group validation) must agree: " + groupErrors + ", " + folderErrors,
          groupError == null, folderError == null);
      return groupError;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * make SUBJ0 a wheel member (config override, like GrouperProvisioningServiceTest)
   */
  private static void setupWheel() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group wheel = new GroupSave(rootSession).assignName(WHEEL_GROUP).assignCreateParentStemsIfNotExist(true).save();
      wheel.addMember(SubjectTestHelper.SUBJ0, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", WHEEL_GROUP);
  }

  /**
   * @param name private method name
   * @return the parent folder method
   * @throws Exception
   */
  private static Method parentFolderMethod(String name) throws Exception {
    Method method = LdapProvisioningTargetDao.class.getDeclaredMethod(name,
        LdapSyncConfiguration.class, LdapSyncDaoForLdap.class, String.class);
    method.setAccessible(true);
    return method;
  }

  /**
   * run a bushy parent-folder method with a null dao: it must return without touching LDAP
   * @param name deleteEmptyParentFolders or createParentFolders
   * @param groupDn group dn
   * @throws Exception
   */
  private static void assertParentFolderMethodStaysOffLdap(String name, String groupDn) throws Exception {
    LdapSync ldapSync = ldapSync(true, BASE);
    LdapProvisioningTargetDao dao = new LdapProvisioningTargetDao();
    dao.setGrouperProvisioner(ldapSync);
    try {
      parentFolderMethod(name).invoke(dao, ldapSync.retrieveGrouperProvisioningConfiguration(), null, groupDn);
    } catch (InvocationTargetException ite) {
      fail(name + " must not search, create or delete parent entries of a dn outside groupSearchBaseDn"
          + " (and must not refuse the dn, a sysadmin may set it): " + ite.getCause());
    }
  }

  // ------------------------------------------------------------------------------------------------
  // tests

  /**
   * dn comparison: strictly below the base, case and spacing insensitive
   */
  public void testIsStrictlyUnderBase() {
    assertTrue(LdapGroupDnOverrideGuard.isStrictlyUnderBase("cn=a,ou=grouperGroups,dc=example,dc=edu", BASE));
    assertTrue(LdapGroupDnOverrideGuard.isStrictlyUnderBase("cn=a,ou=x,OU=GrouperGroups, DC=Example,dc=edu", BASE));
    // the base itself is not a group
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase(BASE, BASE));
    // other OU, the parent of the base, escaped commas that only look like the base
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase(OUTSIDE_DN, BASE));
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase("dc=example,dc=edu", BASE));
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase("cn=a\\,ou=grouperGroups\\,dc=example\\,dc=edu,ou=evil,dc=edu", BASE));
    // unparseable or blank
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase("not a dn", BASE));
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase(null, BASE));
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase(INSIDE_DN, null));
    assertFalse(LdapGroupDnOverrideGuard.isStrictlyUnderBase(INSIDE_DN, " "));
    // the dn rule for non-sysadmins, and no saver means not a sysadmin
    assertNull(LdapGroupDnOverrideGuard.overrideError(null, BASE));
    assertNull(LdapGroupDnOverrideGuard.overrideError(INSIDE_DN, BASE));
    assertNotNull(LdapGroupDnOverrideGuard.overrideError(INSIDE_DN, ""));
    assertNotNull(LdapGroupDnOverrideGuard.overrideErrorForSaver(OUTSIDE_DN, BASE, null));
  }

  /**
   * a sysadmin (GrouperSystem, and a wheel member) may save an override outside the base, and at
   * run time the bushy parent-folder methods neither refuse that dn nor walk up from it (its parents
   * must already exist); the translator and LdapSyncLogic use the stored override unchanged
   * @throws Exception
   */
  public void testSysadminOutOfBaseAllowedAtSaveAndUsedAtRunTime() throws Exception {
    assertNull("GrouperSystem may set any dn", saveError(null, BASE, OUTSIDE_DN));
    // a sysadmin may also set an override when the base is blank
    assertNull("GrouperSystem may set any dn", saveError(null, "", OUTSIDE_DN));

    setupWheel();
    assertNull("a wheel member may set any dn", saveError(SubjectTestHelper.SUBJ0, BASE, OUTSIDE_DN));

    // run time: no refusal, no LDAP calls for parents outside the base
    assertParentFolderMethodStaysOffLdap("createParentFolders", "cn=DirectoryAdmins,ou=a,ou=security,dc=example,dc=edu");
    assertParentFolderMethodStaysOffLdap("deleteEmptyParentFolders", "cn=DirectoryAdmins,ou=a,ou=security,dc=example,dc=edu");
  }

  /**
   * a delegated assigner who is not a sysadmin cannot save an override outside the base (UI and
   * API), nor any override when the base is blank
   */
  public void testNonSysadminOutOfBaseRefusedAtSave() {
    setupWheel();
    assertNotNull("non-wheel override outside groupSearchBaseDn must be refused",
        saveError(SubjectTestHelper.SUBJ1, BASE, OUTSIDE_DN));
    assertNotNull("non-wheel unparseable override must be refused",
        saveError(SubjectTestHelper.SUBJ1, BASE, "not a dn"));
    assertNotNull("non-wheel override with a blank base must be refused",
        saveError(SubjectTestHelper.SUBJ1, "", INSIDE_DN));
  }

  /**
   * a delegated assigner who is not a sysadmin can save an override under the base
   */
  public void testNonSysadminInBaseAllowed() {
    setupWheel();
    assertNull("non-wheel override under groupSearchBaseDn is allowed",
        saveError(SubjectTestHelper.SUBJ1, BASE, INSIDE_DN));
    assertNull("blank override is allowed", saveError(SubjectTestHelper.SUBJ1, BASE, ""));
  }

  /**
   * bushy create and delete of parent entries for a dn outside the base: return without touching
   * LDAP (before the fix both walked up to the directory root, searching, creating or deleting)
   * @throws Exception
   */
  public void testBushyDoesNotCreateParentsOutsideBase() throws Exception {
    assertParentFolderMethodStaysOffLdap("createParentFolders", OUTSIDE_DN);
    assertParentFolderMethodStaysOffLdap("createParentFolders", "cn=x,ou=a,ou=b,dc=other,dc=edu");
    assertParentFolderMethodStaysOffLdap("deleteEmptyParentFolders", OUTSIDE_DN);
  }
}
