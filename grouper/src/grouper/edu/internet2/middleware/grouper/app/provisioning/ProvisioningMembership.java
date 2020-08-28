package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * tuple of group and entity in target system
 * @author mchyzer
 *
 */
public class ProvisioningMembership implements ProvisioningUpdatable {

  /**
   * id of membership (optional)
   */
  private String id;
  
  private String provisioningGroupId;
  
  private String provisioningEntityId;
  
  /**
   * group of memProvisioningGroup*ProvisioningGroup ProvisioningGroup targetGroup;
  
  /**
   * entity of membership
   */
  private ProvisioningEntity provisioningEntity;
  
  private ProvisioningGroup provisioningGroup;

  /**
   * more attributes in name/value pairs
   */
  private Map<String, ProvisioningAttribute> attributes = new HashMap<String, ProvisioningAttribute>();

  
  private Map<MultiKey, Object> internal_fieldsToUpdate = null;
  
  private ProvisioningMembershipWrapper provisioningMembershipWrapper;
  
  
  
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }


  
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
  }


  /**
   * multikey is either the string "field", "attribute", the second param is field name or attribute name
   * third param is "insert", "update", or "delete"
   * and the value is the old value
   * @return
   */
  public Map<MultiKey, Object> getInternal_fieldsToUpdate() {
    return internal_fieldsToUpdate;
  }

  
  public void setInternal_fieldsToUpdate(Map<MultiKey, Object> internal_fieldsToUpdate) {
    this.internal_fieldsToUpdate = internal_fieldsToUpdate;
  }
  
  /**
   * id of membership (optional)
   * @return id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * id of membership (optional)
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  

  
  public ProvisioningGroup getProvisioningGroup() {
    return provisioningGroup;
  }

  
  public void setProvisioningGroup(ProvisioningGroup provisioningGroup) {
    this.provisioningGroup = provisioningGroup;
  }

  

  
  public ProvisioningEntity getProvisioningEntity() {
    return provisioningEntity;
  }


  
  public void setProvisioningEntity(ProvisioningEntity provisioningEntity) {
    this.provisioningEntity = provisioningEntity;
  }


  /**
   * more attributes in name/value pairs
   * @return attributes
   */
  public Map<String, ProvisioningAttribute> getAttributes() {
    return attributes;
  }

  /**
   * more attributes in name/value pairs
   * @param attributes1
   */
  public void setAttributes(Map<String, ProvisioningAttribute> attributes1) {
    this.attributes = attributes1;
  }

  
  
  public String getProvisioningGroupId() {
    return provisioningGroupId;
  }


  
  public void setProvisioningGroupId(String provisioningGroupId) {
    this.provisioningGroupId = provisioningGroupId;
  }


  public String getProvisioningEntityId() {
    return provisioningEntityId;
  }


  
  public void setProvisioningEntityId(String provisioningEntityId) {
    this.provisioningEntityId = provisioningEntityId;
  }

  
}
