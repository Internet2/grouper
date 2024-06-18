package edu.internet2.middleware.grouper.app.browser;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class is used to programmatically create a group. It uses the 'Create new group' button on the homescreen in the upper left.
 * This will end on the group main screen.
 * <p>
 * Create a group
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserGroupCreate grouperUiBrowserGroupCreate = 
 *    new GrouperUiBrowserGroupCreate(page).assignStemName("test").assignGroupDisplayExtension("test24")
 *    .assignGroupExtension("test24id").assignDescription("Test group").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserGroupCreate
    extends GrouperUiBrowser {

  public GrouperUiBrowserGroupCreate(GrouperPage grouperPage) {
    super(grouperPage);
  }

  /**
   * Folder name
   */
  private String stemName;

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
   * Folder name
   */
  public String getStemName() {
    return stemName;
  }

  /**
   * Folder name
   */
  public GrouperUiBrowserGroupCreate assignStemName(String stemName) {
    this.stemName = stemName;
    return this;
  }
  
  /**
   * Folder Uuid
   */
  public GrouperUiBrowserGroupCreate assignStemUuid(String stemUuid) {
    this.stemName = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemUuid, true).getName();
    return this;
  }
  
  /**
   * Folder object
   */
  public GrouperUiBrowserGroupCreate assignStem(Stem stem) {
    this.stemName = stem.getName();
    return this;
  }

  /**
   * Name in UI, Name is the label that identifies this group, and might change.
   */
  public String getGroupDisplayExtension() {
    return groupDisplayExtension;
  }

  /**
   * Name in UI, Name is the label that identifies this group, and might change.
   */
  public GrouperUiBrowserGroupCreate assignGroupDisplayExtension(
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
  public GrouperUiBrowserGroupCreate assignGroupExtension(String groupExtension) {
    this.groupExtension = groupExtension;
    return this;
  }

  /**
   * Description contains notes about the group, which could include: what the group represents, why it was created, etc.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Description contains notes about the group, which could include: what the group represents, why it was created, etc.
   */
  public GrouperUiBrowserGroupCreate assignDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Method used to programmatically create a new group
   */
  public GrouperUiBrowserGroupCreate browse() {
    this.navigateToGrouperHome();
    this.getGrouperPage().getPage().locator("#homepageCreateGroupButton").click();
    this.waitForJspToLoad("newGroup");
    this.getGrouperPage().getPage().locator("#parentFolderComboId").fill(stemName);
    this.getGrouperPage().getPage().locator("#groupName").fill(groupDisplayExtension);
    if (!StringUtils.equals(groupDisplayExtension, groupExtension)) {
      this.getGrouperPage().getPage().locator("#nameDifferentThanIdId").check();
      
      // Waiting for the javascript to finish
      GrouperUtil.sleep(5);
      this.getGrouperPage().getPage().locator("#groupId").fill(groupExtension);
    }
    this.getGrouperPage().getPage().locator("#groupDescription").fill(description);
    this.getGrouperPage().getPage().locator("#newgroupsavebutton").click();
    
    // Null because both the same page (if it is a failure) or a new group page (if it is a success) could load.
    // We are waiting for anything to finish
    this.waitForJspToLoad(null);
    this.findMessageInMessages("groupCreateSuccess", true);
    return this;
  }
  

  
}