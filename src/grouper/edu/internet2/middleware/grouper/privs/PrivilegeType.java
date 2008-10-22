package edu.internet2.middleware.grouper.privs;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * enum of privilege types
 */
public enum PrivilegeType {
  
  /** access privileges are for groups */
  ACCESS {
    /**
     * retrieve a privilege with this name
     * @param name
     * @return the privilege
     */
    @Override
    public Privilege retrievePrivilege(String name) {
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.READ.getName())) {
        return AccessPrivilege.READ;
      }
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.VIEW.getName())) {
        return AccessPrivilege.VIEW;
      }
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.UPDATE.getName())) {
        return AccessPrivilege.UPDATE;
      }
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.ADMIN.getName())) {
        return AccessPrivilege.ADMIN;
      }
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.OPTIN.getName())) {
        return AccessPrivilege.OPTIN;
      }
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.OPTOUT.getName())) {
        return AccessPrivilege.OPTOUT;
      }
      if (StringUtils.isBlank(name)) {
        return null;
      }
      throw new RuntimeException("Cant find access privilege name '" + name + "'");
    }
  },

  /** naming privileges are for stems */
  NAMING {
    /**
     * retrieve a privilege with this name
     * @param name
     * @return the privilege
     */
    @Override
    public Privilege retrievePrivilege(String name) {
      if (StringUtils.equalsIgnoreCase(name, NamingPrivilege.CREATE.getName())) {
        return NamingPrivilege.CREATE;
      }
      if (StringUtils.equalsIgnoreCase(name, NamingPrivilege.STEM.getName())) {
        return NamingPrivilege.STEM;
      }
      throw new RuntimeException("Cant find access privilege name '" + name + "'");
    }
  };

  /**
   * retrieve a privilege with this name
   * @param name
   * @return the privilege
   */
  public abstract Privilege retrievePrivilege(String name);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static GrouperTransactionType valueOfIgnoreCase(String string) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperTransactionType.class,string, false );
  }
  
}