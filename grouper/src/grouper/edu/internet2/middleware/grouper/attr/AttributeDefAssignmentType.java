package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of assignment, immediate for a direct assignment, 
 * effective for an assignment due to another assignment (e.g. a set of attributes, 
 * or a role hierarchy), or self for each attribute def (so we can do a simple join)
 * @author mchyzer
 *
 */
public enum AttributeDefAssignmentType {

  /** immediate for a direct assignment */
  immediate,
  
  /** row for self so we can do a simple join */
  self,
  
  /** 
   * effective for an assignment due to another assignment (e.g. a set of attributes, 
   * or a role hierarchy)
   */
  effective;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefAssignmentType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefAssignmentType.class, 
        string, exceptionOnNull);

  }
}
