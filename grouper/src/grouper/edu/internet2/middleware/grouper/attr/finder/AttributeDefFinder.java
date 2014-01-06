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
/**
 * @author mchyzer
 * $Id: AttributeDefFinder.java,v 1.2 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * finder methods for attribute def
 */
public class AttributeDefFinder {

  /**
   * find an attributeDef by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   * @throws AttributeDefNotFoundException
   */
  public static AttributeDef findById(String id, boolean exceptionIfNull) {
    
    
    AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByIdSecure(id, exceptionIfNull);
    
    return attributeDef;
    
  }
  
  /**
   * find an attributeDef by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @param queryOptions
   * @return the attribute def or null
   * @throws AttributeDefNotFoundException
   */
  public static AttributeDef findById(String id, boolean exceptionIfNull, QueryOptions queryOptions) {
    
    
    AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByIdSecure(id, exceptionIfNull, queryOptions);
    
    return attributeDef;
    
  }
  
  /**
   * find an attributeDef by attribute def name id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   * @throws AttributeDefNotFoundException
   */
  public static AttributeDef findByAttributeDefNameId(String id, boolean exceptionIfNull) {
    
    
    AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByAttributeDefNameIdSecure(id, exceptionIfNull);
    
    return attributeDef;
    
  }
  
  /**
   * find an attributeDef by name.  This is a secure method, a GrouperSession must be open
   * @param name of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @param queryOptions 
   * @return the attribute def or null
   * @throws AttributeDefNotFoundException
   */
  public static AttributeDef findByName(String name, boolean exceptionIfNull, QueryOptions queryOptions) {
    return GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(name, exceptionIfNull, queryOptions);
  }

  /**
   * find an attributeDef by name.  This is a secure method, a GrouperSession must be open
   * @param name of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   * @throws AttributeDefNotFoundException
   */
  public static AttributeDef findByName(String name, boolean exceptionIfNull) {
    return GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(name, exceptionIfNull);
  }

  /**
   * Find an attributeDef within the registry by ID index.
   * @param idIndex id index of attributeDef to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param queryOptions 
   * @return  A {@link AttributeDef}
   * @throws AttributeDefNotFoundException if not found an exceptionIfNotFound is true
   */
  public static AttributeDef findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound,  QueryOptions queryOptions) 
      throws AttributeDefNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(GrouperSession.staticGrouperSession());
    AttributeDef a = GrouperDAOFactory.getFactory().getAttributeDef().findByIdIndexSecure(idIndex, exceptionIfNotFound, queryOptions);
    return a;
  }

  /**
   * parent or ancestor stem of the attribute def
   */
  private String parentStemId;
  /**
   * find attribute definitions where the static grouper session has certain privileges on the results
   */
  private Set<Privilege> privileges;
  /**
   * if sorting or paging
   */
  private QueryOptions queryOptions;
  
  /**
   * scope to look for attribute defs  Wildcards will be appended or percent is the wildcard
   */
  private String scope;

  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScope;
  
  /**
   * if passing in a stem, this is the stem scope...
   */
  private Scope stemScope;
  
  /**
   * this is the subject that has certain privileges
   */
  private Subject subject;
  
  /**
   * if we are looking up an attribute def, only look by uuid or name
   */
  private boolean findByUuidOrName;

  /**
   * add a privilege to filter by that the subject has on the attribute definition
   * @param privilege should be AttributeDefPrivilege
   * @return this for chaining
   */
  public AttributeDefFinder addPrivilege(Privilege privilege) {
    
    if (this.privileges == null) {
      this.privileges = new HashSet<Privilege>();
    }
    
    this.privileges.add(privilege);
    
    return this;
  }

  /**
   * parent or ancestor stem of the attribute def
   * @param theParentStemId
   * @return this for chaining
   */
  public AttributeDefFinder assignParentStemId(String theParentStemId) {
    this.parentStemId = theParentStemId;
    return this;
  }

  /**
   * assign privileges to filter by that the subject has on the attribute definition
   * @param thePrivileges
   * @return this for chaining
   */
  public AttributeDefFinder assignPrivileges(Set<Privilege> thePrivileges) {
    this.privileges = thePrivileges;
    return this;
  }

  /**
   * if sorting, paging, caching, etc
   * @param theQueryOptions
   * @return this for chaining
   */
  public AttributeDefFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }

  /**
   * scope to look for attribute defs  Wildcards will be appended or percent is the wildcard
   * @param theScope
   * @return this for chaining
   */
  public AttributeDefFinder assignScope(String theScope) {
    this.scope = theScope;
    return this;
  }

  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScope
   * @return this for chaining
   */
  public AttributeDefFinder assignSplitScope(boolean theSplitScope) {
    this.splitScope = theSplitScope;
    return this;
  }

  /**
   * if passing in a stem, this is the stem scope...
   * @param theStemScope
   * @return this for chaining
   */
  public AttributeDefFinder assignStemScope(Scope theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }

  /**
   * this is the subject that has certain privileges or is in the service
   * @param theSubject
   * @return this for chaining
   */
  public AttributeDefFinder assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }

  /**
   * find all the attribute defs
   * @return the set of attribute defs or the empty set if none found
   */
  public Set<AttributeDef> findAttributes() {
    return GrouperDAOFactory.getFactory().getAttributeDef()
      .findAllAttributeDefsSecure(this.scope, this.splitScope, 
          this.subject, this.privileges, 
          this.queryOptions, this.parentStemId, this.stemScope, this.findByUuidOrName);
  }

  /**
   * if we are looking up a attribute def, only look by uuid or name
   * @param theFindByUuidOrName
   * @return the attribute def finder
   */
  public AttributeDefFinder assignFindByUuidOrName(boolean theFindByUuidOrName) {
    
    this.findByUuidOrName = theFindByUuidOrName;
    
    return this;
  }

  /**
   * find the attributeDef
   * @return the attributeDef or null
   */
  public AttributeDef findAttribute() {
    Set<AttributeDef> attributeDefs = this.findAttributes();
    
    return GrouperUtil.setPopOne(attributeDefs);
  }
  
  
  
}
