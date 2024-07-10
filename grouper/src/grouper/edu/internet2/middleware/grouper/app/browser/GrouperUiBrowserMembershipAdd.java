package edu.internet2.middleware.grouper.app.browser;

import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * This class is used to programmatically add a subject to a group
 * <p>
 * Add subject with subjectId: "test.subject.1" to group with name: "test:test"
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserMembershipAdd grouperUiBrowserMembershipAdd = new GrouperUiBrowserMembershipAdd(page).
 *      assignGroupToAddToName("test:test").assignSubjectId("test.subject.1").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserMembershipAdd extends GrouperUiBrowser {
  
  /**
   * Subject to be added
   */
  private Subject subject;

  
  /**
   * Pass in a subject object
   * @param subject1
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignSubject(Subject subject1) {
    this.subject = subject1;
    return this;
  }

  /**
   * Assign subjectId of the subject to be added
   * @param subject1
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignSubjectId(String subjectId) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findById(subjectId, true);
    return this;
  }

  /**
   * Assign subjectIdentifier of the subject to be added
   * @param subject1
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignSubjectIdentifier(String subjectIdentifier) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findByIdentifier(subjectIdentifier, true);
    return this;
  }

  /**
   * Assign subjectId and sourceId of the subject to be added
   * @param subject1
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignSubjectIdAndSourceId(String subjectId,
      String sourceId) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
    return this;
  }

  /**
   * Assign subjectIdentifier and sourceId of the subject to be added
   * @param subject1
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignSubjectIdentifierAndSourceId(
      String subjectIdentifier, String sourceId) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId,
        true);
    return this;
  }

  /**
   * Name of the group to add to
   */
  private String groupToAddToName;

  /**
   * Id Path in UI
   * @param groupToAddToName
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignGroupToAddToName(
      String groupToAddToName) {
    GrouperSession.startRootSession();
    this.groupToAddToName = groupToAddToName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToAddToName
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignGroupToAddToId(
      String groupToAddToId) {
    Group group = GroupFinder.findByUuid(groupToAddToId, true);
    this.groupToAddToName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToAddToName
   * @return this object
   */
  public GrouperUiBrowserMembershipAdd assignGroupToAddTo(
      Group group) {
    this.groupToAddToName = group.getName();
    return this;
  }

  public GrouperUiBrowserMembershipAdd(GrouperPage grouperPage) {
    super(grouperPage);
  }

  /**
   * Method used to programmatically add a membership to a group. It types in the subject's
   * combo value and presses enter, then adds the member and verifies a success message and an ajax refresh.
   * @return
   */
  public GrouperUiBrowserMembershipAdd browse() {
    this.navigateToGroup(groupToAddToName);
    this.getGrouperPage().getPage().locator("#show-add-block").click();
    // No ajax refresh so must sleep
    GrouperUtil.sleep(300);
    
    this.getGrouperPage().getPage().locator("#groupAddMemberComboId")
        .fill(subject.getSourceId() + "||" + subject.getId());
    this.getGrouperPage().getPage().keyboard().press("Enter");
    this.getGrouperPage().getPage().locator("#add-members-submit").click();
    GrouperUtil.sleep(1000);
    this.findMessageInMessages("groupAddMemberMadeChangesSuccess", true);
    return this;
  }

}
