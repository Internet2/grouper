package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * group in target system
 * @author mchyzer
 *
 */
public class ProvisioningGroup extends ProvisioningUpdatable {
  
  public ProvisioningGroup() {
    super();
  }

  public boolean isLoggableHelper() {
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseGroupNames().contains(this.getName())) {
      return true;
    }
    return false;
  }
  
  public boolean isLoggable() {
    ProvisioningGroupWrapper provisioningGroupWrapper = this.getProvisioningGroupWrapper();
    if (provisioningGroupWrapper != null) {
      return provisioningGroupWrapper.getProvisioningStateGroup().isLoggable();
    }
    return isLoggableHelper();
  }
    
  public static void main(String[] args) {
    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    provisioningGroup.assignAttributeValue("name", "someName");
    provisioningGroup.assignAttributeValue("id", "abc123");
    provisioningGroup.addAttributeValue("member", "jsmith");
    provisioningGroup.addAttributeValue("member", "ajackson");
    provisioningGroup.addAttributeValue("member", "tjohnson");
    provisioningGroup.addAttributeValue("objectClass", "groupOfNames");
    provisioningGroup.addAttributeValue("objectClass", "top");
    provisioningGroup.addAttributeValue("objectClass", "memberGroup");
    provisioningGroup.assignAttributeValue("description", "This is the description of the group");
    provisioningGroup.assignAttributeValue("displayName", "Some name");
    provisioningGroup.assignAttributeValue("uuid", "abc123xyz456");
    String json = provisioningGroup._internalal_toJsonForCache("member");
    System.out.println(json);    
  }
  
  private ProvisioningGroupWrapper provisioningGroupWrapper;
  
  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @return id
   */
  public String getId() {
    return this.retrieveAttributeValueString("id");
  }

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @param id1
   */
  public void setId(String id1) {
    this.assignAttributeValue("id", id1);
  }

  /**
   * 
   * @return
   */
  public String getExtension() {
    return GrouperUtil.extensionFromName(this.getName());
  }
  
  /**
   * name of group in target system.  could be group system name, extension, or other
   * @return name
   */
  public String getName() {
    return this.retrieveAttributeValueString("name");
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @param name1
   */
  public void setName(String name1) {
    this.assignAttributeValue("name", name1);
  }

  /**
   * id index in target (optional)
   * @return id index
   */
  public Long getIdIndex() {
    return this.retrieveAttributeValueLong("idIndex");
  }

  /**
   * id index in target (optional)
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.assignAttributeValue("idIndex", idIndex1);
  }

  /**
   * display name (optional)
   * @return display name
   */
  public String getDisplayName() {
    return this.retrieveAttributeValueString("displayName");
  }

  /**
   * 
   * @return
   */
  public String getDisplayExtension() {
    return GrouperUtil.extensionFromName(this.getDisplayName());
  }

  /**
   * display name (optional)
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.assignAttributeValue("displayName", displayName1);
  }

  public ProvisioningGroupWrapper getProvisioningGroupWrapper() {
    return provisioningGroupWrapper;
  }


  public void setProvisioningGroupWrapper(ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrapper = provisioningGroupWrapper;
  }

  public String toString() {
    StringBuilder result = new StringBuilder("Group(");
    boolean firstField = true;
    firstField = this.toStringProvisioningUpdatable(result, firstField);
    
    if (this.provisioningGroupWrapper != null) {
      if (this == this.provisioningGroupWrapper.getGrouperProvisioningGroup() || this == this.provisioningGroupWrapper.getGrouperTargetGroup()) {
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().getGrouperIncrementalDataAction() != null) {
          firstField = toStringAppendField(result, firstField, "action", this.provisioningGroupWrapper.getProvisioningStateGroup().getGrouperIncrementalDataAction());
        }
        firstField = toStringAppendField(result, firstField, "recalcObject", this.provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        firstField = toStringAppendField(result, firstField, "recalcMships", this.provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isCreate()) {
          firstField = toStringAppendField(result, firstField, "create", this.provisioningGroupWrapper.getProvisioningStateGroup().isCreate());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isInsertResultProcessed()) {
          firstField = toStringAppendField(result, firstField, "createProcessed", this.provisioningGroupWrapper.getProvisioningStateGroup().isInsertResultProcessed());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
          firstField = toStringAppendField(result, firstField, "delete", this.provisioningGroupWrapper.getProvisioningStateGroup().isDelete());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isDeleteResultProcessed()) {
          firstField = toStringAppendField(result, firstField, "deleteProcessed", this.provisioningGroupWrapper.getProvisioningStateGroup().isDeleteResultProcessed());
        }
        if (this.provisioningGroupWrapper.getErrorCode() != null) {
          firstField = toStringAppendField(result, firstField, "errorCode", this.provisioningGroupWrapper.getErrorCode().name());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970() != null) {
          firstField = toStringAppendField(result, firstField, "millis1970", this.provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships()) {
          firstField = toStringAppendField(result, firstField, "selectSomeMemberships", this.provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMemberships()) {
          firstField = toStringAppendField(result, firstField, "selectAllMemberships", this.provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMemberships());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
          firstField = toStringAppendField(result, firstField, "selectProcessed", this.provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isUpdate()) {
          firstField = toStringAppendField(result, firstField, "update", this.provisioningGroupWrapper.getProvisioningStateGroup().isUpdate());
        }
        if (this.provisioningGroupWrapper.getProvisioningStateGroup().isUpdateResultProcessed()) {
          firstField = toStringAppendField(result, firstField, "updateProcessed", this.provisioningGroupWrapper.getProvisioningStateGroup().isUpdateResultProcessed());
        }
      }
    }
    
    return result.append(")").toString();
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public ProvisioningGroup clone() {

    ProvisioningGroup provisioningGroup = new ProvisioningGroup();

    this.cloneUpdatable(provisioningGroup, null);
    provisioningGroup.provisioningGroupWrapper = this.provisioningGroupWrapper;

    return provisioningGroup;
  }

  /**
   * do a deep clone of the data, without memberships
   * @param provisioningUpdatables
   * @return the cloned list
   */
  public static List<ProvisioningGroup> cloneWithoutMemberships(List<ProvisioningGroup> provisioningGroups) {
    if (provisioningGroups == null) {
      return null;
    }
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroup : provisioningGroups) {
      ProvisioningGroup provisioningUpdatableClone = (ProvisioningGroup)provisioningGroup.cloneWithoutMemberships();
      result.add(provisioningUpdatableClone);
    }
    return result;
  }

  /**
   * do a deep clone of the data, but add as many objects as there are objects and membership attribute values, one per wrapper
   * @param provisioningUpdatables
   * @return the cloned list
   */
  public static List<ProvisioningGroup> cloneWithOneMembership(List<ProvisioningGroup> provisioningGroups) {
    if (provisioningGroups == null) {
      return null;
    }
    
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    
    List<ProvisioningGroup> result = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroup : provisioningGroups) {
      
      String membershipAttribute = null;
      if (grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
        membershipAttribute = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
      }
      
      ProvisioningAttribute provisioningAttribute = provisioningGroup.getAttributes().get(membershipAttribute);
      if (provisioningAttribute == null) {
        continue;
      }
      
      for (Object value : GrouperUtil.nonNull(provisioningGroup.retrieveAttributeValueSetForMemberships())) {
        ProvisioningGroup provisioningUpdatableClone = (ProvisioningGroup)provisioningGroup.cloneWithoutMemberships();

        ProvisioningMembershipWrapper provisioningMembershipWrapper = GrouperUtil.nonNull(provisioningAttribute.getValueToProvisioningMembershipWrapper()).get(value);

        provisioningUpdatableClone.addAttributeValueForMembership(value, provisioningMembershipWrapper, false);
        
        result.add(provisioningUpdatableClone);
      }
      
    }
    return result;
  }


  /**
   * deep clone the fields in this object without the membership attribute
   */
  public ProvisioningGroup cloneWithoutMemberships() {

    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    String membershipAttributeToIgnore = null;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      membershipAttributeToIgnore = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    }
    this.cloneUpdatable(provisioningGroup, membershipAttributeToIgnore);
    provisioningGroup.provisioningGroupWrapper = this.provisioningGroupWrapper;

    return provisioningGroup;
  }

  @Override
  public boolean canInsertAttribute(String name) {

    GrouperProvisioner grouperProvisioner = this.getProvisioningGroupWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      return retrieveGrouperProvisioningBehavior.isInsertMemberships();
    }

    return retrieveGrouperProvisioningBehavior.canInsertGroupAttribute(name);
  }

  @Override
  public boolean canUpdateAttribute(String name) {
    GrouperProvisioner grouperProvisioner = this.getProvisioningGroupWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      return retrieveGrouperProvisioningBehavior.isUpdateMemberships();
    }
    return this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateGroupAttribute(name);
  }

  @Override
  public boolean canDeleteAttribute(String name) {

    GrouperProvisioner grouperProvisioner = this.getProvisioningGroupWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      return retrieveGrouperProvisioningBehavior.isDeleteMemberships();
    }
    // theres no delete
    return this.canUpdateAttribute(name);
  }

  @Override
  public boolean canDeleteAttributeValue(String name, Object deleteValue) {

    GrouperProvisioner grouperProvisioner = this.getProvisioningGroupWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      
      if (retrieveGrouperProvisioningBehavior.isDeleteMembershipsIfNotExistInGrouper()) {
        return true;
      }
      
      ProvisioningAttribute provisioningAttribute = this.getAttributes().get(name);
      if (provisioningAttribute == null) {
        return false;
      }

      Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper();
      
      if (valueToProvisioningMembershipWrapper == null) {
        return false;
      }

      ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(deleteValue);
      if (provisioningMembershipWrapper == null) {
        return false;
      }
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      if (gcGrouperSyncMembership == null) {
        return false;
      }
      if (retrieveGrouperProvisioningBehavior.isDeleteMembershipsIfGrouperDeleted()) {
        return true;
      }
      if (gcGrouperSyncMembership.isInTargetInsertOrExists() && retrieveGrouperProvisioningBehavior.isDeleteMembershipsIfGrouperCreated()) {
        return true;
      }
      return false;
    }
    // regular delete was already checked
    return true;
  }

  /**
   * 
   * @param name
   * @param value
   */
  public String retrieveAttributeValueString(GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    return GrouperUtil.stringValue(this.retrieveAttributeValue(grouperProvisioningConfigurationAttribute));
  }
  
  /**
   * base on attribute get the value
   * @param grouperProvisioningConfigurationAttribute
   * @return the value
   */
  public Object retrieveAttributeValue(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    if (grouperProvisioningConfigurationAttribute == null) {
      throw new NullPointerException("attribute is null: " + this);
    }
    return this.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
  }

  @Override
  public String objectTypeName() {
    return "group";
  }

}
