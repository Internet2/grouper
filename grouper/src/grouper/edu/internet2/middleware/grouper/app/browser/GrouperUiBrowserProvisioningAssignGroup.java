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
 * This class is used to programmatically assign provisioning of a certain provisioner to a group.
 * <p>
 * Assign provisioning of provisioner with name "myProvisioner" to a group with name: "test:test"
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserProvisioningAssignGroup grouperUiBrowserProvisioningAssignGroup = new GrouperUiBrowserProvisioningAssignGroup(page).
 *      assignGroupToAssignName("test:test").assignProvisionerName("myProvisioner").browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserProvisioningAssignGroup extends GrouperUiBrowser {

  public GrouperUiBrowserProvisioningAssignGroup(GrouperPage grouperPage) {
    super(grouperPage);
  }

  private String groupToAssignName;

  /**
   * Id Path in UI
   * @param groupToAssignName
   * @return this object
   */
  public GrouperUiBrowserProvisioningAssignGroup assignGroupToAssignName(
      String groupToAssignName) {

    this.groupToAssignName = groupToAssignName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToAssignName
   * @return this object
   */
  public GrouperUiBrowserProvisioningAssignGroup assignGroupToAssignId(
      String groupToAssignId) {
    Group group = GroupFinder.findByUuid(groupToAssignId, true);
    this.groupToAssignName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToAssignName
   * @return this object
   */
  public GrouperUiBrowserProvisioningAssignGroup assignGroupToAssign(
      Group group) {
    this.groupToAssignName = group.getName();
    return this;
  }
  

  private String provisionerName;
  
  public GrouperUiBrowserProvisioningAssignGroup assignProvisionerName(
      String provisionerName) {

    this.provisionerName = provisionerName;
    return this;
  }
  
  
  /**
   * This method first navigates to the group, then clicks the more actions button before clicking the provisioning option. 
   * The the actions button of the correct provisioner is clicked and the "provision to" option is clicked. Finally,
   * the method verifies an ajax refresh and a success message. 
   * @return this object
   */
  public GrouperUiBrowserProvisioningAssignGroup browse() {
    this.navigateToGroup(groupToAssignName);
    this.getGrouperPage().getPage().locator("#more-action-button").click();

    this.getGrouperPage().getPage().locator("#groupMoreActionsProvisioningButtonId").click();
    this.waitForJspToLoad("provisioningGroupProvisioners");
    this.getGrouperPage().getPage().locator("#actions_" + provisionerName + "_id").click();

    this.getGrouperPage().getPage().locator("#provisionToGroup_" + provisionerName + "_id").click();
    this.waitForJspToLoad("provisioningGroupProvisioners");
    GrouperUtil.sleep(600);
    findMessageInMessages("provisioningEditSaveSuccess", true);
    return this;
  }

}
