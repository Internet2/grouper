/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

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
