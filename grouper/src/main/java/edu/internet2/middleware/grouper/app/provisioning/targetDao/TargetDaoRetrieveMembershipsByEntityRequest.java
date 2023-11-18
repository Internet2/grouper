package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveMembershipsByEntityRequest {
  
  /**
   * to search for
   */
  private String searchAttribute;
  
  /**
   * values to search for
   */
  private Object searchAttributeValue;
  
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
   * value to search for
   * @return
   */
  public Object getSearchAttributeValue() {
    return searchAttributeValue;
  }

  /**
   * value to search for
   * @param searchAttributeValue
   */
  public void setSearchAttributeValue(Object searchAttributeValue) {
    this.searchAttributeValue = searchAttributeValue;
  }

  private ProvisioningEntity targetEntity;
  
  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public TargetDaoRetrieveMembershipsByEntityRequest(
      ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public TargetDaoRetrieveMembershipsByEntityRequest() {
  }
  
  
  
}
