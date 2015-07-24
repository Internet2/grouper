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
package edu.internet2.middleware.grouper.privs;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * enum of privilege types
 */
public enum PrivilegeType {
  
  /** access privileges are for groups */
  ACCESS("access") {
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
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.GROUP_ATTR_READ.getName())) {
        return AccessPrivilege.GROUP_ATTR_READ;
      }
      if (StringUtils.equalsIgnoreCase(name, AccessPrivilege.GROUP_ATTR_UPDATE.getName())) {
        return AccessPrivilege.GROUP_ATTR_UPDATE;
      }
      if (StringUtils.isBlank(name)) {
        return null;
      }
      throw new RuntimeException("Cant find access privilege name '" + name + "'");
    }
  },

  /** naming privileges are for stems */
  NAMING("naming") {
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
      if (StringUtils.equalsIgnoreCase(name, NamingPrivilege.STEM.getName()) || 
          StringUtils.equalsIgnoreCase(name, NamingPrivilege.STEM_ADMIN.getName())) {
        return NamingPrivilege.STEM_ADMIN;
      }
      if (StringUtils.equalsIgnoreCase(name, NamingPrivilege.STEM_ATTR_READ.getName())) {
        return NamingPrivilege.STEM_ATTR_READ;
      }
      if (StringUtils.equalsIgnoreCase(name, NamingPrivilege.STEM_ATTR_UPDATE.getName())) {
        return NamingPrivilege.STEM_ATTR_UPDATE;
      }
      if (StringUtils.isBlank(name)) {
        return null;
      }
      throw new RuntimeException("Cant find access privilege name '" + name + "'");
    }
  };

  /**
   * privilege name
   */
  private String privilegeName = null;
  
  /**
   * construct
   * @param thePrivilegeName
   */
  private PrivilegeType(String thePrivilegeName) {
    this.privilegeName = thePrivilegeName;
  }
  
  /**
   * privilege name
   * @return privilege name
   */
  public String getPrivilegeName() {
    return this.privilegeName;
  }
  
  /**
   * retrieve a privilege with this name.  should return null if blank
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
  public static PrivilegeType valueOfIgnoreCase(String string) {
    return GrouperUtil.enumValueOfIgnoreCase(PrivilegeType.class,string, false );
  }
  
}
