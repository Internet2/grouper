package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Field;


/**
 * gui membership container
 * @author mchyzer
 *
 */
public class MembershipGuiContainer {
  
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
