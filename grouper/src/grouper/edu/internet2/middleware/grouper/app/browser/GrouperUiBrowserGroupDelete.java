package edu.internet2.middleware.grouper.app.browser;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class is used to programmatically delete a group.
 * <p>
 * Delete group with name "test:test"
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserGroupDelete grouperUiBrowserGroupDelete = new GrouperUiBrowserGroupDelete(page).assignGroupToDeleteName("test:test").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserGroupDelete
    extends GrouperUiBrowser {

  public GrouperUiBrowserGroupDelete(GrouperPage grouperPage) {
    super(grouperPage);
  }

  private String groupToDeleteName;

  /**
   * Id Path in UI
   * @param groupToDeleteName
   * @return this object
   */
  public GrouperUiBrowserGroupDelete assignGroupToDeleteName(
      String groupToDeleteName) {

    this.groupToDeleteName = groupToDeleteName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToDeleteName
   * @return this object
   */
  public GrouperUiBrowserGroupDelete assignGroupToDeleteId(
      String groupToDeleteId) {
    Group group = GroupFinder.findByUuid(groupToDeleteId, true);
    this.groupToDeleteName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToDeleteName
   * @return this object
   */
  public GrouperUiBrowserGroupDelete assignGroupToDelete(
      Group group) {
    this.groupToDeleteName = group.getName();
    return this;
  }

  /**
   * Method used to programmatically delete a group. It navigates to the group, then goes through the process of deleting
   * it through the more actions button. At the end, it verifies an ajax refresh and a success message.
   */
  public GrouperUiBrowserGroupDelete browse() {
    this.navigateToGroup(groupToDeleteName); 
    this.getGrouperPage().getPage().locator("#more-action-button").click();
    GrouperUtil.sleep(500);
    this.getGrouperPage().getPage().locator("#groupActionsGroupDeleteButton").click();
    this.waitForJspToLoad("groupDelete");
    this.getGrouperPage().getPage().locator("#groupDeleteConfirmButton").click();
    this.waitForJspToLoad(null);
    this.findMessageInMessages("groupDeleteSuccess", true);
    return this;
  }

}