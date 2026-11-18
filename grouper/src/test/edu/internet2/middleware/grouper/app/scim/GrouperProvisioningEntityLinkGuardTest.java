package edu.internet2.middleware.grouper.app.scim;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningEntityLinkGuard;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import junit.textui.TestRunner;

/**
 * Security tests for issue provisioningMatchingRelinksToPreclaimedAccount: a full sync must not move a
 * member that is already linked to target account A onto a different, pre-existing account B just
 * because B holds the member's current (mutable) matching value, e.g. its userName.  The member stays
 * on A with an object error (MAT), A is not renamed, deleted or orphaned, and B gets no memberships.
 * <p>
 * The first tests are plain unit tests of GrouperProvisioningEntityLinkGuard.  The others need the mock
 * SCIM servlet (junit.test.tomcat=true, tomcatRunTests()), like GrouperAwsProvisionerTest2 and
 * GrouperScim2ConflictAnchorTest; run the mock Tomcat with the daemon off.
 * </p>
 * <p>
 * The same file ships for every patched release (2.6.19, 4.26.0, 5.x, 6.x, 7.x).  APIs that differ by
 * release (GrouperScim2ApiCommands.createScimUser, the first/last name test setup from 5.3.0) are called by
 * reflection so the file compiles everywhere.
 * </p>
 */
public class GrouperProvisioningEntityLinkGuardTest extends GrouperProvisioningBaseTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningEntityLinkGuardTest("testFullSyncCurrentNameMatchKeepsLinkOnA"));
  }

  /**
   * @param name
   */
  public GrouperProvisioningEntityLinkGuardTest(String name) {
    super(name);
  }

  @Override
  public String defaultConfigId() {
    return "awsProvisioner";
  }

  // ---------------------------------------------------------------- unit tests (no mock)

  /**
   * @param keysAndValues
   * @return ordered map
   */
  private static Map<String, String> map(String... keysAndValues) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      result.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return result;
  }

  /**
   * a candidate contradicts the link only when something is comparable and nothing agrees
   */
  public void testLinkContradiction() {

    // different target id: contradiction (this is the pre-claimed account)
    String contradiction = GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a"), map("id", "b"));
    assertNotNull(contradiction);
    assertTrue(contradiction, contradiction.contains("linked id=a") && contradiction.contains("candidate id=b"));

    // same target id: the linked account itself, allowed
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a"), map("id", "a")));

    // no link (never provisioned, or the link was cleared before a re-insert): allowed
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(map(), map("id", "b")));
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(null, map("id", "b")));

    // nothing comparable (candidate has no value for the cached attribute): cannot tell, allowed
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a"), map()));
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a"), null));
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a"), map("userName", "x")));

    // agrees on one cached attribute (same id, name changed out of band): allowed
    assertNull(GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a", "userName", "old"), map("id", "a", "userName", "new")));

    // disagrees on every comparable cached attribute: contradiction
    contradiction = GrouperProvisioningEntityLinkGuard.linkContradiction(map("id", "a", "userName", "old"), map("id", "b", "userName", "new"));
    assertNotNull(contradiction);
    assertTrue(contradiction, contradiction.contains("userName"));
  }

  /**
   * the linked target entity is the one that agrees on at least one cached attribute
   */
  public void testLinkAgrees() {
    assertTrue(GrouperProvisioningEntityLinkGuard.linkAgrees(map("id", "a"), map("id", "a")));
    assertTrue(GrouperProvisioningEntityLinkGuard.linkAgrees(map("id", "a", "userName", "old"), map("id", "z", "userName", "old")));
    assertFalse(GrouperProvisioningEntityLinkGuard.linkAgrees(map("id", "a"), map("id", "b")));
    assertFalse(GrouperProvisioningEntityLinkGuard.linkAgrees(map("id", "a"), map()));
    assertFalse(GrouperProvisioningEntityLinkGuard.linkAgrees(map(), map("id", "a")));
    assertFalse(GrouperProvisioningEntityLinkGuard.linkAgrees(null, map("id", "a")));
    assertFalse(GrouperProvisioningEntityLinkGuard.linkAgrees(map("id", ""), map("id", "")));
  }

  // ---------------------------------------------------------------- mock SCIM tests

  /**
   * configure the aws scim provisioner against the mock and clear the mock tables.  Matching is the
   * ScimProvisionerTestUtils default: userName only, with the target id cached in
   * entityAttributeValueCache2 (source target).
   */
  private void setupMock() {
    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestConfigInput configInput = new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
        .assignBearerTokenExternalSystemConfigId("awsConfigId");
    // first/last name attributes exist in the test setup from 5.3.0 (see setupConfigs)
    invokeIfExists(configInput, ScimProvisionerTestConfigInput.class, "assignUseFirstLastName", true);
    configInput
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper");
    ScimProvisionerTestUtils.configureScimProvisioner(configInput);

    GrouperStartup.startup();

    // an earlier test in this JVM may have left its provisioner in the inheritable thread local
    GrouperProvisioner.removeCurrentGrouperProvisioner();

    // hitting the mock once makes sure the mock tables exist before we clear them
    GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId", null);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
  }

  /**
   * create a user in the mock directly (as an attacker or another tenant user would)
   * @param userName
   * @param externalId may be null
   * @return id
   */
  private static String createMockUser(String userName, String externalId) {
    GrouperScim2User user = new GrouperScim2User();
    user.setUserName(userName);
    user.setExternalId(externalId);
    user.setDisplayName(userName);
    String id = createScimUserAnyRelease("awsConfigId", user).getId();
    assertNotNull(id);
    return id;
  }

  /**
   * GrouperScim2ApiCommands.createScimUser by reflection: the signature differs by release
   * (configId, acceptHeader, user, fieldsToUpdate) up to 7.x, plus (configId, user, fieldsToUpdate,
   * ScimSettings) in newer releases.  Prefer the ScimSettings one when it is there.  String
   * parameters after the config id (accept header, org name) and the fields to update are null.
   * @param configId
   * @param user
   * @return the created user
   */
  private static GrouperScim2User createScimUserAnyRelease(String configId, GrouperScim2User user) {
    Method chosen = null;
    for (Method method : GrouperScim2ApiCommands.class.getMethods()) {
      if (!"createScimUser".equals(method.getName()) || method.getReturnType() != GrouperScim2User.class) {
        continue;
      }
      boolean hasSettings = false;
      for (Class<?> parameterType : method.getParameterTypes()) {
        hasSettings = hasSettings || "ScimSettings".equals(parameterType.getSimpleName());
      }
      if (chosen == null || hasSettings) {
        chosen = method;
      }
    }
    assertNotNull("GrouperScim2ApiCommands.createScimUser", chosen);
    Class<?>[] parameterTypes = chosen.getParameterTypes();
    Object[] args = new Object[parameterTypes.length];
    boolean configIdAssigned = false;
    try {
      for (int i = 0; i < parameterTypes.length; i++) {
        if (parameterTypes[i] == String.class && !configIdAssigned) {
          args[i] = configId;
          configIdAssigned = true;
        } else if (parameterTypes[i] == GrouperScim2User.class) {
          args[i] = user;
        } else if ("ScimSettings".equals(parameterTypes[i].getSimpleName())) {
          args[i] = parameterTypes[i].newInstance();
        }
      }
      return (GrouperScim2User)chosen.invoke(null, args);
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("createScimUser " + chosen, e);
    }
  }

  /**
   * call a one-argument method if this release has it (static if target is null)
   * @param target
   * @param theClass
   * @param methodName
   * @param arg boolean argument
   * @return true if called
   */
  private static boolean invokeIfExists(Object target, Class<?> theClass, String methodName, boolean arg) {
    Method method = null;
    try {
      method = theClass.getMethod(methodName, boolean.class);
    } catch (NoSuchMethodException nsme) {
      System.out.println("skip " + theClass.getSimpleName() + "." + methodName + ": not in this release");
      return false;
    }
    try {
      method.invoke(target, arg);
      return true;
    } catch (Exception e) {
      throw new RuntimeException(methodName, e);
    }
  }

  /**
   * call a no-argument static method if this release has it
   * @param theClass
   * @param methodName
   * @return true if called
   */
  private static boolean invokeStaticIfExists(Class<?> theClass, String methodName) {
    Method method = null;
    try {
      method = theClass.getMethod(methodName);
    } catch (NoSuchMethodException nsme) {
      System.out.println("skip " + theClass.getSimpleName() + "." + methodName + ": not in this release");
      return false;
    }
    try {
      method.invoke(null);
      return true;
    } catch (Exception e) {
      throw new RuntimeException(methodName, e);
    }
  }

  /**
   * @param id
   * @return the mock user or null
   */
  private static GrouperScim2User mockUserById(String id) {
    return HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2User where id = :id").setString("id", id)
        .uniqueResult(GrouperScim2User.class);
  }

  /**
   * @param userId
   * @return number of mock memberships of the user
   */
  private static int mockMembershipCount(String userId) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_scim_membership where user_id = ?").addBindVar(userId).select(int.class);
  }

  /**
   * provision test:testGroup (with SUBJ0) and return SUBJ0's account A id
   * @param grouperSession
   * @return id of account A
   */
  private String provisionSubj0(GrouperSession grouperSession) {
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("awsProvisioner");
    attributeValue.setTargetName("awsProvisioner");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // initial provision: creates account A linked to SUBJ0 (userName is the subject id)
    fullProvision();
    GrouperUtil.sleep(2000);

    GrouperScim2User accountA = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2User where userName = :u").setString("u", SubjectTestHelper.SUBJ0.getId())
        .uniqueResult(GrouperScim2User.class);
    assertNotNull(accountA);
    assertEquals("A has the group membership", 1, mockMembershipCount(accountA.getId()));

    assertEquals(accountA.getId(), syncMemberSubj0(grouperSession).getEntityAttributeValueCache2());
    return accountA.getId();
  }

  /**
   * @param grouperSession
   * @return the sync member of SUBJ0
   */
  private static GcGrouperSyncMember syncMemberSubj0(GrouperSession grouperSession) {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
    return gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member0.getId());
  }

  /**
   * store one awsProvisioner property in the database config
   * @param suffix property name after provisioner.awsProvisioner.
   * @param value
   */
  private static void storeProvisionerConfig(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("provisioner.awsProvisioner." + suffix).value(value).store();
  }

  @Override
  protected void setupConfigs() {
    // same subject setup as GrouperAwsProvisionerTest2 (first/last name attributes, from 5.3.0)
    invokeIfExists(null, RegistrySubject.class, "assignCreateOtherAttributes", true);
    super.setupConfigs();
  }

  /**
   * GrouperTest calls this after the config cache is cleared from 5.3.0 on (its own version is empty);
   * no @Override and no super call so this file also compiles on releases without the hook (2.6.19,
   * 5.0.0 to 5.2.0), where the first/last name setup does not exist either
   */
  protected void setupConfigsPostClearCache() {
    invokeStaticIfExists(RegistrySubject.class, "addFirstLastNameSubjectAttributes");
    invokeIfExists(null, RegistrySubject.class, "assignCreateOtherAttributes", false);
  }

}
