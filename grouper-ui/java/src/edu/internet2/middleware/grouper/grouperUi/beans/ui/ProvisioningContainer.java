/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
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
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }

    Boolean allowedInProvisioningGroup = null;
    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.provisioning.must.be.in.group"))) {
      String error = GrouperUiFilter.requireUiGroup("uiV2.provisioning.must.be.in.group", loggedInSubject, false);
      //null error means allow
      allowedInProvisioningGroup = ( error == null );
    }

    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        return false;
      }
      if (allowedInProvisioningGroup != null) {
        return allowedInProvisioningGroup;
      }
      return true;
    }

    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return false;
      }
      if (allowedInProvisioningGroup != null) {
        return allowedInProvisioningGroup;
      }
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   * @return true if can write
   */
  public boolean isCanWriteProvisioning() {

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    Boolean allowedInProvisioningGroup = null;
    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.provisioning.must.be.in.group"))) {
      String error = GrouperUiFilter.requireUiGroup("uiV2.provisioning.must.be.in.group", loggedInSubject, false);
      //null error means allow
      allowedInProvisioningGroup = ( error == null );
    }

    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
        return false;
      }
      if (allowedInProvisioningGroup != null) {
        return allowedInProvisioningGroup;
      }
      return true;
    }

    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return false;
      }
      if (allowedInProvisioningGroup != null) {
        return allowedInProvisioningGroup;
      }
      return true;
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
   * @return all targets
   */
  public Set<GrouperProvisioningTarget> getTargets() {
    return new HashSet<GrouperProvisioningTarget>(GrouperProvisioningSettings.getTargets().values());
  }
  
  /**
   * get editable targets for current group/stem and logged in subject
   * @return
   */
  public Set<GrouperProvisioningTarget> getEditableTargets() {
    
    GrouperObject grouperObject = null;
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiGroup != null) {
      grouperObject = guiGroup.getGrouperObject();
    }
    if (guiStem != null) {
      grouperObject = guiStem.getGrouperObject();
    }
    
    Map<String, GrouperProvisioningTarget> targets = GrouperProvisioningSettings.getTargets();
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Set<GrouperProvisioningTarget> editableTargets = new HashSet<GrouperProvisioningTarget>();
    
    for (GrouperProvisioningTarget target: targets.values()) {
      if (GrouperProvisioningService.isTargetEditable(target, loggedInSubject, grouperObject)) {
        editableTargets.add(target);
      }
    }
    
    return editableTargets;
  }

}
