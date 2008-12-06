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
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.subject.Subject;

/** 
 * An instance of a granted naming privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NamingPrivilege.java,v 1.4 2008-12-06 20:39:36 mchyzer Exp $
 */
public class NamingPrivilege implements GrouperPrivilege, Comparable {

  // Public Class Constants
  public static final Privilege CREATE  = Privilege.getInstance("create");
  public static final Privilege STEM    = Privilege.getInstance("stem");

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof NamingPrivilege)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.stem, ( (NamingPrivilege) other ).stem )
      .append( this.name, ( (NamingPrivilege) other ).name )
      .append( this.subj, ( (NamingPrivilege) other ).subj )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (o == null || (!(o instanceof NamingPrivilege))) {
      return -1;
    }
    NamingPrivilege that = (NamingPrivilege)o;
    
    //dont use source since might be down
    String thisSubjectId = this.subj == null ? null : this.subj.getId();
    String thatSubjectName = that.subj == null ? null : that.subj.getId();
    
    return new CompareToBuilder()
      .append(this.stem, that.stem)
      .append(thisSubjectId, thatSubjectName)
      .append(this.name, that.name)
      .toComparison();
  }

  /**
   * @return hashcode
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
    .append( this.stem )
    .append( this.name )
    .append( this.subj )
      .toHashCode();
  } // public int hashCode()


  // Private Instance Variables
  private boolean isRevokable;
  private String  klass;
  private String  name;
  private Stem    stem;
  private Subject owner;
  private Subject subj;


  // Constructors
  public NamingPrivilege(
    Stem      stem, Subject subj,   Subject owner, 
    Privilege priv, String  klass,  boolean isRevokable
  ) 
  {
    this.isRevokable  = isRevokable;
    this.klass        = klass;
    this.name         = priv.toString();
    this.owner        = owner;
    this.stem         = stem;
    this.subj         = subj;
  } // public NamingPrivilege(object, subj, owner, priv, klass, isRevokable)


  // Public Instance Methods

  /**
   * Get name of implementation class for this privilege type.
   * @return  Class name of implementing class.
   */
  public String getImplementationName() {
    return this.klass;
  } // public String getImplementationName()

  /**
   * Returns true if privilege can be revoked.
   * @return  Boolean true if privilege can be revoked.
   */
  public boolean isRevokable() {
    return this.isRevokable;
  } // public boolean isRevokable()

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
   * Get object {@link Stem} that the privilege was
   * granted on.
   * <p/>
   * @return  {@link Stem} object.
   */
  public Stem getStem() {
    return this.stem;
  } // public Object getStem()

  /**
   * Get subject which has this privilege.
   * @return  {@link Subject} that has this privilege.
   */
  public Subject getSubject() {
    return this.subj;
  } // public Subject getSubject()

  public String toString() {
    return new ToStringBuilder(this)
           .append("name"           , this.getName()                )
           .append("implementation" , this.getImplementationName()  )
           .append("revokable"      , this.isRevokable()            ) 
           .append("stem"           , this.getStem()                )
           .append("subject"        , this.getSubject()             )
           .append("owner"          , this.getOwner()               )
           .toString(); 
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.privs.GrouperPrivilege#getGrouperApi()
   */
  public GrouperAPI getGrouperApi() {
    return this.getStem();
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.GrouperPrivilege#getType()
   */
  public String getType() {
    return "naming";
  }

}

