/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public enum AttributeDefType {
    
  /** if this is an attribute */
  attr, 
  
  /** group up things into one application */
  domain, 
  
  /** if this is a type */
  type, 
  
  /** if this is a limit of an attribute */
  limit, 
  
  /** if this is a permission */
  perm;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefType.class, 
        string, exceptionOnNull);

  }
  
}
