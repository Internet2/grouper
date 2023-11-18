package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveMembershipsByEntitiesRequest {

  public TargetDaoRetrieveMembershipsByEntitiesRequest() {
  }
  
  /**
   * to search for
   */
  private String searchAttribute;
  
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
   */
  private Set<Object> searchAttributeValues;
  
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
  
  
  private List<ProvisioningEntity> targetEntities;

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }


  public TargetDaoRetrieveMembershipsByEntitiesRequest(List<ProvisioningEntity> targetEntities) {
    super();
    this.targetEntities = targetEntities;
  }
}
