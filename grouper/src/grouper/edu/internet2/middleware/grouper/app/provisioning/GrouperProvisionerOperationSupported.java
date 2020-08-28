package edu.internet2.middleware.grouper.app.provisioning;


public class GrouperProvisionerOperationSupported {
  
  private Boolean retrieveAllGroupsSupported;
  
  private Boolean retrieveAllMembershipsSupported;

  
  public Boolean getRetrieveAllGroupsSupported() {
    return retrieveAllGroupsSupported;
  }

  
  public void setRetrieveAllGroupsSupported(Boolean retrieveAllGroupsSupported) {
    this.retrieveAllGroupsSupported = retrieveAllGroupsSupported;
  }

  
  public Boolean getRetrieveAllMembershipsSupported() {
    return retrieveAllMembershipsSupported;
  }

  
  public void setRetrieveAllMembershipsSupported(Boolean retrieveAllMembershipsSupported) {
    this.retrieveAllMembershipsSupported = retrieveAllMembershipsSupported;
  }
  
}
