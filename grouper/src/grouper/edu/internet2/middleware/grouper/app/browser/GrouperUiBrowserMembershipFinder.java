package edu.internet2.middleware.grouper.app.browser;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Class used to programmatically find a membership. It navigates to the group, then pages through the entity list until finding the
 * checkbox with the custom attribute matching the subjects combo id. Then it sets the membershipFound boolean to true and breaks
 * out of the paging loop.
 * <p>
 * Find a membership by subject object
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserMembershipAdd grouperUiBrowserMembershipAdd = new GrouperUiBrowserMembershipAdd(page).
 *      assignGroupToAddToName("test:test").assignSubjectIdentifier("test.subject.1").browse();
 * </pre>
 * </blockquote>
 * </p>
 */



public class GrouperUiBrowserMembershipFinder extends GrouperUiBrowser {

  private Subject subject;

  public GrouperUiBrowserMembershipFinder assignSubject(Subject subject1) {
    this.subject = subject1;
    return this;
  }

  public GrouperUiBrowserMembershipFinder assignSubjectId(String subjectId) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findById(subjectId, true);
    return this;
  }

  public GrouperUiBrowserMembershipFinder assignSubjectIdentifier(
      String subjectIdentifier) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findByIdentifier(subjectIdentifier, true);
    return this;
  }

  public GrouperUiBrowserMembershipFinder assignSubjectIdAndSourceId(String subjectId,
      String sourceId) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, true);
    return this;
  }

  public GrouperUiBrowserMembershipFinder assignSubjectIdentifierAndSourceId(
      String subjectIdentifier, String sourceId) {
    GrouperSession.startRootSession();
    this.subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId,
        true);
    return this;
  }

  private String groupToLookInName;

  /**
   * Id Path in UI
   * @param groupToLookInName
   * @return this object
   */
  public GrouperUiBrowserMembershipFinder assignGroupToLookInName(
      String groupToLookInName) {

    this.groupToLookInName = groupToLookInName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToLookInName
   * @return this object
   */
  public GrouperUiBrowserMembershipFinder assignGroupToLookInId(
      String groupToLookInId) {
    Group group = GroupFinder.findByUuid(groupToLookInId, true);
    this.groupToLookInName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToLookInName
   * @return this object
   */
  public GrouperUiBrowserMembershipFinder assignGroupToLookIn(
      Group group) {
    this.groupToLookInName = group.getName();
    return this;
  }

  public GrouperUiBrowserMembershipFinder(GrouperPage grouperPage) {
    super(grouperPage);
  }

  public GrouperUiBrowserMembershipFinder browse() {
    this.navigateToGroup(groupToLookInName);
    this.getGrouperPage().getPage().locator("#table-filter").fill(subject.getId());
    this.getGrouperPage().getPage().locator("#filterSubmitId").click();
    this.waitForJspToLoad("groupContents");
    // Looping through the pages 1000 times, breaking when the desired group is found
    int timeToLive = 1000;
    OUTER: while (true) {
      GrouperUtil.assertion(timeToLive-- > 0, "Endless loop while paging");
      for (Locator locator : this.getGrouperPage().getPage()
          .locator("#membersToDeleteFormId")
          .locator("[data-gr-member-checkbox]").all()) {
        if (StringUtils.equals(locator.getAttribute("data-gr-member-checkbox"),
            subject.getSourceId() + "||" + subject.getId())) {
          break OUTER;
        }
      }

      // See if there is a next page link
      if (this.getGrouperPage().getPage().locator("#groupFilterResultsId")
          .locator("#pagingNextLink").all().isEmpty()) {

        // No next link means the last page has been reached
        throw new RuntimeException("Membership not found: '" + subject.getName() + "'");
      } else {
        this.getGrouperPage().getPage().locator("#groupFilterResultsId")
            .locator("#pagingNextLink").click();
        this.waitForJspToLoad("groupContents");
      }

    }
    return this;
  }

}
