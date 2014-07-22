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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.privs;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/** 
 * Privilege schema specification.  Access the constants for Groups from AccessPrivilege
 * and Stems from NamingPrivilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.9 2009-09-21 06:14:26 mchyzer Exp $
 */
public class Privilege implements Serializable {

  /**
   * see if privilege involves group
   * @return if involves groups
   */
  public boolean isAccess() {
    return Privilege.isAccess(this);
  }
  
  /**
   * see if privilege involves stem
   * @return if involves stems
   */
  public boolean isNaming() {
    return Privilege.isNaming(this);
  }
  
  /**
   * see if privilege involves attribute def
   * @return if involves attribute def
   */
  public boolean isAttributeDef() {
    return Privilege.isAttributeDef(this);
  }
  

  /**
   * convert a list to a privilege for any type of privilege
   * @param list
   * @return the privilege
   */
  public static Privilege listToPriv(String list, boolean exceptionOnNotFound) {
    
    Privilege privilege = AccessPrivilege.listToPriv(list);
    
    if (privilege != null) {
      return privilege;
    }
    
    privilege = NamingPrivilege.listToPriv(list);
    
    if (privilege != null) {
      return privilege;
    }
    
    privilege = AttributeDefPrivilege.listToPriv(list);
    
    if (privilege != null) {
      return privilege;
    }
    
    if (exceptionOnNotFound) {
      throw new RuntimeException("Cant find privilege from field: " + list);
    }
    
    return null;
    
    
  }

  /**
   * convert a list to a privilege for any type of privilege
   * @param fields
   * @return the privilege
   */
  public static Set<Privilege> convertFieldsToPrivileges(Collection<Field> fields) {
    
    if (fields == null) {
      return null;
    }
    
    Set<Privilege> privileges = new LinkedHashSet<Privilege>();
    
    for (Field field : fields) {
      Privilege privilege = listToPriv(field.getName(), true);
      privileges.add(privilege);
    }
    return privileges;
    
  }

  /**
   * convert a list of privilege names or field names to a privilege for any type of privilege
   * @param privilegeNames
   * @return the privilege
   */
  public static Set<Privilege> convertNamesToPrivileges(Collection<String> privilegeNames) {
    
    if (privilegeNames == null) {
      return null;
    }
    
    Set<Privilege> privileges = new LinkedHashSet<Privilege>();
    
    for (String privilegeName : privilegeNames) {
      Privilege privilege = getInstance(privilegeName, true);
      privileges.add(privilege);
    }
    return privileges;
    
  }

  /**
   * convert a collection of privileges to a collection of fields
   * @param privileges
   * @return the fields
   */
  public static Collection<Field> convertPrivilegesToFields(Collection<Privilege> privileges) {
    
    Set<Field> result = new HashSet<Field>();
    
    for (Privilege privilege : GrouperUtil.nonNull(privileges)) {
      
      Field field = privilege.getField();
      result.add(field);
      
    }
    
    return result;
    
  }
  

  /** constant */
  public static final long serialVersionUID = 931658631999330719L;

  /**
   * generate hash code
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  /**
   * string equals
   * @return if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Privilege other = (Privilege) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  
  
  //get references to these from other classes, e.g. AccessPrivilege.READ, or NamingPrivilege
  // PRIVATE CLASS CONSTANTS //
  /** */
  private static final Set<Privilege>         ATTRIBUTE_DEF  = new LinkedHashSet<Privilege>();
  /** */
  private static final Set<Privilege>         ACCESS  = new LinkedHashSet<Privilege>();
  /** */
  private static final Privilege              CREATE  = new Privilege("create");
  /** */
  private static final Privilege              STEM    = new Privilege("stem"  );  
  /** */
  private static final Privilege              STEM_ATTR_READ    = new Privilege("stemAttrRead"  );
  /** */
  private static final Privilege              STEM_ATTR_UPDATE    = new Privilege("stemAttrUpdate"  );
  /** */
  private static final Set<Privilege>         NAMING  = new LinkedHashSet<Privilege>();
  /** */
  private static final Privilege              OPTIN   = new Privilege("optin" );
  /** */
  private static final Privilege              OPTOUT  = new Privilege("optout");
  /** */
  private static final Privilege              READ    = new Privilege("read"  );
  /** */
  private static final Privilege              ADMIN   = new Privilege("admin" );
  /** */
  private static final Privilege              UPDATE  = new Privilege("update");
  /** */
  private static final Privilege              VIEW    = new Privilege("view"  );
  /** */
  private static final Privilege              GROUP_ATTR_READ    = new Privilege("groupAttrRead"  );
  /** */
  private static final Privilege              GROUP_ATTR_UPDATE    = new Privilege("groupAttrUpdate"  );
  /** key is priv name to lower case */
  private static final Map<String,Privilege>  PRIVS   = new HashMap<String,Privilege>();

  /** */
  private static final Privilege              ATTR_OPTIN   = new Privilege("attrOptin" );
  /** */
  private static final Privilege              ATTR_OPTOUT  = new Privilege("attrOptout");
  /** */
  private static final Privilege              ATTR_READ    = new Privilege("attrRead"  );
  /** */
  private static final Privilege              ATTR_ADMIN   = new Privilege("attrAdmin" );
  /** */
  private static final Privilege              ATTR_UPDATE  = new Privilege("attrUpdate");
  /** */
  private static final Privilege              ATTR_VIEW    = new Privilege("attrView"  );
  /** */
  private static final Privilege              ATTR_DEF_ATTR_READ  = new Privilege("attrDefAttrRead");
  /** */
  private static final Privilege              ATTR_DEF_ATTR_UPDATE    = new Privilege("attrDefAttrUpdate"  );
  /** */
  private static final Privilege              SYSTEM  = new Privilege("system");

  /** */
  private String name;

  /**
   * get the inherited privileges for this privilege (including this privilege
   * for instance if the privilege is UPDATE, then return UPDATE and ADMIN
   * @return the inherited privileges
   *
   */
  public Collection<Privilege> getInheritedPrivileges() {
    
    if (this.equals(ADMIN)) {
      return AccessPrivilege.ADMIN_PRIVILEGES;
    }
    
    if (this.equals(VIEW)) {
      return AccessPrivilege.VIEW_PRIVILEGES;
    }
    
    if (this.equals(READ)) {
      return AccessPrivilege.READ_PRIVILEGES;
    }
    
    if (this.equals(UPDATE)) {
      return AccessPrivilege.UPDATE_PRIVILEGES;
    }
    
    if (this.equals(OPTIN)) {
      return AccessPrivilege.OPTIN_PRIVILEGES;
    }
    
    if (this.equals(OPTOUT)) {
      return AccessPrivilege.OPTOUT_PRIVILEGES;
    }
    
    if (this.equals(GROUP_ATTR_READ)) {
      return AccessPrivilege.GROUP_ATTR_READ_PRIVILEGES;
    }
    
    if (this.equals(GROUP_ATTR_UPDATE)) {
      return AccessPrivilege.GROUP_ATTR_UPDATE_PRIVILEGES;
    }
    
    if (this.equals(STEM)) {
      return NamingPrivilege.STEM_PRIVILEGES;
    }
    
    if (this.equals(CREATE)) {
      return NamingPrivilege.CREATE_PRIVILEGES;
    }
    
    if (this.equals(STEM_ATTR_READ)) {
      return NamingPrivilege.STEM_ATTR_READ_PRIVILEGES;
    }
    
    if (this.equals(STEM_ATTR_UPDATE)) {
      return NamingPrivilege.STEM_ATTR_UPDATE_PRIVILEGES;
    }

    if (this.equals(ATTR_ADMIN)) {
      return AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES;
    }
    
    if (this.equals(ATTR_DEF_ATTR_READ)) {
      return AttributeDefPrivilege.ATTR_DEF_ATTR_READ_PRIVILEGES;
    }
    
    if (this.equals(ATTR_DEF_ATTR_UPDATE)) {
      return AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE_PRIVILEGES;
    }
    
    if (this.equals(ATTR_OPTIN)) {
      return AttributeDefPrivilege.ATTR_OPTIN_PRIVILEGES;
    }
    
    if (this.equals(ATTR_OPTOUT)) {
      return AttributeDefPrivilege.ATTR_OPTOUT_PRIVILEGES;
    }
    
    if (this.equals(ATTR_READ)) {
      return AttributeDefPrivilege.ATTR_READ_PRIVILEGES;
    }
    
    if (this.equals(ATTR_UPDATE)) {
      return AttributeDefPrivilege.ATTR_UPDATE_PRIVILEGES;
    }
    
    if (this.equals(ATTR_VIEW)) {
      return AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES;
    }

    throw new RuntimeException("Cant find privilege: " + this.getName());
    
  }

  /**
   * get the privilege that this privilege implied (including this privilege
   * for instance if the privilege is UPDATE, then return UPDATE and ADMIN
   * @return the inherited privileges
   *
   */
  public Collection<Privilege> getImpliedPrivileges() {
    
    if (this.equals(ADMIN)) {
      return AccessPrivilege.ADMIN_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(VIEW)) {
      return AccessPrivilege.VIEW_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(READ)) {
      return AccessPrivilege.READ_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(UPDATE)) {
      return AccessPrivilege.UPDATE_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(OPTIN)) {
      return AccessPrivilege.OPTIN_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(OPTOUT)) {
      return AccessPrivilege.OPTOUT_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(GROUP_ATTR_READ)) {
      return AccessPrivilege.GROUP_ATTR_READ_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(GROUP_ATTR_UPDATE)) {
      return AccessPrivilege.GROUP_ATTR_UPDATE_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(STEM)) {
      return NamingPrivilege.STEM_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(CREATE)) {
      return NamingPrivilege.CREATE_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(STEM_ATTR_READ)) {
      return NamingPrivilege.STEM_ATTR_READ_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(STEM_ATTR_UPDATE)) {
      return NamingPrivilege.STEM_ATTR_UPDATE_IMPLIED_PRIVILEGES;
    }

    if (this.equals(ATTR_ADMIN)) {
      return AttributeDefPrivilege.ATTR_ADMIN_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_DEF_ATTR_READ)) {
      return AttributeDefPrivilege.ATTR_DEF_ATTR_READ_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_DEF_ATTR_UPDATE)) {
      return AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_OPTIN)) {
      return AttributeDefPrivilege.ATTR_OPTIN_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_OPTOUT)) {
      return AttributeDefPrivilege.ATTR_OPTOUT_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_READ)) {
      return AttributeDefPrivilege.ATTR_READ_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_UPDATE)) {
      return AttributeDefPrivilege.ATTR_UPDATE_IMPLIED_PRIVILEGES;
    }
    
    if (this.equals(ATTR_VIEW)) {
      return AttributeDefPrivilege.ATTR_VIEW_IMPLIED_PRIVILEGES;
    }

    throw new RuntimeException("Cant find privilege: " + this.getName());
    
  }



  // STATIC //
  static {
    PRIVS.put(  ADMIN.toString().toLowerCase()  , ADMIN   );
    ACCESS.add( ADMIN                       );
    PRIVS.put(  CREATE.toString().toLowerCase() , CREATE  );
    NAMING.add( CREATE                      );
    PRIVS.put(  OPTIN.toString().toLowerCase()  , OPTIN   );
    ACCESS.add( OPTIN                       );
    PRIVS.put(  OPTOUT.toString().toLowerCase() , OPTOUT  );
    ACCESS.add( OPTOUT                      );
    PRIVS.put(  READ.toString().toLowerCase()   , READ    );
    ACCESS.add( READ                        );
    PRIVS.put(  STEM.toString().toLowerCase()   , STEM    );
    NAMING.add( STEM                        );
    PRIVS.put(  SYSTEM.toString().toLowerCase() , SYSTEM  );
    PRIVS.put(  UPDATE.toString().toLowerCase() , UPDATE  );
    ACCESS.add( UPDATE                      );
    PRIVS.put(  VIEW.toString().toLowerCase()   , VIEW    );
    ACCESS.add( VIEW                        );
    PRIVS.put(GROUP_ATTR_READ.toString().toLowerCase(), GROUP_ATTR_READ);
    ACCESS.add(GROUP_ATTR_READ);
    PRIVS.put(GROUP_ATTR_UPDATE.toString().toLowerCase(), GROUP_ATTR_UPDATE);
    ACCESS.add(GROUP_ATTR_UPDATE);
    PRIVS.put(STEM_ATTR_READ.toString().toLowerCase(), STEM_ATTR_READ);
    NAMING.add(STEM_ATTR_READ);
    PRIVS.put(STEM_ATTR_UPDATE.toString().toLowerCase(), STEM_ATTR_UPDATE);
    NAMING.add(STEM_ATTR_UPDATE);

    PRIVS.put(  ATTR_OPTIN.toString().toLowerCase()   , ATTR_OPTIN    );
    ATTRIBUTE_DEF.add( ATTR_OPTIN                        );
    PRIVS.put(  ATTR_OPTOUT.toString().toLowerCase()   , ATTR_OPTOUT    );
    ATTRIBUTE_DEF.add( ATTR_OPTOUT                        );
    PRIVS.put(  ATTR_READ.toString().toLowerCase()   , ATTR_READ    );
    ATTRIBUTE_DEF.add( ATTR_READ                        );
    PRIVS.put(  ATTR_UPDATE.toString().toLowerCase()   , ATTR_UPDATE    );
    ATTRIBUTE_DEF.add( ATTR_UPDATE                        );
    PRIVS.put(  ATTR_VIEW.toString().toLowerCase()   , ATTR_VIEW    );
    ATTRIBUTE_DEF.add( ATTR_VIEW                        );
    PRIVS.put(  ATTR_ADMIN.toString().toLowerCase()   , ATTR_ADMIN    );
    ATTRIBUTE_DEF.add( ATTR_ADMIN                        );
    PRIVS.put(ATTR_DEF_ATTR_READ.toString().toLowerCase(), ATTR_DEF_ATTR_READ);
    ATTRIBUTE_DEF.add(ATTR_DEF_ATTR_READ);
    PRIVS.put(ATTR_DEF_ATTR_UPDATE.toString().toLowerCase(), ATTR_DEF_ATTR_UPDATE);
    ATTRIBUTE_DEF.add(ATTR_DEF_ATTR_UPDATE);

  
  } // static


  /**
   * return the list name
   * @return the list name
   */
  public String getListName() {
    if (isAccess(this)) {
      return AccessPrivilege.privToList(this);
    }
    if (isNaming(this)) {
      return NamingPrivilege.privToList(this);
    }
    if (isAttributeDef(this)) {
      return AttributeDefPrivilege.privToList(this);
    }
    throw new RuntimeException("Invalid list: " + this);
  }
  
  /**
   * return the list name
   * @return the list name
   * @throws SchemaException 
   */
  public Field getField() throws SchemaException {
    String listName = this.getListName();
    if (!StringUtils.isBlank(listName)) {
      return FieldFinder.find(listName, true, false);
    }
    throw new SchemaException("invalid privilege: " + this);
  }

  /**
   * 
   * @param name
   */
  private Privilege(String name) {
    this.name = name;
  } // private Privilege(name)


  /**
   * 
   * @return access (group) privs
   */
  public static Set<Privilege> getAccessPrivs() {
    return ACCESS;
  } // public static Set getAccessPrivs()

  /**
   * 
   * @param namesCommaSeparated
   * @return the privileges
   */
  public static Set<Privilege> getInstances(String namesCommaSeparated) {
    String[] privilegesArray = GrouperUtil.splitTrim(namesCommaSeparated, ",");
    Set<Privilege> privileges = new LinkedHashSet<Privilege>();
    for (String privilegeString : privilegesArray) {
      Privilege privilege = getInstance(privilegeString);
      privileges.add(privilege);
    }
    return privileges;
  }
  
  /**
   * convert privileges to string comma separated
   * @param privileges
   * @return the privileges
   */
  public static String stringValue(Set<Privilege> privileges) {
    StringBuilder result = new StringBuilder();
    int i=0;
    for (Privilege privilege : privileges) {
      result.append(privilege.getName());
      if (i < privileges.size()-1) {
        result.append(", ");
      }
      i++;
    }
    return result.toString();
  }

  /**
   * 
   * @param name
   * @return priv
   */
  public static Privilege getInstance(String name) {
    return getInstance(name, false);
  }
  /**
   * 
   * @param name
   * @param exceptionIfNotFound
   * @return priv
   */
  public static Privilege getInstance(String name, boolean exceptionIfNotFound) {
    
    //all are upper case
    if (name != null) {
      name = name.toLowerCase();
    }
    Privilege privilege = PRIVS.get(name);
    
    //try list to make things more user friendly?
    if (privilege == null) {
      privilege = listToPriv(name, false);
    }
    
    if (privilege == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find privilege: " + name);
    }
    return privilege;
  } // public static Privilege getInstance(name)

  /**
   * get stem (naming) privs
   * @return set
   */
  public static Set<Privilege> getNamingPrivs() {
    return NAMING;
  }
  
  /**
   * get attribute def privs
   * @return attr def privs
   */
  public static Set<Privilege> getAttributeDefPrivs() {
    return ATTRIBUTE_DEF;
  }

  /**
   * 
   * @param p
   * @return if access
   */
  public static boolean isAccess(Privilege p) {
    if (ACCESS.contains(p)) {
      return true;
    }
    return false;
  }

  /**
   * 
   * @param p
   * @return if naming (stem)
   */
  public static boolean isNaming(Privilege p) {
    if (NAMING.contains(p)) {
      return true;
    }
    return false;
  }

  /**
   * 
   * @param p
   * @return if attribute def
   */
  public static boolean isAttributeDef(Privilege p) {
    return ATTRIBUTE_DEF.contains(p);
  }


  /**
   * 
   * @return name
   */
  public String getName() {
    return this.name;
  } // public String getName()

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getName();
  } // public String toString()


  /**
   * 
   * @return object
   */
  Object readResolve() {
    return getInstance(name);
  } // Object readResolve()

}

