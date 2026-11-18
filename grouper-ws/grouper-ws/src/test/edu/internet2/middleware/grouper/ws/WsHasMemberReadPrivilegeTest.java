package edu.internet2.middleware.grouper.ws;

import java.sql.Timestamp;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * WS hasMember resolved the group with VIEW and then answered the membership question without the
 * field's read privilege, for current and point in time (PIT) queries.  So a caller who could see
 * the group but not list its members could ask whether any subject is (or was) a member.
 *
 * The fix checks the read privilege in WsMemberFilter.hasMember (current) and PITGroup.hasMember
 * (PIT, same rules as PITGroup.getMembers), and WsHasMemberResult reports a denial as EXCEPTION
 * with resultCode2 INSUFFICIENT_PRIVILEGES, never as IS_MEMBER / IS_NOT_MEMBER.
 */
public class WsHasMemberReadPrivilegeTest extends GrouperTest {

  /** group under test */
  private static final String GROUP_NAME = "test:hasMemberAuthz:protected";

  /** start of every PIT range used here */
  private static final Timestamp EPOCH = new Timestamp(0);

  /** grouper version for the service logic calls */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsHasMemberReadPrivilegeTest("testViewOnlyCannotAskCurrentOrPit"));
  }

  /**
   *
   */
  public WsHasMemberReadPrivilegeTest() {
    super();
  }

  /**
   * @param name
   */
  public WsHasMemberReadPrivilegeTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    // so the test subjects only have the privileges granted below
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperServiceUtils.testSession = null;
    super.tearDown();
  }

  /**
   * as root: the group with SUBJ1 and SUBJ3 as members; SUBJ0 VIEW only, SUBJ2 READ (and VIEW);
   * PIT synced
   * @return the group
   */
  private Group setupGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.addMember(SubjectTestHelper.SUBJ1);
      group.addMember(SubjectTestHelper.SUBJ3);
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
      ChangeLogTempToEntity.convertRecords();
      return group;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * as root: remove SUBJ1 (a former member from now on) and sync PIT
   */
  private void removeSubj1() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, GROUP_NAME, true);
      group.deleteMember(SubjectTestHelper.SUBJ1);
      ChangeLogTempToEntity.convertRecords();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * run WS hasMember for SUBJ1 and SUBJ3 as a caller
   * @param caller
   * @param fieldName null for members
   * @param pointInTimeFrom null for a current query
   * @return the results
   */
  private WsHasMemberResults hasMemberAs(Subject caller, String fieldName, Timestamp pointInTimeFrom) {
    GrouperServiceUtils.testSession = GrouperSession.start(caller);
    try {
      WsSubjectLookup[] wsSubjectLookups = new WsSubjectLookup[] {
          new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), null, null),
          new WsSubjectLookup(SubjectTestHelper.SUBJ3.getId(), null, null)};
      return GrouperServiceLogic.hasMember(GROUPER_VERSION, new WsGroupLookup(GROUP_NAME, null),
          wsSubjectLookups, WsMemberFilter.All, null,
          fieldName == null ? null : FieldFinder.find(fieldName, true),
          false, false, null, null, pointInTimeFrom, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
  }

  /**
   * every subject result must be a denial: no IS_MEMBER / IS_NOT_MEMBER answer at all
   * @param wsHasMemberResults
   */
  private static void assertAllDenied(WsHasMemberResults wsHasMemberResults) {
    assertEquals("F", wsHasMemberResults.getResultMetadata().getSuccess());
    assertEquals(2, wsHasMemberResults.getResults().length);
    for (WsHasMemberResult wsHasMemberResult : wsHasMemberResults.getResults()) {
      assertEquals(wsHasMemberResult.getResultMetadata().getResultMessage(),
          WsHasMemberResultCode.EXCEPTION.name(), wsHasMemberResult.getResultMetadata().getResultCode());
      assertEquals("INSUFFICIENT_PRIVILEGES", wsHasMemberResult.getResultMetadata().getResultCode2());
      assertEquals("F", wsHasMemberResult.getResultMetadata().getSuccess());
      // no stack trace for an expected denial
      assertFalse(wsHasMemberResult.getResultMetadata().getResultMessage(),
          wsHasMemberResult.getResultMetadata().getResultMessage().contains("\tat "));
    }
  }

  /**
   * @param wsHasMemberResults
   * @param index
   * @return the result code of one subject
   */
  private static String code(WsHasMemberResults wsHasMemberResults, int index) {
    return wsHasMemberResults.getResults()[index].getResultMetadata().getResultCode();
  }

  /**
   * VIEW without READ: current and PIT hasMember are denied for every subject in the batch,
   * including a former member; READ gets the real answers
   */
  public void testViewOnlyCannotAskCurrentOrPit() {
    setupGroup();

    // current, before removal
    assertAllDenied(hasMemberAs(SubjectTestHelper.SUBJ0, null, null));
    WsHasMemberResults readerResults = hasMemberAs(SubjectTestHelper.SUBJ2, null, null);
    assertEquals(WsHasMemberResultCode.IS_MEMBER.name(), code(readerResults, 0));
    assertEquals(WsHasMemberResultCode.IS_MEMBER.name(), code(readerResults, 1));

    removeSubj1();

    // current, after removal: the reader sees the change
    readerResults = hasMemberAs(SubjectTestHelper.SUBJ2, null, null);
    assertEquals(WsHasMemberResultCode.IS_NOT_MEMBER.name(), code(readerResults, 0));

    // PIT: the former member is only revealed to the reader
    assertAllDenied(hasMemberAs(SubjectTestHelper.SUBJ0, null, EPOCH));
    readerResults = hasMemberAs(SubjectTestHelper.SUBJ2, null, EPOCH);
    assertEquals(WsHasMemberResultCode.IS_MEMBER.name(), code(readerResults, 0));
    assertEquals(WsHasMemberResultCode.IS_MEMBER.name(), code(readerResults, 1));
  }

  /**
   * a non-default field needs its own read privilege: READ on the group does not let you ask
   * the admins list (read priv ADMIN)
   */
  public void testPrivilegeFieldNeedsItsReadPriv() {
    setupGroup();
    assertAllDenied(hasMemberAs(SubjectTestHelper.SUBJ2, "admins", null));
    assertAllDenied(hasMemberAs(SubjectTestHelper.SUBJ2, "admins", EPOCH));

    WsHasMemberResults rootResults = hasMemberAs(SubjectFinder.findRootSubject(), "admins", null);
    assertEquals("T", rootResults.getResultMetadata().getSuccess());
  }

  /**
   * PITGroup.hasMember itself enforces the rule (not only the WS layer); a deleted group is wheel
   * or root only, even for a former reader
   */
  public void testPitGroupHasMemberDirect() {
    Group group = setupGroup();
    String fieldId = Group.getDefaultList().getUuid();
    String groupId = group.getId();

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      PITGroup pitGroup = PITGroupFinder.findBySourceId(groupId, true).iterator().next();
      try {
        pitGroup.hasMember(SubjectTestHelper.SUBJ1, fieldId, EPOCH, null, null);
        fail("VIEW only must not check PIT membership");
      } catch (InsufficientPrivilegeException ipe) {
        // expected
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      PITGroup pitGroup = PITGroupFinder.findBySourceId(groupId, true).iterator().next();
      assertTrue(pitGroup.hasMember(SubjectTestHelper.SUBJ1, fieldId, EPOCH, null, null));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // delete the group: the PIT group becomes inactive
    GrouperSession rootSession = GrouperSession.startRootSession();
    Set<PITGroup> pitGroups = null;
    try {
      group.delete();
      ChangeLogTempToEntity.convertRecords();
      pitGroups = PITGroupFinder.findBySourceId(groupId, true);
      PITGroup inactivePitGroup = pitGroups.iterator().next();
      assertFalse(inactivePitGroup.isActive());
      assertTrue("root can still check a deleted group",
          inactivePitGroup.hasMember(SubjectTestHelper.SUBJ1, fieldId, EPOCH, null, null));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      pitGroups.iterator().next().hasMember(SubjectTestHelper.SUBJ1, fieldId, EPOCH, null, null);
      fail("a deleted group is wheel or root only");
    } catch (InsufficientPrivilegeException ipe) {
      // expected
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * a group deleted but still active in point in time (the PIT sync has not run yet) fails closed with
   * InsufficientPrivilegeException, not a GroupNotFoundException stack trace (2026-11-10)
   */
  public void testPitGroupDeletedButPitStillActive() {
    Group group = setupGroup();
    String fieldId = Group.getDefaultList().getUuid();
    String groupId = group.getId();

    // read the PIT group while it is active, then delete the group without running the PIT sync
    PITGroup activePitGroup = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      activePitGroup = PITGroupFinder.findBySourceId(groupId, true).iterator().next();
      assertTrue(activePitGroup.isActive());
      group.delete();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    try {
      activePitGroup.hasMember(SubjectTestHelper.SUBJ1, fieldId, EPOCH, null, null);
      fail("a deleted group is wheel or root only, even before the PIT sync");
    } catch (InsufficientPrivilegeException ipe) {
      // expected
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
