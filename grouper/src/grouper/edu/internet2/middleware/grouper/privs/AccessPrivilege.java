/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperAccessAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/** 
 * An instance of a granted access privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessPrivilege.java,v 1.12 2009-09-25 16:13:45 tzeller Exp $
 */
public class AccessPrivilege implements GrouperPrivilege, Comparable {

  /**
   * filter some privs for access privs
   * @param privileges
   * @return a new set of privs
   */
  public static Set<Privilege> filter(Collection<Privilege> privileges) {
    
    if (privileges == null) {
      return null;
    }
    Set<Privilege> result = new LinkedHashSet<Privilege>();
    for (Privilege privilege : privileges) {
      if (Privilege.isAccess(privilege)) {
        result.add(privilege);
      }
    }
    return result;
  }
  
  /** */
  public static final Privilege ADMIN   = Privilege.getInstance("admin");
  
  /** */
  public static final Privilege OPTIN   = Privilege.getInstance("optin");
  
  /** */
  public static final Privilege OPTOUT  = Privilege.getInstance("optout");
  
  /** */
  public static final Privilege READ    = Privilege.getInstance("read");
  
  /** */
  public static final Privilege SYSTEM  = Privilege.getInstance("system");
  
  /** */
  public static final Privilege UPDATE  = Privilege.getInstance("update");

  /** */
  public static final Privilege VIEW    = Privilege.getInstance("view");
  
  /** */
  public static final Privilege GROUP_ATTR_READ    = Privilege.getInstance("groupAttrRead");

  /** */
  public static final Privilege GROUP_ATTR_UPDATE    = Privilege.getInstance("groupAttrUpdate");
  
  /** any of these constitutes VIEW on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> VIEW_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(VIEW, READ, ADMIN, UPDATE, GROUP_ATTR_READ, GROUP_ATTR_UPDATE, OPTIN, OPTOUT));
  
  /** any of these constitutes GROUP_ATTR_READ on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> ATTRIBUTE_READ_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(GROUP_ATTR_READ, ADMIN));
  
  /** any of these constitutes GROUP_ATTR_UPDATE on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> ATTRIBUTE_UPDATE_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(GROUP_ATTR_UPDATE, ADMIN));

  /** any of these constitutes UPDATE on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> UPDATE_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(UPDATE, ADMIN));


  /** any of these constitutes VIEW on an entity
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> VIEW_ENTITY_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(VIEW, ADMIN, GROUP_ATTR_READ, GROUP_ATTR_UPDATE));

  /** convert a list to priv */
  private static Map<String,Privilege> list2priv = new HashMap<String, Privilege>();

  static {
    list2priv.put( Field.FIELD_NAME_ADMINS,  AccessPrivilege.ADMIN);
    list2priv.put( Field.FIELD_NAME_OPTINS,  AccessPrivilege.OPTIN);
    list2priv.put( Field.FIELD_NAME_OPTOUTS, AccessPrivilege.OPTOUT);
    list2priv.put( Field.FIELD_NAME_READERS, AccessPrivilege.READ);
    list2priv.put( Field.FIELD_NAME_UPDATERS, AccessPrivilege.UPDATE);
    list2priv.put( Field.FIELD_NAME_VIEWERS, AccessPrivilege.VIEW);
    list2priv.put( Field.FIELD_NAME_GROUP_ATTR_READERS, AccessPrivilege.GROUP_ATTR_READ);
    list2priv.put( Field.FIELD_NAME_GROUP_ATTR_UPDATERS, AccessPrivilege.GROUP_ATTR_UPDATE);
  }

  /** convert a list to a priv */
  private static Map<Privilege, String> priv2list = new HashMap<Privilege, String>();

  static {
    priv2list.put(  AccessPrivilege.ADMIN , Field.FIELD_NAME_ADMINS    );
    priv2list.put(  AccessPrivilege.OPTIN , Field.FIELD_NAME_OPTINS    );
    priv2list.put(  AccessPrivilege.OPTOUT, Field.FIELD_NAME_OPTOUTS   );
    priv2list.put(  AccessPrivilege.READ  , Field.FIELD_NAME_READERS   );
    priv2list.put(  AccessPrivilege.UPDATE, Field.FIELD_NAME_UPDATERS  );
    priv2list.put(  AccessPrivilege.VIEW  , Field.FIELD_NAME_VIEWERS   );
    priv2list.put(  AccessPrivilege.GROUP_ATTR_READ  , Field.FIELD_NAME_GROUP_ATTR_READERS   );
    priv2list.put(  AccessPrivilege.GROUP_ATTR_UPDATE  , Field.FIELD_NAME_GROUP_ATTR_UPDATERS   );
  }
  
  /**
   * convert a list to a privilege
   * @param list
   * @return the privilege
   */
  public static Privilege listToPriv(String list) {
    return list2priv.get(list);
  }

  /**
   * convert a privilege to a list
   * @param privilege
   * @return the list name
   */
  public static String privToList(Privilege privilege) {
    String listName = priv2list.get(privilege);
    return listName;
  }


  /** any of these constitutes READ on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> READ_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(READ, ADMIN));
  
  /** any of these constitutes MANAGE on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> MANAGE_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(ADMIN, UPDATE, GROUP_ATTR_UPDATE));

  /** */
  private Group   group;

  /** */
  private boolean isRevokable;
  
  /** */
  private String  klass;
  
  /** */
  private String  name;
  
  /** */
  private Subject owner;
  
  /** */
  private Subject subj;

  /** optionally link the audit id with the low level action */
  private String contextId;

  /**
   * 
   * @param group
   * @param subj
   * @param owner
   * @param priv
   * @param klass
   * @param isRevokable
   */
  public AccessPrivilege(
    Group   group , Subject subj,   Subject owner, 
    Privilege priv, String  klass,  boolean isRevokable, String contextId1
  ) 
  {
    this.group        = group;
    this.isRevokable  = isRevokable;
    if (!StringUtils.equals(GrouperAccessAdapter.class.getName(), klass)) {
      throw new RuntimeException("Why is this constructor called with " + klass + ", shouldnt it be: " + GrouperAccessAdapter.class.getName());
    }
    this.klass        = klass;
    this.name         = priv.toString();
    this.owner        = owner;
    this.subj         = subj;
    this.contextId = contextId1;
  } // public AccessPrivilege(object, subj, owner, priv, klass, isRevokable)

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AccessPrivilege)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.group, ( (AccessPrivilege) other ).group )
      .append( this.name, ( (AccessPrivilege) other ).name )
      .append( this.subj, ( (AccessPrivilege) other ).subj )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @return hashcode
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
    .append( this.group )
    .append( this.name )
    .append( this.subj )
      .toHashCode();
  } // public int hashCode()

  /**
   * get the privilege, convert from name
   * @return named Privilege
   */
  public Privilege getPrivilege() {
    return Privilege.getInstance(this.name);
  }
  

  // Public Instance Methods

  /**
   * Get {@link Group} that the privilege was granted on.
   * <p/>
   * @return  {@link Group}
   */
  public Group getGroup() {
    return this.group;
  } // public Group getGroup()

  /**
   * Get name of implementation class for this privilege type.
   * @return  Class name of implementing class.
   */
  public String getImplementationName() {
    return this.klass;
  } // public String getImplementationName()

  /**
   * Get name of privilege.
   * @return  Name of privilege.
   */
  public String getName() {
    return this.name;
  } // public String getName()

  /**
   * Get subject which was granted privilege on this object.
   * @return  {@link Subject} that was granted privilege.
   */
  public Subject getOwner() {
    return this.owner;
  } // public Subject getOwner()

  /**
   * Get subject which has this privilege.
   * @return  {@link Subject} that has this privilege.
   */
  public Subject getSubject() {
    return this.subj;
  } // public Subject getSubject()

  /**
   * Returns true if privilege can be revoked.
   * @return  Boolean true if privilege can be revoked.
   */
  public boolean isRevokable() {
    return this.isRevokable;
  } // public boolean isRevokable()

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
           .append("name"           , this.getName()                )
           .append("implementation" , this.getImplementationName()  )
           .append("revokable"      , this.isRevokable()            ) 
           .append("group"          , this.getGroup()               )
           .append("subject"        , this.getSubject()             )
           .append("owner"          , this.getOwner()               )
           .toString(); 
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.privs.GrouperPrivilege#getGrouperApi()
   */
  public GrouperAPI getGrouperApi() {
    return this.getGroup();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.GrouperPrivilege#getType()
   */
  public String getType() {
    return "access";
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (o == null || (!(o instanceof AccessPrivilege))) {
      return -1;
    }
    AccessPrivilege that = (AccessPrivilege)o;
    
    //dont use source since might be down
    String thisSubjectId = this.subj == null ? null : this.subj.getId();
    String thatSubjectId = that.subj == null ? null : that.subj.getId();
    
    return new CompareToBuilder()
      .append(this.group, that.group)
      .append(thisSubjectId, thatSubjectId)
      .append(this.name, that.name)
      .toComparison();
  }

  /**
   * optionally link the audit id with the low level action
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }
  
  /**
   * @see GrouperPrivilege#internalSetSubject(Subject)
   */
  public void internalSetSubject(Subject subject) {
    this.subj = subject;
  }

  /**
   * Get all privilege names.
   * 
   * @return the set of privilege names
   */
  public static Set<String> getAllPrivilegeNames() {
    return list2priv.keySet();
  }

}

