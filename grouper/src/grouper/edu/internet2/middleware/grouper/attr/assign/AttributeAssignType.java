/**
 * @author Kate
 * $Id: AttributeAssignType.java,v 1.1 2009-10-10 18:02:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum AttributeAssignType {
  
  /** attribute assigned to group */
  group,

  /** attribute assigned to member */
  member,
  
  /** attribute assigned to stem */
  stem,

  /** attribute assigned to effective membership */
  any_mem,
  
  /** attribute assigned to immediate membership */
  imm_mem,
  
  /** attribute assigned to attribute def */
  attr_def,
  
  /** attribute assigned to group assignment */
  group_asgn,
  
  /** attribute assigned to member assignment */
  mem_asgn,
  
  /** attribute assigned to stem assignment */
  stem_asgn,
  
  /** attribute assigned to effective membership assignment */
  any_mem_asgn,
  
  /** attribute assigned to an immediate membership assignment */
  imm_mem_asgn,
  
  /** attribute assigned to an attribute def assignment */
  attr_def_asgn;

  /**
   * if assignment is to a group
   * @return true if to group
   */
  public boolean isGroup() {
    return this == group;
  }
  
  /**
   * if assignment is to a group
   * @return true if to group
   */
  public boolean isEffectiveMembership() {
    return this == any_mem;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignType.class, 
        string, exceptionOnNull);
  }

}
