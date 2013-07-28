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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * An instance of a granted naming privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NamingPrivilege.java,v 1.8 2009-04-13 20:24:29 mchyzer Exp $
 */
public class NamingPrivilege implements GrouperPrivilege, Comparable {

  /** can create objects in this stem */
  public static final Privilege CREATE  = Privilege.getInstance("create");

  /** can create stems in this stem */
  public static final Privilege STEM    = Privilege.getInstance("stem");

  /** can read attributes on this stem */
  public static final Privilege STEM_ATTR_READ    = Privilege.getInstance("stemAttrRead");

  /** can update attributes on this stem */
  public static final Privilege STEM_ATTR_UPDATE    = Privilege.getInstance("stemAttrUpdate");
  
  /** any of these constitutes CREATE on a stem
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> CREATE_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(CREATE, STEM));
  
  /** any of these constitutes STEM_ATTR_READ on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> ATTRIBUTE_READ_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(STEM, CREATE, STEM_ATTR_READ));
  
  /** any of these constitutes STEM_ATTR_UPDATE on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> ATTRIBUTE_UPDATE_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(STEM, CREATE, STEM_ATTR_UPDATE));

  /** convert a list to priv */
  private static Map<String,Privilege> list2priv = new HashMap<String, Privilege>();

  static {
    list2priv.put( Field.FIELD_NAME_CREATORS,  NamingPrivilege.CREATE);
    list2priv.put( Field.FIELD_NAME_STEMMERS,  NamingPrivilege.STEM);
    list2priv.put( Field.FIELD_NAME_STEM_ATTR_READERS,  NamingPrivilege.STEM_ATTR_READ);
    list2priv.put( Field.FIELD_NAME_STEM_ATTR_UPDATERS,  NamingPrivilege.STEM_ATTR_UPDATE);
  }

  /** convert a list to a priv */
  private static Map<Privilege, String> priv2list = new HashMap<Privilege, String>();

  static {
    priv2list.put(  NamingPrivilege.CREATE , Field.FIELD_NAME_CREATORS    );
    priv2list.put(  NamingPrivilege.STEM , Field.FIELD_NAME_STEMMERS    );
    priv2list.put(  NamingPrivilege.STEM_ATTR_READ , Field.FIELD_NAME_STEM_ATTR_READERS    );
    priv2list.put(  NamingPrivilege.STEM_ATTR_UPDATE , Field.FIELD_NAME_STEM_ATTR_UPDATERS    );
  }
 
  /**
   * convert a privilege to a list
   * @param privilege
   * @return the list name
   */
  public static String privToList(Privilege privilege) {
    return priv2list.get(privilege);
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
  /**
   * optionally link the audit id with the low level action 
   */
  private String contextId;


  // Constructors
  public NamingPrivilege(
    Stem      stem, Subject subj,   Subject owner, 
    Privilege priv, String  klass,  boolean isRevokable, String contextId1
  ) 
  {
    this.isRevokable  = isRevokable;
    this.klass        = klass;
    this.name         = priv.toString();
    this.owner        = owner;
    this.stem         = stem;
    this.subj         = subj;
    this.contextId = contextId1;
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
  
  

}

