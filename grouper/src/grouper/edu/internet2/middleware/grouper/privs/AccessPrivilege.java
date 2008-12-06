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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.subject.Subject;


/** 
 * An instance of a granted access privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessPrivilege.java,v 1.5 2008-12-06 20:39:36 mchyzer Exp $
 */
public class AccessPrivilege implements GrouperPrivilege, Comparable {


  // Public Class Constants
  public static final Privilege ADMIN   = Privilege.getInstance("admin");
  public static final Privilege OPTIN   = Privilege.getInstance("optin");
  public static final Privilege OPTOUT  = Privilege.getInstance("optout");
  public static final Privilege READ    = Privilege.getInstance("read");
  public static final Privilege SYSTEM  = Privilege.getInstance("system");
  public static final Privilege UPDATE  = Privilege.getInstance("update");
  public static final Privilege VIEW    = Privilege.getInstance("view");


  // Private Instance Variables
  private Group   group;
  private boolean isRevokable;
  private String  klass;
  private String  name;
  private Subject owner;
  private Subject subj;


  // Constructors
  public AccessPrivilege(
    Group   group , Subject subj,   Subject owner, 
    Privilege priv, String  klass,  boolean isRevokable
  ) 
  {
    this.group        = group;
    this.isRevokable  = isRevokable;
    this.klass        = klass;
    this.name         = priv.toString();
    this.owner        = owner;
    this.subj         = subj;
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
  
  

}

