package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * if searching for inheritance ancestors or descendents, this is the relation
 * to the current one being searched for
 * 
 * @author mchyzer
 *
 */
public enum WsInheritanceSetRelation {

  /**
   * find values that are implied by this value
   */
  IMPLIED_BY_THIS,

  /**
   * find values that are implied this value immediately, i.e. can be directly unassigned
   */
  IMPLIED_BY_THIS_IMMEDIATE,
  
  /**
   * find values that imply this value
   */
  THAT_IMPLY_THIS,
  
  /**
   * find values that imply this value immediately, i.e. can be directly unassigned
   */
  THAT_IMPLY_THIS_IMMEDIATE;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsInheritanceSetRelation valueOfIgnoreCase(String string) {
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsInheritanceSetRelation.class, string, false);
  }
  
}
