package edu.internet2.middleware.grouper.app.browser;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class is used to programmatically edit a group. It navigates to the group, then clicks group actions and selects edit group.
 * If group editing fields (groupDisplayExtension, groupExtension, or description) have been filled, they will be entered. Before
 * confirming, it is made sure that the alternate Id path of the group is not updated. Finally, the group edit confirmation
 * message is received.
 * <p>
 * Edit a group
 * <blockquote> 
 * <pre>
 *   GrouperUiBrowserGroupEdit grouperUiBrowserGroupEdit = new GrouperUiBrowserGroupEdit(grouperPage).
 *     assignGroupToEditName("test:test22").assignGroupExtension("testeditedagain").assignDescription("this is the edited description").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserGroupEdit
    extends GrouperUiBrowser {

  public GrouperUiBrowserGroupEdit(GrouperPage grouperPage) {
    super(grouperPage);
  }


  /**
   * Name in UI, Name is the label that identifies this group, and might change.
   */
  private String groupDisplayExtension;

  /**
   * Id in UI, Id is the unique identifier for this group.
   */
  private String groupExtension;

  /**
   * Description contains notes about the group, which could include: what the group represents, why it was created, etc.
   */
  private String description;
  

  /**
   * Name in UI, Name is the label that identifies this group, and might change.
   */
  public String getGroupDisplayExtension() {
    return groupDisplayExtension;
  }

  /**
   * Name in UI, Name is the label that identifies this group, and might change.
   */
  public GrouperUiBrowserGroupEdit assignGroupDisplayExtension(
      String groupDisplayExtension) {
    this.groupDisplayExtension = groupDisplayExtension;
    return this;
  }

  /**
   * Id in UI, Id is the unique identifier for this group.
   */
  public String getGroupExtension() {
    return groupExtension;
  }

  /**
   * Id in UI, Id is the unique identifier for this group.
   */
  public GrouperUiBrowserGroupEdit assignGroupExtension(String groupExtension) {
    this.groupExtension = groupExtension;
    return this;
  }

  /**
   * Description contains notes about the group, which could include: what the group represents, why it was created, etc.
   */
  public String getDescription() {
    return description;
  }
  
  private String groupToEditName;

  /**
   * Id Path in UI
   * @param groupToEditName
   * @return this object
   */
  public GrouperUiBrowserGroupEdit assignGroupToEditName(
      String groupToEditName) {

    this.groupToEditName = groupToEditName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToEditName
   * @return this object
   */
  public GrouperUiBrowserGroupEdit assignGroupToEditId(
      String groupToEditId) {
    Group group = GroupFinder.findByUuid(groupToEditId, true);
    this.groupToEditName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToEditName
   * @return this object
   */
  public GrouperUiBrowserGroupEdit assignGroupToEdit(
      Group group) {
    this.groupToEditName = group.getName();
    return this;
  }
  
  /**
   * Description contains notes about the group, which could include: what the group represents, why it was created, etc.
   */
  public GrouperUiBrowserGroupEdit assignDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   *  Method used to programmatically edit a group
   */
  public GrouperUiBrowserGroupEdit browse() {
    this.navigateToGroup(groupToEditName);
    this.getGrouperPage().getPage().locator("#more-action-button").click();
    // No ajax here so must sleep
    GrouperUtil.sleep(100);
    this.getGrouperPage().getPage().locator("#groupActionsEditGroupButton").click();
    this.waitForJspToLoad("groupEdit");
    if (groupDisplayExtension != null) {
      this.getGrouperPage().getPage().locator("#groupName").fill(groupDisplayExtension);
    }
    if (groupExtension != null) {
      this.getGrouperPage().getPage().locator("#groupId").fill(groupExtension);
    }
    if (description != null) {
      this.getGrouperPage().getPage().locator("#groupDescription").fill(description);
    }
    // See if alternate Id path is suggested to update
    GrouperUtil.sleep(1000);
    if (this.getGrouperPage().getPage().locator("#groupRenameUpdateAlternateName")
        .isChecked() && this.getGrouperPage().getPage().locator("#groupRenameUpdateAlternateName")
        .isVisible()) {
      GrouperUtil.sleep(1000);
      this.getGrouperPage().getPage().locator("#groupRenameUpdateAlternateName").uncheck();
    }
    this.getGrouperPage().getPage().locator("#editGroupSaveButton").click();
    this.waitForJspToLoad("viewGroup");
    this.findMessageInMessages("groupEditSuccess", true);
    return this;
  }

}