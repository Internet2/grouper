package edu.internet2.middleware.grouper.app.provisioning;


public class ProvisioningStateGlobal {
  
  private GrouperProvisioner grouperProvisioner;
  
  private boolean selectResultProcessedGroups;
  
  private boolean selectResultProcessedEntities;

  private boolean selectResultProcessedMemberships;
  
  private boolean selectResultProcessedIndividualMemberships;
  
  

  
  public boolean isSelectResultProcessedIndividualMemberships() {
    return selectResultProcessedIndividualMemberships;
  }


  
  public void setSelectResultProcessedIndividualMemberships(
      boolean selectResultProcessedIndividualMemberships) {
    this.selectResultProcessedIndividualMemberships = selectResultProcessedIndividualMemberships;
  }


  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  
  public boolean isSelectResultProcessedGroups() {
    return selectResultProcessedGroups;
  }

  
  public void setSelectResultProcessedGroups(boolean selectResultProcessedGroups) {
    this.selectResultProcessedGroups = selectResultProcessedGroups;
  }

  
  public boolean isSelectResultProcessedEntities() {
    return selectResultProcessedEntities;
  }

  
  public void setSelectResultProcessedEntities(boolean selectResultProcessedEntities) {
    this.selectResultProcessedEntities = selectResultProcessedEntities;
  }

  
  public boolean isSelectResultProcessedMemberships() {
    return selectResultProcessedMemberships;
  }

  
  public void setSelectResultProcessedMemberships(boolean selectResultProcessedMemberships) {
    this.selectResultProcessedMemberships = selectResultProcessedMemberships;
  }
  
}
