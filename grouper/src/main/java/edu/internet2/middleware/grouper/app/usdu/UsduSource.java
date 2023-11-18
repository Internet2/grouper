package edu.internet2.middleware.grouper.app.usdu;

/**
 * 
 * bean to hold the usdu configuration defined in properties files
 */
public class UsduSource {

  /**
   * source id of the subjects
   */
  private String sourceId;
  
  /**
   * source label of the subjects
   */
  private String sourceLabel;
  
  /**
   * maximum number of subjects that can be deleted in one job run
   */
  private int maxUnresolvableSubjects;
  
  /**
   * should the subjects up to maxUnresolvableSubjects be deleted
   * if more than that found while running the job 
   */
  private boolean removeUpToFailsafe;
  
  /**
   * after how many days subject should be deleted after being unresolved
   */
  private int deleteAfterDays;

  
  /**
   * 
   * @return source id of the subjects
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * source id of the subjects
   * @param sourceId
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * 
   * @return source label of the subjects
   */
  public String getSourceLabel() {
    return sourceLabel;
  }

  /**
   * source label of the subjects
   * @param sourceLabel
   */
  public void setSourceLabel(String sourceLabel) {
    this.sourceLabel = sourceLabel;
  }

  /**
   * should the subjects up to maxUnresolvableSubjects be deleted
   * if more than that found while running the job 
   * @return
   */
  public boolean isRemoveUpToFailsafe() {
    return removeUpToFailsafe;
  }

  /**
   * should the subjects up to maxUnresolvableSubjects be deleted
   * if more than that found while running the job 
   * @param removeUpToFailsafe
   */
  public void setRemoveUpToFailsafe(boolean removeUpToFailsafe) {
    this.removeUpToFailsafe = removeUpToFailsafe;
  }


  /**
   * 
   * @return maximum number of subjects that can be deleted in one job run
   */
  public int getMaxUnresolvableSubjects() {
    return maxUnresolvableSubjects;
  }


  /**
   * maximum number of subjects that can be deleted in one job run
   * @param maxUnresolvableSubjects
   */
  public void setMaxUnresolvableSubjects(int maxUnresolvableSubjects) {
    this.maxUnresolvableSubjects = maxUnresolvableSubjects;
  }


  /**
   * after how many days subject should be deleted after being unresolved
   * @return
   */
  public int getDeleteAfterDays() {
    return deleteAfterDays;
  }


  /**
   * after how many days subject should be deleted after being unresolved
   * @param deleteAfterDays
   */
  public void setDeleteAfterDays(int deleteAfterDays) {
    this.deleteAfterDays = deleteAfterDays;
  }
  
}
