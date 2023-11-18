package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveGroupRequest {

  public TargetDaoRetrieveGroupRequest() {
  }

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


  public TargetDaoRetrieveGroupRequest(ProvisioningGroup targetGroup,
      boolean includeAllMembershipsIfApplicable) {
    super();
    this.targetGroup = targetGroup;
    this.includeAllMembershipsIfApplicable = includeAllMembershipsIfApplicable;
  }


  
}
