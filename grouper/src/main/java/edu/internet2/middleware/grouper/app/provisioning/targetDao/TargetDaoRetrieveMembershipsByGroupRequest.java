package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveMembershipsByGroupRequest {

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

  private ProvisioningGroup targetGroup;

  
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }

  
  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }


  public TargetDaoRetrieveMembershipsByGroupRequest() {
  }


  public TargetDaoRetrieveMembershipsByGroupRequest(
      ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }
  
}
