/**
 * 
 */
package edu.internet2.middleware.grouper.group;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 * @author mchyzer
 */
public enum TypeOfGroup {
  
  /** group (normal group of subjects) */
  group,
  
  /** can be assigned groups or other subjects, and also privileges */
  role;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static TypeOfGroup valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(TypeOfGroup.class, 
        string, exceptionOnNull);

  }

}
