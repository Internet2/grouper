package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsByEntitiesResponse {

  public TargetDaoRetrieveMembershipsByEntitiesResponse() {
  }

  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   */
  private Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = new HashMap<ProvisioningEntity, Object>();

  
  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   * @return
   */
  public Map<ProvisioningEntity, Object> getTargetEntityToTargetNativeEntity() {
    return targetEntityToTargetNativeEntity;
  }

  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   * @param targetEntityToTargetNativeEntity
   */
  public void setTargetEntityToTargetNativeEntity(Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity) {
    this.targetEntityToTargetNativeEntity = targetEntityToTargetNativeEntity;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  private List<ProvisioningMembership> targetMemberships;

  private List<ProvisioningEntity> targetEntities;
  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsByEntitiesResponse(
      List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }
  
}
