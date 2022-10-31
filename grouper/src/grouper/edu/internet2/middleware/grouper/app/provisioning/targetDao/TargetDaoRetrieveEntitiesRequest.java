package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveEntitiesRequest {

  public TargetDaoRetrieveEntitiesRequest() {
  }

  private boolean includeNativeEntity;
  
  
  
  
  public boolean isIncludeNativeEntity() {
    return includeNativeEntity;
  }

  
  public void setIncludeNativeEntity(boolean includeNativeEntity) {
    this.includeNativeEntity = includeNativeEntity;
  }

  /**
   * to search for
   */
  private String searchAttribute;
  
  /**
   * values to search for
   */
  private Set<Object> searchAttributeValues;
  
  /**
   * to search for
   * @return
   */
  public String getSearchAttribute() {
    return searchAttribute;
  }

  /**
   * to search for
   * @param searchAttribute
   */
  public void setSearchAttribute(String searchAttribute) {
    this.searchAttribute = searchAttribute;
  }

  /**
   * values to search for
   * @return
   */
  public Set<Object> getSearchAttributeValues() {
    return searchAttributeValues;
  }

  /**
   * values to search for
   * @param searchAttributeValues
   */
  public void setSearchAttributeValues(Set<Object> searchAttributeValues) {
    this.searchAttributeValues = searchAttributeValues;
  }

  public TargetDaoRetrieveEntitiesRequest(List<ProvisioningEntity> targetEntities,
      boolean includeAllMembershipsIfApplicable) {
    this.targetEntities = targetEntities;
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }

  private List<ProvisioningEntity> targetEntities;
  
  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }

  
  /**
   * if memberships are part of entity (e.g. in attribute), then if true, get all memberships too, otherwise just get entity part
   */
  private boolean includeAllMembershipsIfApplicable;

  /**
   * if memberships are part of entity (e.g. in attribute), then if true, get all memberships too, otherwise just get entity part
   * @return
   */
  public boolean isIncludeAllMembershipsIfApplicable() {
    return includeAllMembershipsIfApplicable;
  }

  /**
   * if memberships are part of entity (e.g. in attribute), then if true, get all memberships too, otherwise just get entity part
   * @param includeAllMembershipsIfApplicable
   */
  public void setIncludeAllMembershipsIfApplicable(
      boolean includeAllMembershipsIfApplicable) {
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }
}
