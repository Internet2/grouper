/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class ProvisioningContainer {
  
  /**
   * target name user is currently working on
   */
  private String targetName;
  
  /**
   * attribute value for given group/stem and type
   */
  private GrouperProvisioningAttributeValue grouperProvisioningAttributeValue;
  
  /**
   * list of all grouper provisioning attribute values for a given group/stem
   */
  private List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues = new ArrayList<GuiGrouperProvisioningAttributeValue>();

  
  /**
   * @return target name user is currently working on
   */
  public String getTargetName() {
    return targetName;
  }

  /**
   * target name user is currently working on
   * @param targetName
   */
  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }
  
  
  /**
   * @return attribute value for given group/stem and type
   */
  public GrouperProvisioningAttributeValue getGrouperProvisioningAttributeValue() {
    return grouperProvisioningAttributeValue;
  }

  /**
   * attribute value for given group/stem and type
   * @param grouperProvisioningAttributeValue
   */
  public void setGrouperProvisioningAttributeValue(GrouperProvisioningAttributeValue grouperProvisioningAttributeValue) {
    this.grouperProvisioningAttributeValue = grouperProvisioningAttributeValue;
  }

  /**
   * @return list of all grouper provisioning attribute values for a given group/stem
   */
  public List<GuiGrouperProvisioningAttributeValue> getGuiGrouperProvisioningAttributeValues() {
    return guiGrouperProvisioningAttributeValues;
  }


  /**
   * list of all grouper provisioning attribute values for a given group/stem
   * @param guiGrouperProvisioningAttributeValues
   */
  public void setGuiGrouperProvisioningAttributeValues(List<GuiGrouperProvisioningAttributeValue> guiGrouperProvisioningAttributeValues) {
    this.guiGrouperProvisioningAttributeValues = guiGrouperProvisioningAttributeValues;
  }

  /**
   * 
   * @return true if can read
   */
  public boolean isCanReadProvisioning() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * 
   * @return true if can write
   */
  public boolean isCanWriteProvisioning() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }

    return false;
  }
  
  /**
   * if this object or any parent has provisioning settings configured
   * @return if there is type
   */
  public boolean isHasProvisioningOnThisObjectOrParent() {
    
    for (GuiGrouperProvisioningAttributeValue attributeValue: guiGrouperProvisioningAttributeValues) {
      if (attributeValue.getGrouperProvisioningAttributeValue().isDirectAssignment() || StringUtils.isNotBlank(attributeValue.getGrouperProvisioningAttributeValue().getOwnerStemId())) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * 
   * @return true if can run daemon
   */
  public boolean isCanRunDaemon() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    return false;
  }
  
  /**
   * if provisioning in ui is enabled in the config
   * @return true if enabled
   */
  public boolean isProvisioningEnabled() {
    return GrouperProvisioningSettings.provisioningInUiEnabled();
  }
  
  /**
   * @return target names
   */
  public List<String> getTargetNames() {
    return GrouperProvisioningSettings.getTargetNames();
  }

}
