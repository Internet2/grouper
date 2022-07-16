package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveMembershipsByGroupsRequest {
  
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

  private List<ProvisioningGroup> targetGroups;
  /**
   * values to search for
   */
  private Set<Object> searchAttributeValues;

  
  public TargetDaoRetrieveMembershipsByGroupsRequest(
      List<ProvisioningGroup> targetGroups) {
    super();
    this.targetGroups = targetGroups;
  }


  public TargetDaoRetrieveMembershipsByGroupsRequest() {
    super();
    // TODO Auto-generated constructor stub
  }


  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
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
}
