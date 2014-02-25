package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;


/**
 * gui membership container
 * @author mchyzer
 *
 */
public class MembershipGuiContainer {

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
