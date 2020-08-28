package edu.internet2.middleware.grouper.app.provisioning;

/**
 * tuple of group and entity in target system
 * @author mchyzer
 *
 */
public class ProvisioningMembership extends ProvisioningUpdatable {

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

  private ProvisioningMembershipWrapper provisioningMembershipWrapper;
  
  
  
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }


  
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
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
