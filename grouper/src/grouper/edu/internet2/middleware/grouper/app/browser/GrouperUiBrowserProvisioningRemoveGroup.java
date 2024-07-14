package edu.internet2.middleware.grouper.app.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * This class is used to programmatically remove a provisioner from a group.
 * <p>
 * Remove provisioner with name "myProvisioner" from a group with name: "test:test"
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserProvisioningRemoveGroup grouperUiBrowserProvisioningRemoveGroup = new GrouperUiBrowserProvisioningRemoveGroup(page).
 *      assignGroupToRemoveName("test:test").assignProvisionerName("myProvisioner").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserProvisioningRemoveGroup extends GrouperUiBrowser {

  public GrouperUiBrowserProvisioningRemoveGroup(GrouperPage grouperPage) {
    super(grouperPage);
  }

  private String groupToRemoveName;

  /**
   * Id Path in UI
   * @param groupToRemoveName
   * @return this object
   */
  public GrouperUiBrowserProvisioningRemoveGroup assignGroupToRemoveName(
      String groupToRemoveName) {

    this.groupToRemoveName = groupToRemoveName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToRemoveName
   * @return this object
   */
  public GrouperUiBrowserProvisioningRemoveGroup assignGroupToRemoveId(
      String groupToRemoveId) {
    Group group = GroupFinder.findByUuid(groupToRemoveId, true);
    this.groupToRemoveName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToRemoveName
   * @return this object
   */
  public GrouperUiBrowserProvisioningRemoveGroup assignGroupToRemove(
      Group group) {
    this.groupToRemoveName = group.getName();
    return this;
  }
  
  
  private String provisionerName;
  
  public GrouperUiBrowserProvisioningRemoveGroup assignProvisionerName(
      String provisionerName) {

    this.provisionerName = provisionerName;
    return this;
  }
  
  
  /**
   * This method first navigates to the group, then clicks the more actions button before clicking the provisioning option. 
   * The the actions button of the correct provisioner is clicked and the "do not provision to" option is clicked. Finally,
   * the method verifies an ajax refresh and a success message. 
   * @return this object
   */
  public GrouperUiBrowserProvisioningRemoveGroup browse() {
    this.navigateToGroup(groupToRemoveName);
    this.getGrouperPage().getPage().locator("#more-action-button").click();
    this.getGrouperPage().getPage().locator("#groupMoreActionsProvisioningButtonId").click();
    this.waitForJspToLoad("provisioningGroupProvisioners");
    this.getGrouperPage().getPage().locator("#actions_" + provisionerName + "_id").click();
    this.getGrouperPage().getPage().locator("#doNotProvisionToGroup_" + provisionerName + "_id").click();
    this.waitForJspToLoad("provisioningGroupProvisioners");
    findMessageInMessages("provisioningEditSaveSuccess", true);
    return this;
  }

}
