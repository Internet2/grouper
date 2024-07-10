package edu.internet2.middleware.grouper.app.browser;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * This class is used to programmatically remove a subject from a group
 * <p>
 * Remove subject with subjectId: "test.subject.1" to group with name: "test:test"
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserMembershipRemove grouperUiBrowserMembershipRemove = new GrouperUiBrowserMembershipRemove(page).
 *      assignGroupToRemoveFromName("test:test").assignSubjectId("test.subject.1").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserMembershipRemove extends GrouperUiBrowser {

    private Subject subject;
    
    public GrouperUiBrowserMembershipRemove assignSubject(Subject subject1) {
      this.subject = subject1;
      return this;
    }
    
    public GrouperUiBrowserMembershipRemove assignSubjectId(String subjectId) {
      GrouperSession.startRootSession();
      this.subject = SubjectFinder.findById(subjectId, true);
      return this;
    }
    
    public GrouperUiBrowserMembershipRemove assignSubjectIdentifier(String subjectIdentifier) {
      GrouperSession.startRootSession();
      this.subject = SubjectFinder.findByIdentifier(subjectIdentifier, true);
      return this;
    }
    
    public GrouperUiBrowserMembershipRemove assignSubjectIdAndSourceId(String subjectId, String sourceId) {
      GrouperSession.startRootSession();
      this.subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
      return this;
    }
    
    public GrouperUiBrowserMembershipRemove assignSubjectIdentifierAndSourceId(String subjectIdentifier, String sourceId) {
      GrouperSession.startRootSession();
      this.subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, true);
      return this;    
    }
    
    private String groupToRemoveFromName;
    
    /**
     * Id Path in UI
     * @param groupToRemoveFromName
     * @return this object
     */
    public GrouperUiBrowserMembershipRemove assignGroupToRemoveFromName(
        String groupToRemoveFromName) {

      this.groupToRemoveFromName = groupToRemoveFromName;
      return this;
    }

    /**
     * Uuid of the group
     * @param groupToRemoveFromName
     * @return this object
     */
    public GrouperUiBrowserMembershipRemove assignGroupToRemoveFromId(
        String groupToRemoveFromId) {
      Group group = GroupFinder.findByUuid(groupToRemoveFromId, true);
      this.groupToRemoveFromName = group.getName();
      return this;
    }

    /**
     * Pass in a group object
     * @param groupToRemoveFromName
     * @return this object
     */
    public GrouperUiBrowserMembershipRemove assignGroupToRemoveFrom(
        Group group) {
      this.groupToRemoveFromName = group.getName();
      return this;
    }

    
    public GrouperUiBrowserMembershipRemove(GrouperPage grouperPage) {
      super(grouperPage);
    }
    
    /**
     * This method navigates to the correct group, then clicks on the checkbox with the custom attribute matching the 
     * desired subject's combo box. It clicks "remove slected members" and verifies an ajax refresh and a success message.
     * @return
     */
    public GrouperUiBrowserMembershipRemove browse()  {
      this.findMembership(groupToRemoveFromName, subject);
      this.getGrouperPage().getPage().locator("#membersToDeleteFormId [data-gr-member-checkbox=\"" + subject.getSourceId() + "||" + subject.getId() + "\"]").check();
      this.getGrouperPage().getPage().locator("#groupRemoveSelectedMembersButton").click();
      this.waitForJspToLoad("groupContents");
      this.findLiteralTextInMessages("Success: removed 1 members", true);
      return this;
  }
  
}
