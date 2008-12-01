/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * member filter for retrieving members.
 * 
 * @author mchyzer
 * 
 */
public enum WsMemberFilter {
  /** retrieve all members (immediate and effective) */
  All,

  /** retrieve non direct (non immediate) members */
  Effective,

  /** return only direct members, not indirect */
  Immediate,

  /**
   * if this is a composite group, then return the two groups which make up
   * the composition (and the group math operator (union, minus, etc)
   */
  Composite;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsMemberFilter valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(WsMemberFilter.class, string, false);
  }
}
