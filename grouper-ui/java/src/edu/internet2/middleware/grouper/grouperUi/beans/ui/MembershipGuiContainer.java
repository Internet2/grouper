package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembership;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;


/**
 * gui membership container
 * @author mchyzer
 *
 */
public class MembershipGuiContainer {

  /**
   * gui membership subject container being edited
   */
  private GuiMembershipSubjectContainer guiMembershipSubjectContainer;
  
  /**
   * gui membership subject container for the privileges, map of field name to gui membership subject container
   */
  private GuiMembershipSubjectContainer privilegeGuiMembershipSubjectContainer;
  
  /**
   * gui membership subject container being edited
   * @return container
   */
  public GuiMembershipSubjectContainer getGuiMembershipSubjectContainer() {
    return this.guiMembershipSubjectContainer;
  }

  /**
   * gui membership subject container being edited
   * @param guiMembershipSubjectContainer1
   */
  public void setGuiMembershipSubjectContainer(
      GuiMembershipSubjectContainer guiMembershipSubjectContainer1) {
    this.guiMembershipSubjectContainer = guiMembershipSubjectContainer1;
  }

  /**
   * gui membership subject containers for the privileges, map of field name to gui membership subject container
   * @return mape
   */
  public GuiMembershipSubjectContainer getPrivilegeGuiMembershipSubjectContainer() {
    return this.privilegeGuiMembershipSubjectContainer;
  }

  /**
   * gui membership subject containers for the privileges, map of field name to gui membership subject container
   * @param privilegeGuiMembershipSubjectContainers1
   */
  public void setPrivilegeGuiMembershipSubjectContainer(
      GuiMembershipSubjectContainer privilegeGuiMembershipSubjectContainer1) {
    this.privilegeGuiMembershipSubjectContainer = privilegeGuiMembershipSubjectContainer1;
  }

  /**
   * direct gui membership being edited
   */
  private GuiMembership directGuiMembership;
  
  /**
   * direct gui membership being edited
   * @return direct gui membership
   */
  public GuiMembership getDirectGuiMembership() {
    return this.directGuiMembership;
  }

  /**
   * direct gui membership being edited
   * @param directGuiMembership1
   */
  public void setDirectGuiMembership(GuiMembership directGuiMembership1) {
    this.directGuiMembership = directGuiMembership1;
  }

  /**
   * if has a direct membership
   */
  private boolean directMembership;
  
  /**
   * if has a indirect membership
   */
  private boolean indirectMembership;
  
  /**
   * if has a direct membership
   * @return if direct membership
   */
  public boolean isDirectMembership() {
    return this.directMembership;
  }

  /**
   * if has a direct membership
   * @param directMembership1
   */
  public void setDirectMembership(boolean directMembership1) {
    this.directMembership = directMembership1;
  }

  /**
   * if has a indirect membership
   * @return if indirect
   */
  public boolean isIndirectMembership() {
    return this.indirectMembership;
  }

  /**
   * if has a direct membership
   * @param indirectMembership1
   */
  public void setIndirectMembership(boolean indirectMembership1) {
    this.indirectMembership = indirectMembership1;
  }

  /**
   * if edit membership should go back to subject
   */
  private boolean editMembershipFromSubject;
  
  /**
   * if edit membership should go back to subject
   * @return subject
   */
  public boolean isEditMembershipFromSubject() {
    return this.editMembershipFromSubject;
  }

  /**
   * if edit membership should go back to subject
   * @param editMembershipFromSubject1
   */
  public void setEditMembershipFromSubject(boolean editMembershipFromSubject1) {
    this.editMembershipFromSubject = editMembershipFromSubject1;
  }

  /**
   * list of comma separated privileges for this trace of path
   */
  private String privilegeLabelsString;

  /**
   * list of comma separated privileges for this trace of path
   * @return comma separated privileges
   */
  public String getPrivilegeLabelsString() {
    return this.privilegeLabelsString;
  }

  /**
   * list of comma separated privileges for this trace of path
   * @param privilegeLabelsString1
   */
  public void setPrivilegeLabelsString(String privilegeLabelsString1) {
    this.privilegeLabelsString = privilegeLabelsString1;
  }

  /**
   * number of paths the user is not allowed to see
   */
  private int pathCountNotAllowed;

  /**
   * number of paths the user is not allowed to see
   * @return paths
   */
  public int getPathCountNotAllowed() {
    return this.pathCountNotAllowed;
  }

  /**
   * number of paths the user is not allowed to see
   * @param pathCountNotAllowed1
   */
  public void setPathCountNotAllowed(int pathCountNotAllowed1) {
    this.pathCountNotAllowed = pathCountNotAllowed1;
  }

  /**
   * if should trace memberships from a subject
   */
  private boolean traceMembershipFromSubject;

  /**
   * if should trace memberships from a subject
   * @return if should trace
   */
  public boolean isTraceMembershipFromSubject() {
    return this.traceMembershipFromSubject;
  }

  /**
   * if should trace memberships from a subject
   * @param traceMembershipFromSubject1
   */
  public void setTraceMembershipFromSubject(boolean traceMembershipFromSubject1) {
    this.traceMembershipFromSubject = traceMembershipFromSubject1;
  }

  /**
   * line number of trace starting with 0
   */
  private int lineNumber;
  
  /**
   * line number of trace starting with 0
   * @return line number
   */
  public int getLineNumber() {
    return this.lineNumber;
  }

  /**
   * line number of trace starting with 0
   * @param lineNumber1
   */
  public void setLineNumber(int lineNumber1) {
    this.lineNumber = lineNumber1;
  }

  /**
   * gui group that is the factor of the composite
   */
  private GuiGroup guiGroupFactor;
  
  /**
   * gui group that is the factor of the composite
   * @return group
   */
  public GuiGroup getGuiGroupFactor() {
    return this.guiGroupFactor;
  }

  /**
   * gui group that is the factor of the composite
   * @param guiGroupFactor1
   */
  public void setGuiGroupFactor(GuiGroup guiGroupFactor1) {
    this.guiGroupFactor = guiGroupFactor1;
  }

  /**
   * current gui group e.g. when tracing memberships
   */
  private GuiGroup guiGroupCurrent;
  
  /**
   * current gui stem e.g. when tracing memberships
   */
  private GuiStem guiStemCurrent;
  
  /**
   * current gui stem e.g. when tracing memberships
   * @return gui stem
   */
  public GuiStem getGuiStemCurrent() {
    return this.guiStemCurrent;
  }

  /**
   * current gui stem e.g. when tracing memberships
   * @param guiStemCurrent1
   */
  public void setGuiStemCurrent(GuiStem guiStemCurrent1) {
    this.guiStemCurrent = guiStemCurrent1;
  }

  /**
   * current gui attributeDef e.g. when tracing memberships
   * @return attribute def
   */
  public GuiAttributeDef getGuiAttributeDefCurrent() {
    return this.guiAttributeDefCurrent;
  }

  /**
   * current gui attributeDef e.g. when tracing memberships
   * @param guiAttributeDefCurrent1
   */
  public void setGuiAttributeDefCurrent(GuiAttributeDef guiAttributeDefCurrent1) {
    this.guiAttributeDefCurrent = guiAttributeDefCurrent1;
  }

  /**
   * current gui attributeDef e.g. when tracing memberships
   */
  private GuiAttributeDef guiAttributeDefCurrent;
  
  /**
   * current gui group e.g. when tracing memberships
   * @return gui group
   */
  public GuiGroup getGuiGroupCurrent() {
    return this.guiGroupCurrent;
  }

  /**
   * current gui group e.g. when tracing memberships
   * @param guiGroupCurrent1
   */
  public void setGuiGroupCurrent(GuiGroup guiGroupCurrent1) {
    this.guiGroupCurrent = guiGroupCurrent1;
  }


  /**
   * string of trace memberships
   */
  private String traceMembershipsString;
  
  /**
   * string of trace memberships
   * @return trace memberships
   */
  public String getTraceMembershipsString() {
    return this.traceMembershipsString;
  }

  /**
   * string of trace memberships
   * @param traceMembershipsString1
   */
  public void setTraceMembershipsString(String traceMembershipsString1) {
    this.traceMembershipsString = traceMembershipsString1;
  }


  /**
   * field on the screen
   */
  private Field field;

  /**
   * if should trace memberships from a membership
   */
  private boolean traceMembershipFromMembership;

  /**
   * if should trace memberships from a membership
   * @return should trace
   */
  public boolean isTraceMembershipFromMembership() {
    return this.traceMembershipFromMembership;
  }

  /**
   * if should trace memberships from a membership
   * @param traceMembershipFromMembership1
   */
  public void setTraceMembershipFromMembership(boolean traceMembershipFromMembership1) {
    this.traceMembershipFromMembership = traceMembershipFromMembership1;
  }

  /**
   * field on the screen
   * @return field
   */
  public Field getField() {
    return this.field;
  }

  /**
   * field on the screen
   * @param field1
   */
  public void setField(Field field1) {
    this.field = field1;
  }
  
}
