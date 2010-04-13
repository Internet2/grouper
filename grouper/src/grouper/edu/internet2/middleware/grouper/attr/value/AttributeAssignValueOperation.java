/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * can be used for generic method for attribute assignments
 */
public enum AttributeAssignValueOperation {
  
  /** if the value is there, leave it alone, else change an existing value for it */
  assign_value,
  
  /** whether or not the attribute is already assigned, assign it again */
  add_value,
  
  /** remove all assignments of this attribute value */
  remove_value,
  
  /** take the new values, and replace the existing values */ 
  replace_values;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignValueOperation valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignValueOperation.class, 
        string, exceptionOnNull);
  }

}
