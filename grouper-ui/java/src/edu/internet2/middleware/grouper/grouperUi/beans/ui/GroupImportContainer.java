/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;


/**
 * data for group import
 * @author mchyzer
 *
 */
public class GroupImportContainer {

  /**
   * original group count of members
   */
  private int groupCountOriginal;
  
  /**
   * new group count of members
   */
  private int groupCountNew;
  
  /**
   * count of added members
   */
  private int groupCountAdded;
  
  /**
   * count of deleted members
   */
  private int groupCountDeleted;
  
  /**
   * count of errors
   */
  private int groupCountErrors;
  
  /**
   * original group count of members
   * @return the groupCountOriginal
   */
  public int getGroupCountOriginal() {
    return this.groupCountOriginal;
  }

  
  /**
   * original group count of members
   * @param groupCountOriginal1 the groupCountOriginal to set
   */
  public void setGroupCountOriginal(int groupCountOriginal1) {
    this.groupCountOriginal = groupCountOriginal1;
  }

  
  /**
   * new group count of members
   * @return the groupCountNew
   */
  public int getGroupCountNew() {
    return this.groupCountNew;
  }

  
  /**
   * new group count of members
   * @param groupCountNew1 the groupCountNew to set
   */
  public void setGroupCountNew(int groupCountNew1) {
    this.groupCountNew = groupCountNew1;
  }

  
  /**
   * count of added members
   * @return the groupCountAdded
   */
  public int getGroupCountAdded() {
    return this.groupCountAdded;
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
   * @return the groupCountDeleted
   */
  public int getGroupCountDeleted() {
    return this.groupCountDeleted;
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
   * @return the groupCountErrors
   */
  public int getGroupCountErrors() {
    return this.groupCountErrors;
  }

  
  /**
   * count of errors
   * @param groupCountErrors1 the groupCountErrors to set
   */
  public void setGroupCountErrors(int groupCountErrors1) {
    this.groupCountErrors = groupCountErrors1;
  }

  /**
   * row number there is an error
   */
  private int errorRowNumber;
  
  /**
   * row number there is an error
   * @return the errorRowNumber
   */
  public int getErrorRowNumber() {
    return this.errorRowNumber;
  }
  
  /**
   * row number there is an error
   * @param errorRowNumber1 the errorRowNumber to set
   */
  public void setErrorRowNumber(int errorRowNumber1) {
    this.errorRowNumber = errorRowNumber1;
  }

  /**
   * error text
   */
  private String errorText;
  
  /**
   * error text
   * @return the errorText
   */
  public String getErrorText() {
    return this.errorText;
  }
  
  /**
   * error text
   * @param errorText1 the errorText to set
   */
  public void setErrorText(String errorText1) {
    this.errorText = errorText1;
  }

  /**
   * error subject
   */
  private String errorSubject;
  
  
  /**
   * error subject
   * @return the errorSubject
   */
  public String getErrorSubject() {
    return this.errorSubject;
  }
  
  /**
   * error subject
   * @param errorSubject1 the errorSubject to set
   */
  public void setErrorSubject(String errorSubject1) {
    this.errorSubject = errorSubject1;
  }

  /**
   * groups which we are importing to
   */
  private Set<GuiGroup> guiGroups;
  
  /**
   * key is group name, value is the report for the group
   */
  private Map<String, String> reportForGroupNameMap;
  
  /**
   * groups which we are importing to
   * @return groups
   */
  public Set<GuiGroup> getGuiGroups() {
    return this.guiGroups;
  }

  /**
   * groups which we are importing to
   * @param groups1
   */
  public void setGuiGroups(Set<GuiGroup> groups1) {
    this.guiGroups = groups1;
  }

  /**
   * key is group name, value is the report for the group
   * @return map
   */
  public Map<String, String> getReportForGroupNameMap() {
    return this.reportForGroupNameMap;
  }

  /**
   * key is group name, value is the report for the group
   * @param reportForGroupNameMap1
   */
  public void setReportForGroupNameMap(Map<String, String> reportForGroupNameMap1) {
    this.reportForGroupNameMap = reportForGroupNameMap1;
  }

  /**
   * comma separated list of entity ids not found
   */
  private String entityIdsNotFound;
  
  /**
   * comma separated list of entity ids not found
   * @return list
   */
  public String getEntityIdsNotFound() {
    return this.entityIdsNotFound;
  }

  /**
   * comma separated list of entity ids not found
   * @param entityIdsNotFound1
   */
  public void setEntityIdsNotFound(String entityIdsNotFound1) {
    this.entityIdsNotFound = entityIdsNotFound1;
  }

  /**
   * gui groups in addition to the one in the combobox
   */
  private Set<GuiGroup> groupImportExtraGuiGroups;

  /**
   * gui subjects in addition to the one in the combobox
   */
  private Set<GuiSubject> groupImportExtraGuiSubjects;
  
  /**
   * gui subjects in addition to the one in the combobox
   * @return gui subjects
   */
  public Set<GuiSubject> getGroupImportExtraGuiSubjects() {
    return this.groupImportExtraGuiSubjects;
  }

  /**
   * gui subjects in addition to the one in the combobox
   * @param groupImportExtraGuiSubjects1
   */
  public void setGroupImportExtraGuiSubjects(Set<GuiSubject> groupImportExtraGuiSubjects1) {
    this.groupImportExtraGuiSubjects = groupImportExtraGuiSubjects1;
  }

  /**
   * if import from group
   */
  private boolean importFromGroup;
  /**
   * if import from subject
   */
  private boolean importFromSubject;

  /**
   * gui groups in addition to the one in the combobox
   * @return the set of groups
   */
  public Set<GuiGroup> getGroupImportExtraGuiGroups() {
    return this.groupImportExtraGuiGroups;
  }

  /**
   * if there is a subject in url and we want to populate that field, 
   * then this is the value
   * @return the value
   */
  public String getImportDefaultSubject() {
    
    SubjectContainer subjectContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSubjectContainer();
    
    if(subjectContainer.getGuiSubject() != null) {
      return subjectContainer.getGuiSubject().getSubject().getSourceId() + "||" + subjectContainer.getGuiSubject().getSubject().getId();
    }
    return null;
  }

  /**
   * if import from group
   * @return if from group
   */
  public boolean isImportFromGroup() {
    return this.importFromGroup;
  }

  /**
   * if import from subject
   * @return if from subject
   */
  public boolean isImportFromSubject() {
    return this.importFromSubject;
  }

  /**
   * gui groups in addition to the one in the combobox
   * @param groupImportExtraGuiGroups1
   */
  public void setGroupImportExtraGuiGroups(Set<GuiGroup> groupImportExtraGuiGroups1) {
    this.groupImportExtraGuiGroups = groupImportExtraGuiGroups1;
  }

  /**
   * if import from group
   * @param importFromGroup1
   */
  public void setImportFromGroup(boolean importFromGroup1) {
    this.importFromGroup = importFromGroup1;
  }

  /**
   * if import from subject
   * @param importFromSubject1
   */
  public void setImportFromSubject(boolean importFromSubject1) {
    this.importFromSubject = importFromSubject1;
  }

}
