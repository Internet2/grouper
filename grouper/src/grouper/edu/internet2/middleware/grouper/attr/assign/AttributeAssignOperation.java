/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * can be used for generic method for attribute assignments
 */
public enum AttributeAssignOperation {
  
  /** if the attribute is there, leave it alone, else assign it */
  assign_attr,
  
  /** whether or not the attribute is already assigned, assign it again */
  add_attr,
  
  /** take this set of attributes and replace what is there (filter by names of attributeDefs) */
  replace_attrs,
  
  /** remove all assignments of this attribute name, or just certain attributes if by id */
  remove_attr;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignOperation valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignOperation.class, 
        string, exceptionOnNull);
  }
 
  
}
