package edu.internet2.middleware.grouper.grouperUi.beans.dojo;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.GrouperSession;

/**
 * base abstract class for dojo combos
 * @author mchyzer
 *
 */
public abstract class DojoComboQueryLogicBase<T> implements DojoComboQueryLogic<T> {

  /**
   * return true if this is a valid query, or false for the default behavior
   */
  @Override
  public boolean validQueryOverride(GrouperSession grouperSession, String query) {
    return false;
  }

  /**
   * @see DojoComboQueryLogic#retrieveHtmlLabel(GrouperSession, Object)
   */
  @Override
  public String retrieveHtmlLabel(GrouperSession grouperSession, T t) {
    return null;
  }

  /**
   * @see DojoComboQueryLogic#initialValidationError(HttpServletRequest, GrouperSession)
   */
  @Override
  public String initialValidationError(HttpServletRequest request,
      GrouperSession grouperSession) {
    return null;
  }

  
  
}
