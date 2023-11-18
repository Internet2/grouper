package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;

public class ActionWithLimits {
  
  private AttributeAssignAction action;
  private Set<PermissionLimitBean> limits;
  private boolean assigned;
  
  public ActionWithLimits(AttributeAssignAction action, 
      Set<PermissionLimitBean> limits, boolean assigned) {
    this.action = action;
    this.limits = limits;
    this.assigned = assigned;
  }
  
  public AttributeAssignAction getAction() {
    return action;
  }
  
  public void setAction(AttributeAssignAction action) {
    this.action = action;
  }
  
  public Set<PermissionLimitBean> getLimits() {
    return limits;
  }
  
  public void setLimits(Set<PermissionLimitBean> limits) {
    this.limits = limits;
  }
  
  public boolean isAssigned() {
    return assigned;
  }
  
  public void setAssigned(boolean assigned) {
    this.assigned = assigned;
  }

}
