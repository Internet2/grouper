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
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
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
   * number of groups in a folder for a provisioner target 
   */
  private long groupsCount;
  
  /**
   * number of users in a folder for a provisioner target
   */
  private long usersCount;
  
  /**
   * number of memberships in a folder for a provisioner target
   */
  private long membershipsCount;
  
  /**
   * gc grouper sync group to show for a particular group and provisioner 
   */
  private GcGrouperSyncGroup gcGrouperSyncGroup;
  
  /**
   * logs for a particular group
   */
  private List<GcGrouperSyncLog> gcGrouperSyncLogs = new ArrayList<GcGrouperSyncLog>();
  
  /**
   * grouper sync members to show on subject provisioning screen
   */
  private List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
  
  /**
   * grouper sync memberships to show on membership provisioning screen
   */
  private List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();
  
  /**
   * gc grouper sync membership to show for a particular membership with provisioner 
   */
  private GcGrouperSyncMembership gcGrouperSyncMembership;

  /**
   * gc grouper sync member to show for a particular member with provisioner 
   */
  private GcGrouperSyncMember gcGrouperSyncMember;
  
  
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

//    Boolean allowedInProvisioningGroup = null;
//    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.provisioning.must.be.in.group"))) {
//      String error = GrouperUiFilter.requireUiGroup("uiV2.provisioning.must.be.in.group", loggedInSubject, false);
//      //null error means allow
//      allowedInProvisioningGroup = ( error == null );
//    }
//
//    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
//    
//    if (guiGroup != null) {
//      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
//        return false;
//      }
//      if (allowedInProvisioningGroup != null) {
//        return allowedInProvisioningGroup;
//      }
//      return true;
//    }
//
//    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
//    
//    if (guiStem != null) {
//      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
//        return false;
//      }
//      if (allowedInProvisioningGroup != null) {
//        return allowedInProvisioningGroup;
//      }
//      return true;
//    }
    
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
    
//    Boolean allowedInProvisioningGroup = null;
//    if (!StringUtils.isBlank(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.provisioning.must.be.in.group"))) {
//      String error = GrouperUiFilter.requireUiGroup("uiV2.provisioning.must.be.in.group", loggedInSubject, false);
//      //null error means allow
//      allowedInProvisioningGroup = ( error == null );
//    }
//
//    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
//    
//    if (guiGroup != null) {
//      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
//        return false;
//      }
//      if (allowedInProvisioningGroup != null) {
//        return allowedInProvisioningGroup;
//      }
//      return true;
//    }
//
//    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
//    
//    if (guiStem != null) {
//      if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
//        return false;
//      }
//      if (allowedInProvisioningGroup != null) {
//        return allowedInProvisioningGroup;
//      }
//      return true;
//    }

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
    return new HashSet<GrouperProvisioningTarget>(GrouperProvisioningSettings.getTargets(true).values());
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
    
    Map<String, GrouperProvisioningTarget> targets = GrouperProvisioningSettings.getTargets(true);
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    Set<GrouperProvisioningTarget> editableTargets = new HashSet<GrouperProvisioningTarget>();
    
    for (GrouperProvisioningTarget target: targets.values()) {
      if (GrouperProvisioningService.isTargetEditable(target, loggedInSubject, grouperObject)) {
        editableTargets.add(target);
      }
    }
    
    return editableTargets;
  }

  /**
   * number of groups in a folder for a provisioner target 
   * @return
   */
  public long getGroupsCount() {
    return groupsCount;
  }

  /**
   * number of groups in a folder for a provisioner target 
   * @param groupsCount
   */
  public void setGroupsCount(long groupsCount) {
    this.groupsCount = groupsCount;
  }

  /**
   * number of users in a folder for a provisioner target 
   * @return
   */
  public long getUsersCount() {
    return usersCount;
  }

  /**
   * number of users in a folder for a provisioner target 
   * @param usersCount
   */
  public void setUsersCount(long usersCount) {
    this.usersCount = usersCount;
  }

  /**
   * number of memberships in a folder for a provisioner target 
   * @return
   */
  public long getMembershipsCount() {
    return membershipsCount;
  }

  /**
   * number of memberships in a folder for a provisioner target 
   * @param membershipsCount
   */
  public void setMembershipsCount(long membershipsCount) {
    this.membershipsCount = membershipsCount;
  }

  /**
   * grouper sync members to show on subject provisioning screen
   * @return
   */
  public List<GcGrouperSyncMember> getGcGrouperSyncMembers() {
    return gcGrouperSyncMembers;
  }

  /**
   * grouper sync members to show on subject provisioning screen
   * @param gcGrouperSyncMembers
   */
  public void setGcGrouperSyncMembers(List<GcGrouperSyncMember> gcGrouperSyncMembers) {
    this.gcGrouperSyncMembers = gcGrouperSyncMembers;
  }

  /**
   * grouper sync memberships to show on membership provisioning screen
   * @return
   */
  public List<GcGrouperSyncMembership> getGcGrouperSyncMemberships() {
    return gcGrouperSyncMemberships;
  }

  /**
   * grouper sync memberships to show on membership provisioning screen
   * @param gcGrouperSyncMemberships
   */
  public void setGcGrouperSyncMemberships(List<GcGrouperSyncMembership> gcGrouperSyncMemberships) {
    this.gcGrouperSyncMemberships = gcGrouperSyncMemberships;
  }

  /**
   * gc grouper sync group to show for a particular group and provisioner 
   * @return
   */
  public GcGrouperSyncGroup getGcGrouperSyncGroup() {
    return gcGrouperSyncGroup;
  }

  /**
   * gc grouper sync group to show for a particular group and provisioner 
   * @param gcGrouperSyncGroup
   */
  public void setGcGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.gcGrouperSyncGroup = gcGrouperSyncGroup;
  }

  /**
   * logs for a particular group
   * @return
   */
  public List<GcGrouperSyncLog> getGcGrouperSyncLogs() {
    return gcGrouperSyncLogs;
  }

  /**
   * logs for a particular group
   * @param gcGrouperSyncLogs
   */
  public void setGcGrouperSyncLogs(List<GcGrouperSyncLog> gcGrouperSyncLogs) {
    this.gcGrouperSyncLogs = gcGrouperSyncLogs;
  }
  
  /**
   * @return gc grouper sync membership to show for a particular membership with provisioner 
   */
  public GcGrouperSyncMembership getGcGrouperSyncMembership() {
    return gcGrouperSyncMembership;
  }

  /**
   * gc grouper sync membership to show for a particular membership with provisioner 
   * @param gcGrouperSyncMembership
   */
  public void setGcGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    this.gcGrouperSyncMembership = gcGrouperSyncMembership;
  }
  
  /**
   * gc grouper sync member to show for a particular member with provisioner 
   * @return
   */
  public GcGrouperSyncMember getGcGrouperSyncMember() {
    return gcGrouperSyncMember;
  }

  /**
   * gc grouper sync member to show for a particular member with provisioner 
   * @param gcGrouperSyncMember
   */
  public void setGcGrouperSyncMember(GcGrouperSyncMember gcGrouperSyncMember) {
    this.gcGrouperSyncMember = gcGrouperSyncMember;
  }




  /**
   * keep track of the paging on the config history screen
   */
  private GuiPaging guiPaging = null;

  
  /**
   * keep track of the paging on the config history screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  
}
