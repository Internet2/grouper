/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
