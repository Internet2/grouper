package edu.internet2.middleware.grouper.ldap;

import javax.naming.directory.SearchControls;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * ldap search scope
 * @author mchyzer
 *
 */
public enum LdapSearchScope {
  
  /** search an object */
  OBJECT_SCOPE {

    /**
     * @see LdapSearchScope#getSeachControlsConstant()
     */
    @Override
    public int getSeachControlsConstant() {
      return SearchControls.OBJECT_SCOPE;
    }
  }, 
  
  /** search in the one level DN of the tree */
  ONELEVEL_SCOPE {

    /**
     * @see LdapSearchScope#getSeachControlsConstant()
     */
    @Override
    public int getSeachControlsConstant() {
      return SearchControls.ONELEVEL_SCOPE;
    }
  }, 
  
  /** search in subtrees of the DN of the tree */
  SUBTREE_SCOPE {

    /**
     * @see LdapSearchScope#getSeachControlsConstant()
     */
    @Override
    public int getSeachControlsConstant() {
      return SearchControls.SUBTREE_SCOPE;
    }
  };

  /**
   * 
   * @return the constant
   */
  public abstract int getSeachControlsConstant();
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static LdapSearchScope valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(LdapSearchScope.class, 
        string, exceptionOnNull);
  
  }

  
}
