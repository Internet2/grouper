package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

public class GrouperProvisioningErrorSummary {
  
  private long errorsCount;
  
  private long groupErrorsCount;
  
  private Map<String, Long> groupErrorTypeCount;
  
  private long entityErrorsCount;
  
  private Map<String, Long> entityErrorTypeCount;
  
  private long membershipErrorsCount;
  
  private Map<String, Long> membershipsErrorTypeCount;

  
  public long getErrorsCount() {
    return errorsCount;
  }

  
  public void setErrorsCount(long errorsCount) {
    this.errorsCount = errorsCount;
  }

  
  public long getGroupErrorsCount() {
    return groupErrorsCount;
  }

  
  public void setGroupErrorsCount(long groupErrorsCount) {
    this.groupErrorsCount = groupErrorsCount;
  }

  
  public Map<String, Long> getGroupErrorTypeCount() {
    return groupErrorTypeCount;
  }

  
  public void setGroupErrorTypeCount(Map<String, Long> groupErrorTypeCount) {
    this.groupErrorTypeCount = groupErrorTypeCount;
  }

  
  public long getEntityErrorsCount() {
    return entityErrorsCount;
  }

  
  public void setEntityErrorsCount(long entityErrorsCount) {
    this.entityErrorsCount = entityErrorsCount;
  }

  
  public Map<String, Long> getEntityErrorTypeCount() {
    return entityErrorTypeCount;
  }

  
  public void setEntityErrorTypeCount(Map<String, Long> entityErrorTypeCount) {
    this.entityErrorTypeCount = entityErrorTypeCount;
  }

  
  public long getMembershipErrorsCount() {
    return membershipErrorsCount;
  }

  
  public void setMembershipErrorsCount(long membershipErrorsCount) {
    this.membershipErrorsCount = membershipErrorsCount;
  }

  
  public Map<String, Long> getMembershipsErrorTypeCount() {
    return membershipsErrorTypeCount;
  }

  
  public void setMembershipsErrorTypeCount(Map<String, Long> membershipsErrorTypeCount) {
    this.membershipsErrorTypeCount = membershipsErrorTypeCount;
  }
  
}
