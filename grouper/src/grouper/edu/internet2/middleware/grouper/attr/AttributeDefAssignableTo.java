/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * types of objects which attributes can be assigned to
 *
 */
public enum AttributeDefAssignableTo {

  /** attribute assigned to a group */
  group, 
  
  /** attribute assigned to a stem */
  stem, 

  /** attribute assigned to a membership */
  membership, 
  
  /** attribute assigned to a member */
  member, 
  
  /** attribute assigned to a group attribute */
  groupAttribute, 

  /** attribute assigned to a stem attribute */
  stemAttribute, 
  
  /** attribute assigned to a membership attribute */
  membershipAttribute, 

  /** attribute assigned to a member attribute */
  memberAttribute;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefAssignableTo valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefAssignableTo.class, 
        string, exceptionOnNull);

  }
  
}
