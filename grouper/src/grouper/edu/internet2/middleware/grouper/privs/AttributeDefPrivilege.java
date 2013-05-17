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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/** 
 * An instance of a granted access privilege to attribute.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AttributeDefPrivilege.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 */
public class AttributeDefPrivilege implements GrouperPrivilege, Comparable {

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
      if (Privilege.isAttributeDef(privilege)) {
        result.add(privilege);
      }
    }
    return result;
  }
  
  /** */
  public static final Privilege ATTR_ADMIN   = Privilege.getInstance("attrAdmin");
  
  /** */
  public static final Privilege ATTR_OPTIN   = Privilege.getInstance("attrOptin");
  
  /** */
  public static final Privilege ATTR_OPTOUT  = Privilege.getInstance("attrOptout");
  
  /** */
  public static final Privilege ATTR_READ    = Privilege.getInstance("attrRead");
  
  /** */
  public static final Privilege ATTR_UPDATE  = Privilege.getInstance("attrUpdate");

  /** */
  public static final Privilege ATTR_VIEW    = Privilege.getInstance("attrView");

  /** */
  public static final Privilege ATTR_DEF_ATTR_READ    = Privilege.getInstance("attrDefAttrRead");

  /** */
  public static final Privilege ATTR_DEF_ATTR_UPDATE    = Privilege.getInstance("attrDefAttrUpdate");
  
  /** any of these constitutes VIEW on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> VIEW_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(ATTR_VIEW, ATTR_READ, ATTR_ADMIN, ATTR_UPDATE, ATTR_DEF_ATTR_READ, ATTR_DEF_ATTR_UPDATE, ATTR_OPTIN, ATTR_OPTOUT));

  /** convert a list to priv */
  private static Map<String,Privilege> list2priv = new HashMap<String, Privilege>();

  static {
    list2priv.put( "attrAdmins",  AttributeDefPrivilege.ATTR_ADMIN);
    list2priv.put( "attrOptins",  AttributeDefPrivilege.ATTR_OPTIN);
    list2priv.put( "attrOptouts", AttributeDefPrivilege.ATTR_OPTOUT);
    list2priv.put( "attrReaders", AttributeDefPrivilege.ATTR_READ);
    list2priv.put( "attrUpdaters", AttributeDefPrivilege.ATTR_UPDATE);
    list2priv.put( "attrViewers", AttributeDefPrivilege.ATTR_VIEW);
    list2priv.put( "attrDefAttrReaders", AttributeDefPrivilege.ATTR_DEF_ATTR_READ);
    list2priv.put( "attrDefAttrUpdaters", AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE);
  }

  /** convert a list to a priv */
  private static Map<Privilege, String> priv2list = new HashMap<Privilege, String>();

  static {
    priv2list.put(  AttributeDefPrivilege.ATTR_ADMIN , "attrAdmins"    );
    priv2list.put(  AttributeDefPrivilege.ATTR_OPTIN , "attrOptins"    );
    priv2list.put(  AttributeDefPrivilege.ATTR_OPTOUT, "attrOptouts"   );
    priv2list.put(  AttributeDefPrivilege.ATTR_READ  , "attrReaders"   );
    priv2list.put(  AttributeDefPrivilege.ATTR_UPDATE, "attrUpdaters"  );
    priv2list.put(  AttributeDefPrivilege.ATTR_VIEW  , "attrViewers"   );
    priv2list.put(  AttributeDefPrivilege.ATTR_DEF_ATTR_READ  , "attrDefAttrReaders"   );
    priv2list.put(  AttributeDefPrivilege.ATTR_DEF_ATTR_UPDATE  , "attrDefAttrUpdaters"   );
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
      GrouperUtil.toSet(ATTR_READ, ATTR_ADMIN, ATTR_OPTIN, ATTR_OPTOUT));
  
  /** any of these constitutes MANAGE on a group
   * note, keep most common/likely privs toward the front  */
  public static Set<Privilege> MANAGE_PRIVILEGES = Collections.unmodifiableSet(
      GrouperUtil.toSet(ATTR_ADMIN, ATTR_UPDATE));

  /** */
  private AttributeDef attributeDef;

  /** */
  private boolean revokable;
  
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
   * @param attributeDef1
   * @param subj1
   * @param owner1
   * @param priv
   * @param klass1
   * @param isRevokable1
   * @param contextId1 
   */
  public AttributeDefPrivilege(
    AttributeDef   attributeDef1 , Subject subj1,   Subject owner1, 
    Privilege priv, String  klass1,  boolean isRevokable1, String contextId1
  ) 
  {
    this.attributeDef        = attributeDef1;
    this.revokable  = isRevokable1;
    this.klass        = klass1;
    this.name         = priv.toString();
    this.owner        = owner1;
    this.subj         = subj1;
    this.contextId = contextId1;
  } 

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AttributeDefPrivilege)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.attributeDef, ( (AttributeDefPrivilege) other ).attributeDef )
      .append( this.name, ( (AttributeDefPrivilege) other ).name )
      .append( this.subj, ( (AttributeDefPrivilege) other ).subj )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @return hashcode
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
    .append( this.attributeDef )
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
   * Get {@link AttributeDef} that the privilege was granted on.
   * <p/>
   * @return  {@link AttributeDef}
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

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
    return this.revokable;
  } 

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
           .append("attributeDef"   , this.getAttributeDef()        )
           .append("subject"        , this.getSubject()             )
           .append("owner"          , this.getOwner()               )
           .toString(); 
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.GrouperPrivilege#getGrouperApi()
   */
  public GrouperAPI getGrouperApi() {
    return this.getAttributeDef();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.GrouperPrivilege#getType()
   */
  public String getType() {
    return "attributeDef";
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (o == null || (!(o instanceof AttributeDefPrivilege))) {
      return -1;
    }
    AttributeDefPrivilege that = (AttributeDefPrivilege)o;
    
    //dont use source since might be down
    String thisSubjectId = this.subj == null ? null : this.subj.getId();
    String thatSubjectId = that.subj == null ? null : that.subj.getId();
    
    return new CompareToBuilder()
      .append(this.attributeDef, that.attributeDef)
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
  
  
  
  

}

