package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * summary for one group in a group import
 * @author mchyzer
 *
 */
public class GroupImportGroupSummary {

  private boolean complete = false;
  
  private List<GroupImportError> groupImportErrors = new ArrayList<GroupImportError>();
  
  public List<GroupImportError> getGroupImportErrors() {
    return groupImportErrors;
  }


  public boolean isComplete() {
    return complete;
  }

  
  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  /**
   * count of added members
   */
  private int groupCountAdded;
  
  public void groupCountAddedIncrement() {
    this.groupCountAdded++;
  }
  
  public void groupCountDeletedIncrement() {
    this.groupCountDeleted++;
  }
  
  public void groupCountErrorsIncrement() {
    this.groupCountErrors++;
  }
  

  /**
   * count of deleted members
   */
  private int groupCountDeleted;
  /**
   * count of errors
   */
  private int groupCountErrors;
  /**
   * new group count of members
   */
  private int groupCountNew;
  /**
   * original group count of members
   */
  private int groupCountOriginal;

  public GroupImportGroupSummary() {
  }

  /**
   * count of added members
   * @return the groupCountAdded
   */
  public int getGroupCountAdded() {
    return this.groupCountAdded;
  }

  /**
   * count of deleted members
   * @return the groupCountDeleted
   */
  public int getGroupCountDeleted() {
    return this.groupCountDeleted;
  }

  /**
   * count of errors
   * @return the groupCountErrors
   */
  public int getGroupCountErrors() {
    return this.groupCountErrors;
  }

  /**
   * new group count of members
   * @return the groupCountNew
   */
  public int getGroupCountNew() {
    return this.groupCountNew;
  }

  /**
   * original group count of members
   * @return the groupCountOriginal
   */
  public int getGroupCountOriginal() {
    return this.groupCountOriginal;
  }

  /**
   * count of added members
   * @param groupCountAdded1 the groupCountAdded to set
   */
  public void setGroupCountAdded(int groupCountAdded1) {
    this.groupCountAdded = groupCountAdded1;
  }

  /**
   * count of deleted members
   * @param groupCountDeleted1 the groupCountDeleted to set
   */
  public void setGroupCountDeleted(int groupCountDeleted1) {
    this.groupCountDeleted = groupCountDeleted1;
  }

  /**
   * count of errors
   * @param groupCountErrors1 the groupCountErrors to set
   */
  public void setGroupCountErrors(int groupCountErrors1) {
    this.groupCountErrors = groupCountErrors1;
  }

  /**
   * new group count of members
   * @param groupCountNew1 the groupCountNew to set
   */
  public void setGroupCountNew(int groupCountNew1) {
    this.groupCountNew = groupCountNew1;
  }

  /**
   * original group count of members
   * @param groupCountOriginal1 the groupCountOriginal to set
   */
  public void setGroupCountOriginal(int groupCountOriginal1) {
    this.groupCountOriginal = groupCountOriginal1;
  }

  
  
}
