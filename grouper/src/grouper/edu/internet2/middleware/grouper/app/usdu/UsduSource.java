package edu.internet2.middleware.grouper.app.usdu;


public class UsduSource {

  private String sourceId;
  
  private String sourceLabel;
  
  private int maxUnresolvableSubjects;
  
  private boolean removeUpToFailsafe;
  
  private int deleteAfterDays;

  
  public String getSourceId() {
    return sourceId;
  }

  
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  
  public String getSourceLabel() {
    return sourceLabel;
  }

  
  public void setSourceLabel(String sourceLabel) {
    this.sourceLabel = sourceLabel;
  }

  
  public boolean isRemoveUpToFailsafe() {
    return removeUpToFailsafe;
  }

  
  public void setRemoveUpToFailsafe(boolean removeUpToFailsafe) {
    this.removeUpToFailsafe = removeUpToFailsafe;
  }


  
  public int getMaxUnresolvableSubjects() {
    return maxUnresolvableSubjects;
  }


  
  public void setMaxUnresolvableSubjects(int maxUnresolvableSubjects) {
    this.maxUnresolvableSubjects = maxUnresolvableSubjects;
  }


  
  public int getDeleteAfterDays() {
    return deleteAfterDays;
  }


  
  public void setDeleteAfterDays(int deleteAfterDays) {
    this.deleteAfterDays = deleteAfterDays;
  }

  
  
}
