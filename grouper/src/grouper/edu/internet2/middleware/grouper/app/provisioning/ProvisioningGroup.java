package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.util.GrouperUtil;
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
      if (this.provisioningGroupWrapper.isRecalcObject()) {
        firstField = toStringAppendField(result, firstField, "recalcObject", this.provisioningGroupWrapper.isRecalcObject());
      }
      if (this.provisioningGroupWrapper.isRecalcGroupMemberships()) {
        firstField = toStringAppendField(result, firstField, "recalcMships", this.provisioningGroupWrapper.isRecalcGroupMemberships());
      }
      if (this.provisioningGroupWrapper.isCreate()) {
        firstField = toStringAppendField(result, firstField, "create", this.provisioningGroupWrapper.isCreate());
      }
      if (this.provisioningGroupWrapper.isDelete()) {
        firstField = toStringAppendField(result, firstField, "delete", this.provisioningGroupWrapper.isDelete());
      }
      if (this.provisioningGroupWrapper.isIncrementalSyncMemberships()) {
        firstField = toStringAppendField(result, firstField, "incrementalSyncMemberships", this.provisioningGroupWrapper.isIncrementalSyncMemberships());
      }
      if (this.provisioningGroupWrapper.getErrorCode() != null) {
        firstField = toStringAppendField(result, firstField, "errorCode", this.provisioningGroupWrapper.getErrorCode().name());
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

    this.cloneUpdatable(provisioningGroup);
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

      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper().get(deleteValue);
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
