/**
 * 
 */
package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * what type of membership this is, e.g.: list, groupPrivilege, stemPrivilege, attributeDefPrivilege
 * 
 * @author mchyzer
 */
public enum MembershipOwnerType {

  /**
   * membership list (generally this is the members list
   */
  list, 

  /**
   * group access privilege, e.g. READ or ADMIN
   */
  groupPrivilege, 

  /**
   * stem naming privilege, e.g. stem, create
   */
  stemPrivilege, 

  /**
   * attribute definition privilege
   */
  attributeDefPrivilege;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static MembershipOwnerType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    return GrouperUtil.enumValueOfIgnoreCase(MembershipOwnerType.class, 
        string, exceptionOnNull);
  }

  
  
}
