package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix 101: workflow identity is source id plus subject id.  A same-id subject from
 * another source must not view an instance, be resolved as the initiator, or as an explicit
 * approver.
 *
 * Setup as in the report: a group whose uuid is the id of a jdbc test subject, so g:gsa||id and
 * jdbc||id both exist.
 * Before the fix this does not compile (GrouperWorkflowSubjectIdentity is missing); the service
 * assertions fail because the lookups are by id only (view allowed for the other source, id only
 * lookup throws not unique or returns the wrong subject).
 * </pre>
 */
public class GrouperWorkflowSubjectIdentityTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /** the jdbc subject (source B) */
  private Subject jdbcSubject;

  /** group with the same id as the jdbc subject (source A, g:gsa) */
  private Group collisionGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowSubjectIdentityTest("testInitiatorAndViewAreSourceBound"));
  }

  /**
   */
  public GrouperWorkflowSubjectIdentityTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowSubjectIdentityTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    this.grouperSession = grouperSessionResult.getGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");

    this.jdbcSubject = SubjectTestHelper.SUBJ2;

    this.collisionGroup = new GroupSave(this.grouperSession).assignName("test:wf:collision")
        .assignUuid(this.jdbcSubject.getId()).assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group).save();
  }

  /**
   * an instance whose initiate entry is the group subject (source g:gsa)
   * @return the instance
   */
  private GrouperWorkflowInstance instanceInitiatedByGroupSubject() {
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setAction(GrouperWorkflowConstants.INITIATE_ACTION);
    logEntry.setState(GrouperWorkflowConstants.INITIATE_STATE);
    logEntry.setSubjectId(this.collisionGroup.getId());
    logEntry.setSubjectSourceId(this.collisionGroup.toSubject().getSourceId());
    logEntry.setMillisSince1970(1L);
    GrouperWorkflowInstanceLogEntries logEntries = new GrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(logEntry);

    GrouperWorkflowInstance instance = new GrouperWorkflowInstance();
    instance.setWorkflowInstanceState(GrouperWorkflowConstants.INITIATE_STATE);
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
    return instance;
  }

  /**
   * matches and sameSubject need source and id
   */
  public void testMatches() {

    Subject groupSubject = this.collisionGroup.toSubject();
    assertEquals("setup: same id in two sources", groupSubject.getId(), this.jdbcSubject.getId());
    assertFalse("setup: different sources", groupSubject.getSourceId().equals(this.jdbcSubject.getSourceId()));

    GrouperWorkflowInstanceLogEntry entry = instanceInitiatedByGroupSubject().getGrouperWorkflowInstanceLogEntries().getLogEntries().get(0);

    assertTrue(GrouperWorkflowSubjectIdentity.matches(entry, groupSubject));
    assertFalse(GrouperWorkflowSubjectIdentity.matches(entry, this.jdbcSubject));
    assertFalse(GrouperWorkflowSubjectIdentity.matches(entry, null));
    assertFalse(GrouperWorkflowSubjectIdentity.matches(null, groupSubject));

    // no source recorded: fail closed
    entry.setSubjectSourceId(null);
    assertFalse(GrouperWorkflowSubjectIdentity.matches(entry, groupSubject));

    assertTrue(GrouperWorkflowSubjectIdentity.sameSubject(groupSubject, this.collisionGroup.toSubject()));
    assertFalse(GrouperWorkflowSubjectIdentity.sameSubject(groupSubject, this.jdbcSubject));
    assertFalse(GrouperWorkflowSubjectIdentity.sameSubject(groupSubject, null));
  }

  /**
   * initiator and view checks use the source
   */
  public void testInitiatorAndViewAreSourceBound() {

    GrouperWorkflowInstance instance = instanceInitiatedByGroupSubject();

    // while both exist the source bound lookup finds the right one (an id only lookup is not unique)
    Subject initiator = GrouperWorkflowInstanceService.subjectWhoInitiatedWorkflow(instance);
    assertNotNull(initiator);
    assertEquals(this.collisionGroup.toSubject().getSourceId(), initiator.getSourceId());

    // the jdbc subject with the same id cannot view the group subject's instance
    assertFalse(GrouperWorkflowInstanceService.canInstanceBeViewed(instance, this.jdbcSubject));
    assertTrue(GrouperWorkflowInstanceService.canInstanceBeViewed(instance, this.collisionGroup.toSubject()));

    // the original subject goes away: the same-id jdbc subject must not inherit the instance
    this.collisionGroup.delete();
    SubjectFinder.flushCache();

    assertNull(GrouperWorkflowSubjectIdentity.findInitiator(instance, false));
    assertNull(GrouperWorkflowInstanceService.subjectWhoInitiatedWorkflow(instance));
    assertFalse(GrouperWorkflowInstanceService.canInstanceBeViewed(instance, this.jdbcSubject));
  }

  /**
   * explicit approver is resolved by source and id
   */
  public void testApproverIsSourceBound() {

    GrouperWorkflowApprovalState approvalState = new GrouperWorkflowApprovalState();
    approvalState.setStateName("approver");
    approvalState.setApproverSubjectId(this.jdbcSubject.getId());
    approvalState.setApproverSubjectSourceId(this.jdbcSubject.getSourceId());

    // both subjects exist, no SubjectNotUniqueException, and only the jdbc subject is an approver
    List<Subject> approvers = GrouperWorkflowInstanceService.getApprovers(approvalState);
    assertEquals(1, approvers.size());
    assertEquals(this.jdbcSubject.getSourceId(), approvers.get(0).getSourceId());
    assertEquals(this.jdbcSubject.getId(), approvers.get(0).getId());

    // approver configured as the group subject, which then is deleted: nobody, and no null entry
    approvalState.setApproverSubjectSourceId(this.collisionGroup.toSubject().getSourceId());
    this.collisionGroup.delete();
    SubjectFinder.flushCache();
    approvers = GrouperWorkflowInstanceService.getApprovers(approvalState);
    assertEquals(0, approvers.size());

    // no source id: nobody (fail closed)
    approvalState.setApproverSubjectSourceId(null);
    assertNull(GrouperWorkflowSubjectIdentity.findApprover(approvalState));
  }
}
