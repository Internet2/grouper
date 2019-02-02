package edu.internet2.middleware.grouper.app.provisioning;


public class GrouperProvisioningTarget {
  
  private String key;
  
  private String groupAllowedToAssign;
  
  private boolean allowAssignmentsOnlyOnOneStem;
  
  private boolean readOnly;
  
  public GrouperProvisioningTarget(String key, String groupAllowedToAssign, 
      boolean allowAssignmentsOnlyOnOneStem, boolean readOnly) {
   this.key = key;
   this.groupAllowedToAssign = groupAllowedToAssign;
   this.allowAssignmentsOnlyOnOneStem = allowAssignmentsOnlyOnOneStem;
   this.readOnly = readOnly;
  }
  
  public String getKey() {
    return key;
  }

  public String getGroupAllowedToAssign() {
    return groupAllowedToAssign;
  }
  
  public Boolean isAllowAssignmentsOnlyOnOneStem() {
    return allowAssignmentsOnlyOnOneStem;
  }

  
  public boolean isReadOnly() {
    return readOnly;
  }
  
}
