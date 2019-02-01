package edu.internet2.middleware.grouper.app.provisioning;


public class GrouperProvisioningTarget {
  
  private String key;
  
  private Boolean groupAllowedToAssign;
  
  private Boolean allowAssignmentsOnlyOnOneStem;
  
  public GrouperProvisioningTarget(String key, Boolean groupAllowedToAssign, Boolean allowAssignmentsOnlyOnOneStem) {
   this.key = key;
   this.groupAllowedToAssign = groupAllowedToAssign;
   this.allowAssignmentsOnlyOnOneStem = allowAssignmentsOnlyOnOneStem;
  }
  
  public String getKey() {
    return key;
  }

  
  public Boolean getGroupAllowedToAssign() {
    return groupAllowedToAssign;
  }

  
  public Boolean getAllowAssignmentsOnlyOnOneStem() {
    return allowAssignmentsOnlyOnOneStem;
  }

}
