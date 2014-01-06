/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.dojo;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.GrouperSession;


/**
 * Logic you implement for a query from a combobox
 * The template is the return type for the queries
 * @author mchyzer
 *
 */
public interface DojoComboQueryLogic<T> {

  /**
   * return true if this is a valid query, or false for the default behavior
   * @param grouperSession
   * @param query
   */
  public boolean validQueryOverride(GrouperSession grouperSession, String query);
  
  /**
   * lookup one object (could be by name or id).  Note,
   * this should do the security too
   * @param query
   * @param request
   * @param grouperSession
   * @return the object or null if not found.
   */
  public T lookup(HttpServletRequest request, GrouperSession grouperSession, String query);
  
  /**
   * get a paged list of return objects by the query.  Note,
   * this should do the security too
   * @param query
   * @param request
   * @param grouperSession
   * @return the objects or null if not found
   */
  public Collection<T> search(HttpServletRequest request, GrouperSession grouperSession, String query);

  /**
   * get the id of the object for the json, do not escape HTML
   * @param t
   * @return the id
   */
  public String retrieveId(GrouperSession grouperSession, T t);
  
  /**
   * get the label of the object for the json, do not escape HTML
   * @param t
   * @return the id
   */
  public String retrieveLabel(GrouperSession grouperSession, T t);
  
  /**
   * get the html label (if applicable) for the object for the json.
   * if null, then it will just use the label
   * @param grouperSession
   * @param t
   * @return the label
   */
  public String retrieveHtmlLabel(GrouperSession grouperSession, T t);
  
  /**
   * return a string if there is some sort of validation error at the beginning
   * @param request
   * @param groupperSession
   * @return an error message or null if none
   */
  public String initialValidationError(HttpServletRequest request, GrouperSession grouperSession);
}
