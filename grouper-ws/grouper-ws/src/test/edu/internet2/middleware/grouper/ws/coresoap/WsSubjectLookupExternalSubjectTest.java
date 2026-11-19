package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security advisory M7 (wsAddMemberExternalSubjectAsRoot): WS addMember with addExternalSubjectIfNotFound
 * used to create the external subject (and its externalSubjects.autoaddGroups memberships) as root,
 * before the UPDATE check on the group.  Now the caller must be allowed to edit external users
 * (wheel or externalSubjects.groupAllowedForEdit) AND have UPDATE on the target group, both checked
 * before anything is created, and the EXTERNAL_SUBJECT_ADD audit records the caller.
 * Before the fix the denied cases create the subject and the auto-add membership, and the audit
 * has no act-as member.
 * </pre>
 */
public class WsSubjectLookupExternalSubjectTest extends GrouperTest {

  /** group a caller adds to */
  private static final String TARGET_GROUP_NAME = "test:viewOnly";

  /** externalSubjects.groupAllowedForEdit */
  private static final String EDITORS_GROUP_NAME = "etc:externalEditors";

  /** externalSubjects.autoaddGroups */
  private static final String AUTOADD_GROUP_NAME = "test:guestAccess";

  /** target group */
  private Group targetGroup;

  /** auto-add group */
  private Group autoaddGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsSubjectLookupExternalSubjectTest("testDeniedViewOnlyNotEditor"));
  }

  /**
   * @param name
   */
  public WsSubjectLookupExternalSubjectTest(String name) {
    super(name);
  }

  /**
   * external subject source and config, the groups.  SUBJ1 may edit external users.
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ExternalSubjectTest.setupHelper();
    // external subjects are off by default (externalSubjects.enabled, 2026-11-08); this test writes them
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.groupAllowedForEdit", EDITORS_GROUP_NAME);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.autoaddGroups", AUTOADD_GROUP_NAME);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.autoaddGroupActions", "insert,update");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group editors = new GroupSave(grouperSession).assignName(EDITORS_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    editors.addMember(SubjectTestHelper.SUBJ1, false);
    this.autoaddGroup = new GroupSave(grouperSession).assignName(AUTOADD_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
    this.targetGroup = new GroupSave(grouperSession).assignName(TARGET_GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperServiceUtils.testSession = null;
    WsSubjectLookup.assignExternalSubjectTargetGroup(null, null);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.groupAllowedForEdit");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.autoaddGroups");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.autoaddGroupActions");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.enabled");
    super.tearDown();
  }

  /**
   * call WS addMember on the target group as a caller with addExternalSubjectIfNotFound
   * @param caller
   * @param identifier
   * @return the results
   */
  private static WsAddMemberResults addMemberAs(Subject caller, String identifier) {
    // the logic method uses this session; starting it also makes it the static session
    GrouperServiceUtils.testSession = GrouperSession.start(caller);
    return GrouperServiceLogic.addMember(GrouperVersion.valueOfIgnoreCase("v4_0_000"),
        new WsGroupLookup(TARGET_GROUP_NAME, null), new WsSubjectLookup[] {new WsSubjectLookup(null, null, identifier)},
        false, null, null, null, false, false, null, null, null, null, true);
  }

  /**
   * @param identifier
   * @return the external subject or null, looked up as root
   */
  private static Subject findExternalSubject(String identifier) {
    GrouperSession.startRootSession();
    SubjectFinder.flushCache();
    return SubjectFinder.findByIdentifierAndSource(identifier, ExternalSubject.sourceId(), false);
  }

  /**
   * assert nothing was created for the identifier and the auto-add group is empty
   * @param results
   * @param identifier
   */
  private void assertNothingCreated(WsAddMemberResults results, String identifier) {
    assertFalse(results.getResults()[0].getResultMetadata().getResultCode(),
        "T".equals(results.getResults()[0].getResultMetadata().getSuccess()));
    assertNull(findExternalSubject(identifier));
    assertEquals(0, this.autoaddGroup.getImmediateMembers().size());
    assertEquals(0, this.targetGroup.getImmediateMembers().size());
  }

  /**
   * the reported attack: VIEW only on the group and not an external-user editor
   */
  public void testDeniedViewOnlyNotEditor() {
    this.targetGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    assertNothingCreated(addMemberAs(SubjectTestHelper.SUBJ0, "attacker@example.org"), "attacker@example.org");
  }

  /**
   * UPDATE on the group is not enough without the external-user edit right
   */
  public void testDeniedUpdateButNotEditor() {
    this.targetGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    assertNothingCreated(addMemberAs(SubjectTestHelper.SUBJ0, "attacker@example.org"), "attacker@example.org");
  }

  /**
   * the external-user edit right is not enough without UPDATE on the group
   */
  public void testDeniedEditorWithoutUpdate() {
    this.targetGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW, false);
    assertNothingCreated(addMemberAs(SubjectTestHelper.SUBJ1, "attacker@example.org"), "attacker@example.org");
  }

  /**
   * a direct lookup with no target group resolved in the session creates nothing (fails closed)
   */
  public void testDeniedWithoutTargetGroup() {
    WsSubjectLookup.assignExternalSubjectTargetGroup(null, null);
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(null, null, "nogroup@example.org");
    assertNull(wsSubjectLookup.retrieveSubject(true));
    assertEquals(WsSubjectLookup.SubjectFindResult.SUBJECT_NOT_FOUND, wsSubjectLookup.retrieveSubjectFindResult());
    assertNull(findExternalSubject("nogroup@example.org"));
    assertEquals(0, this.autoaddGroup.getImmediateMembers().size());
  }

  /**
   * editor with UPDATE: subject created, added to the group and the auto-add group, audited as the caller
   */
  public void testAllowedEditorWithUpdate() {
    this.targetGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE, false);
    WsAddMemberResults results = addMemberAs(SubjectTestHelper.SUBJ1, "allowed@example.org");
    assertEquals(results.getResults()[0].getResultMetadata().getResultCode(), "T",
        results.getResults()[0].getResultMetadata().getSuccess());

    Subject created = findExternalSubject("allowed@example.org");
    assertNotNull(created);
    assertTrue(this.targetGroup.hasImmediateMember(created));
    assertTrue(this.autoaddGroup.hasImmediateMember(created));

    // the add is audited with the caller as the actor, not root
    String subj1MemberId = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, true).getUuid();
    List<String> actAsMemberIds = new GcDbAccess().sql("select ae.act_as_member_id from grouper_audit_entry ae, grouper_audit_type at "
        + "where ae.audit_type_id = at.id and at.audit_category = 'externalSubject' and at.action_name = 'addExternalSubject' "
        + "and ae.string01 = ?").addBindVar(created.getId()).selectList(String.class);
    assertEquals(1, actAsMemberIds.size());
    assertEquals(subj1MemberId, actAsMemberIds.get(0));
  }
}
