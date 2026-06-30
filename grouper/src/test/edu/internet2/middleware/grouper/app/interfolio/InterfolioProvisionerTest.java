package edu.internet2.middleware.grouper.app.interfolio;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Tests for the Interfolio provisioner.  The API-level tests exercise the external system + mock +
 * api commands end to end; the full-sync test exercises the provisioner.  All are gated on
 * tomcatRunTests() since they reach the mock servlet over HTTP.
 */
public class InterfolioProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new InterfolioProvisionerTest("testInterfolioApiUserLifecycle"));
  }

  @Override
  public String defaultConfigId() {
    return "myInterfolioProvisioner";
  }

  public InterfolioProvisionerTest(String name) {
    super(name);
  }

  public InterfolioProvisionerTest() {
  }

  public void setUp() {
    super.setUp();

    InterfolioProvisionerTestUtils.setupInterfolioExternalSystem();

    // clear the mock table between tests
    try {
      new GcDbAccess().connectionName("grouper").sql("delete from mock_interfolio_user").executeSql();
    } catch (Exception e) {
      // table may not exist yet
    }
  }

  /**
   * Exercise the working api commands directly against the mock: create, search, update, subscribe
   * and unsubscribe to RPT and FS.
   */
  public void testInterfolioApiUserLifecycle() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = InterfolioProvisionerTestUtils.EXTERNAL_SYSTEM_CONFIG_ID;

    // create
    InterfolioUser created = GrouperInterfolioApiCommands.createUser(configId,
        "jsmith", "jsmith@upenn.edu", "internal", "John", "Smith", "jsmith@upenn.edu");
    assertNotNull(created.getPid());

    // search returns the created user
    List<InterfolioUser> found = GrouperInterfolioApiCommands.searchUsers(configId, "jsmith", 25, 1);
    assertEquals(1, GrouperUtil.length(found));
    assertEquals("jsmith@upenn.edu", found.get(0).getEmail());
    assertEquals(created.getPid(), found.get(0).getPid());

    // update (mutable fields)
    InterfolioUser updated = GrouperInterfolioApiCommands.updateUser(configId, created.getPid(),
        "jsmith", "jsmith@upenn.edu", "internal", "Johnny", "Smith", "jsmith@upenn.edu");
    assertEquals("Johnny", updated.getFirstName());

    // subscribe to RPT and FS, then unsubscribe - verify via the mock table flags
    GrouperInterfolioApiCommands.subscribeUserToRpt(configId, created.getPid());
    GrouperInterfolioApiCommands.subscribeUserToFs(configId, created.getPid());
    assertEquals("T", rptFlag(created.getPid()));
    assertEquals("T", fsFlag(created.getPid()));

    GrouperInterfolioApiCommands.unsubscribeUserFromRpt(configId, created.getPid());
    GrouperInterfolioApiCommands.unsubscribeUserFromFs(configId, created.getPid());
    assertEquals("F", rptFlag(created.getPid()));
    assertEquals("F", fsFlag(created.getPid()));
  }

  /**
   * Creating a user with a duplicate email should fail (the IAM API rejects it with a 400).
   */
  public void testInterfolioCreateDuplicateEmailFails() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = InterfolioProvisionerTestUtils.EXTERNAL_SYSTEM_CONFIG_ID;

    GrouperInterfolioApiCommands.createUser(configId, "wangje", "wangje@upenn.edu", "internal", "Jenny", "Wang", "wangje@upenn.edu");

    try {
      GrouperInterfolioApiCommands.createUser(configId, "wangje", "wangje@upenn.edu", "internal", "Jenny", "Wang", "wangje@upenn.edu");
      fail("Expected a duplicate-email failure");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("already exists"));
    }
  }

  /**
   * Updating institution_user_id should fail (it is immutable).
   */
  public void testInterfolioUpdateImmutableUidFails() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = InterfolioProvisionerTestUtils.EXTERNAL_SYSTEM_CONFIG_ID;

    InterfolioUser created = GrouperInterfolioApiCommands.createUser(configId, "marycats", "marycats@upenn.edu", "internal", "Mary", "Smith", "marycats@upenn.edu");

    try {
      GrouperInterfolioApiCommands.updateUser(configId, created.getPid(), "marycats2", "marycats@upenn.edu", "internal", "Mary", "Smith", "marycats@upenn.edu");
      fail("Expected an immutable institution_user_id failure");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("can't be changed"));
    }
  }

  /**
   * Full sync: mark a folder provisionable, add members to a group, and provision the members as
   * Interfolio users.  Entity-only, so only mock_interfolio_user rows are produced.
   *
   * NOTE: the entity-only provisioning config in InterfolioProvisionerTestUtils may need tuning on
   * the first real run; this asserts the members land in the mock as users.
   */
  public void testFullProvisionInterfolio() {

    if (!tomcatRunTests()) {
      return;
    }

    InterfolioProvisionerTestUtils.configureInterfolioProvisioner(new InterfolioProvisionerTestConfigInput());

    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_interfolio_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myInterfolioProvisioner");
    attributeValue.setTargetName("myInterfolioProvisioner");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

    // the two members should be provisioned as Interfolio users
    assertEquals(2, HibernateSession.byHqlStatic().createQuery("from InterfolioUser").list(InterfolioUser.class).size());
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    // both were granted RPT and FS access on create (enableFs defaults to true)
    String email0 = "test.subject.0@somewhere.someSchool.edu";
    String email1 = "test.subject.1@somewhere.someSchool.edu";
    assertEquals("T", rptFlagByEmail(email0));
    assertEquals("T", fsFlagByEmail(email0));
    assertEquals("T", rptFlagByEmail(email1));
    assertEquals("T", fsFlagByEmail(email1));

    // remove subject 1 from the group and re-sync: deprovision should unsubscribe them from RPT and FS,
    // but the user account remains in Interfolio (no hard delete)
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(2, HibernateSession.byHqlStatic().createQuery("from InterfolioUser").list(InterfolioUser.class).size());
    assertEquals("F", rptFlagByEmail(email1));
    assertEquals("F", fsFlagByEmail(email1));
    // subject 0 still has access
    assertEquals("T", rptFlagByEmail(email0));
    assertEquals("T", fsFlagByEmail(email0));
  }

  /**
   * Full sync with enableFs=false: provision a user (granted RPT only, FS never touched), then remove
   * them from the group and deprovision (unsubscribed from RPT; FS still never touched).
   */
  public void testFullProvisionInterfolioRptOnly() {

    if (!tomcatRunTests()) {
      return;
    }

    InterfolioProvisionerTestUtils.configureInterfolioProvisioner(
        new InterfolioProvisionerTestConfigInput().addExtraConfig("enableFs", "false"));

    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_interfolio_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myInterfolioProvisioner");
    attributeValue.setTargetName("myInterfolioProvisioner");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    String email0 = "test.subject.0@somewhere.someSchool.edu";

    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    assertEquals(1, HibernateSession.byHqlStatic().createQuery("from InterfolioUser").list(InterfolioUser.class).size());

    // RPT granted, FS NOT subscribed (enableFs=false)
    assertEquals("T", rptFlagByEmail(email0));
    assertEquals("F", fsFlagByEmail(email0));

    // remove from group and deprovision: unsubscribed from RPT, FS still untouched
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);
    grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());

    assertEquals(1, HibernateSession.byHqlStatic().createQuery("from InterfolioUser").list(InterfolioUser.class).size());
    assertEquals("F", rptFlagByEmail(email0));
    assertEquals("F", fsFlagByEmail(email0));
  }

  /**
   * @param email the user's email
   * @return the rpt flag ("T"/"F") from the mock table
   */
  private static String rptFlagByEmail(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select rpt from mock_interfolio_user where email = ?").addBindVar(email).select(String.class);
  }

  /**
   * @param email the user's email
   * @return the fs flag ("T"/"F") from the mock table
   */
  private static String fsFlagByEmail(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select fs from mock_interfolio_user where email = ?").addBindVar(email).select(String.class);
  }

  /**
   * @param pid the Interfolio person id
   * @return the rpt flag ("T"/"F") from the mock table
   */
  private static String rptFlag(String pid) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select rpt from mock_interfolio_user where pid = ?").addBindVar(pid).select(String.class);
  }

  /**
   * @param pid the Interfolio person id
   * @return the fs flag ("T"/"F") from the mock table
   */
  private static String fsFlag(String pid) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select fs from mock_interfolio_user where pid = ?").addBindVar(pid).select(String.class);
  }

}
