/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
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
    return new ArrayList<String>(GrouperProvisioningSettings.getTargets().keySet());
  }
  
  
  public Set<String> getEditableTargetNames() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    Map<String, GrouperProvisioningTarget> targets = GrouperProvisioningSettings.getTargets();
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Set<String> editableTargetNames = new HashSet<String>();
    
    for (Entry<String, GrouperProvisioningTarget> entry: targets.entrySet()) {
      
      String groupAllowedToAssign = entry.getValue().getGroupAllowedToAssign();
      boolean allowAssignOnOneStem = entry.getValue().isAllowAssignmentsOnlyOnOneStem();
      boolean readOnly = entry.getValue().isReadOnly();
      
      if(readOnly) {
        continue;
      }
      
      
      if (guiGroup == null) { //we are working with a stem
        
        if (allowAssignOnOneStem) { //can not edit if this target is already assigned to another stem
          
          List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
              .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true")
              .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_TARGET).addAttributeValuesOnAssignment2(entry.getKey())
              .findStems());
          
          if (stems.size() > 0) {
            continue;
          }
          
        }
        
      } else { //we are working with a group
        
        if (allowAssignOnOneStem) {
          continue;
        }
        
      }
      
      if (StringUtils.isBlank(groupAllowedToAssign)) {
        
        if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
          editableTargetNames.add(entry.getKey());
        }
      } else {
        
        Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupAllowedToAssign, false);
        if (group == null) {
          try { // try looking up group by id
            Long groupId = Long.valueOf(groupAllowedToAssign);
            group = GroupFinder.findByIdIndexSecure(groupId, false, new QueryOptions());
            if (group == null) {
              throw new RuntimeException(groupAllowedToAssign+" is not a valid group id or group name");
            }
          } catch (Exception e) {
            throw new RuntimeException(groupAllowedToAssign+" is not a valid group id or group name");
          }
         
        }
        
        for (Member member: group.getMembers()) {
          Subject groupSubject = member.getSubject();
          if (loggedInSubject.getId().equals(groupSubject.getId())) {
            editableTargetNames.add(entry.getKey());
          }
        }
      }
      
    }
    
    return editableTargetNames;
  }

}
