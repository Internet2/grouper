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
/**
 * @author mchyzer
 * $Id: AttributeDefNameFinder.java,v 1.1 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * finder methods for attribute def name.
 * the chained API is secure based on the static grouper session
 */
public class AttributeDefNameFinder {

  /**
   * id of attribute def name
   */
  private Set<String> idsOfAttributeDefName;
  
  /**
   * id of attribute def name
   * @param theIdsOfAttributeDefName
   * @return this for chaining
   */
  public AttributeDefNameFinder addIdOfAttributeDefName(String theIdsOfAttributeDefName) {
    if (GrouperUtil.length(theIdsOfAttributeDefName) > 1) {
      throw new RuntimeException("Currently only supports one ID");
    }
    
    if (this.idsOfAttributeDefName == null) {
      this.idsOfAttributeDefName = new HashSet<String>();
    }
    
    this.idsOfAttributeDefName.add(theIdsOfAttributeDefName);
    return this;
  }
  
  /**
   * parent or ancestor stem of the attribute def name
   */
  private String parentStemId;
  
  /**
   * parent or ancestor stem of the attribute def
   * @param theParentStemId
   * @return this for chaining
   */
  public AttributeDefNameFinder assignParentStemId(String theParentStemId) {
    this.parentStemId = theParentStemId;
    return this;
  }
  
  /**
   * if passing in a stem, this is the stem scope...
   */
  private Scope stemScope;

  /**
   * if passing in a stem, this is the stem scope...
   * @param theStemScope
   * @return this for chaining
   */
  public AttributeDefNameFinder assignStemScope(Scope theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }
  
  /**
   * scope to look for attribute def names  Wildcards will be appended or percent is the wildcard
   */
  private String scope;

  /**
   * scope to look for attribute def names  Wildcards will be appended or percent is the wildcard
   * @param theScope
   * @return this for chaining
   */
  public AttributeDefNameFinder assignScope(String theScope) {
    this.scope = theScope;
    return this;
  }
  
  /**
   * find attribute def names based on one attribute definition
   */
  private String attributeDefId;
  
  /**
   * find attribute def names based on one attribute definition
   * @param theAttributeDefId
   * @return this for chaining
   */
  public AttributeDefNameFinder assignAttributeDefId(String theAttributeDefId) {
    this.attributeDefId = theAttributeDefId;
    return this;
  }
  
  /**
   * if filtering by service, this is the role, or null for all
   */
  private ServiceRole serviceRole;
  
  /**
   * if filtering by service, this is the service role, or null for all
   * @param theServiceRole
   * @return this for chaining
   */
  public AttributeDefNameFinder assignServiceRole(ServiceRole theServiceRole) {
    this.serviceRole = theServiceRole;
    return this;
  }
  
  /**
   * this is the subject that has certain privileges
   */
  private Subject subject;
  
  /**
   * this is the subject that has certain privileges or is in the service
   * @param theSubject
   * @return this for chaining
   */
  public AttributeDefNameFinder assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }
  
  /**
   * find attribute definition names where the static grouper session has certain privileges on the results
   */
  private Set<Privilege> privileges;
  
  /**
   * assign privileges to filter by that the subject has on the attribute definition
   * @param thePrivileges
   * @return this for chaining
   */
  public AttributeDefNameFinder assignPrivileges(Set<Privilege> thePrivileges) {
    this.privileges = thePrivileges;
    return this;
  }

  /**
   * add a privilege to filter by that the subject has on the attribute definition
   * @param privilege should be AttributeDefPrivilege
   * @return this for chaining
   */
  public AttributeDefNameFinder addPrivilege(Privilege privilege) {
    
    if (this.privileges == null) {
      this.privileges = new HashSet<Privilege>();
    }
    
    this.privileges.add(privilege);
    
    return this;
  }
  
  /**
   * if sorting or paging
   */
  private QueryOptions queryOptions;
  
  /**
   * if sorting, paging, caching, etc
   * @param theQueryOptions
   * @return this for chaining
   */
  public AttributeDefNameFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }
  
  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private boolean splitScope;
  
  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScope
   * @return this for chaining
   */
  public AttributeDefNameFinder assignSplitScope(boolean theSplitScope) {
    this.splitScope = theSplitScope;
    return this;
  }
  
  /**
   * the type of assignment that the attributes can have
   */
  private AttributeAssignType attributeAssignType;
  
  /**
   * the type of assignment that the attributes can have
   * @param theAttributeAssignType
   * @return this for chaining
   */
  public AttributeDefNameFinder assignAttributeAssignType(AttributeAssignType theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
  }
  
  /**
   * the type of attribute
   */
  private AttributeDefType attributeDefType;
  
  /**
   * find an attributeDefName by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeDefName
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   * @throws AttributeDefNameNotFoundException
   */
  public static AttributeDefName findById(String id, boolean exceptionIfNull) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(id, exceptionIfNull);
    return attributeDefName;
  }
  
  /**
   * find all the attribute def names
   * @return the set of attribute def names or the empty set if none found
   */
  public Set<AttributeDefName> findAttributeNames() {
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.emptySetOfLookupsReturnsNoResults", true)) {
      // if passed in empty set of attributeDefName ids and no names, then no attributeDefNames found
      // uncomment this if we can search by attributeDefName names
      if (this.idsOfAttributeDefName != null && this.idsOfAttributeDefName.size() == 0 /* && GrouperUtil.length(this.namesOfAttributeDefName) == 0 */ ) {
        return new HashSet<AttributeDefName>();
      }
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    return GrouperDAOFactory.getFactory().getAttributeDefName()
      .findAllAttributeNamesSecure(this.scope, this.splitScope, grouperSession, 
          this.attributeDefId, this.subject, this.privileges, 
          this.queryOptions, this.attributeAssignType, 
          this.attributeDefType, 
          this.serviceRole, this.anyServiceRole, this.parentStemId, 
          this.stemScope, this.findByUuidOrName, this.idsOfAttributeDefName);
  }
  
  /**
   * mutually exclusive with serviceRole... this is true if looking for services where the user has any role
   */
  private boolean anyServiceRole = false;
  /**
   * if we are looking up a attribute def name, only look by uuid or name
   */
  private boolean findByUuidOrName;

  /**
   * mutually exclusive with serviceRole... this is true if looking for services where the user has any role
   * @param theAnyRole
   * @return this for chaining
   */
  public AttributeDefNameFinder assignAnyRole(boolean theAnyRole) {
    this.anyServiceRole = theAnyRole;
    return this;
  }
  
  /**
   * if we are looking up an attributedefname, only look by uuid or name
   * @param theFindByUuidOrName
   * @return the attribute def name finder
   */
  public AttributeDefNameFinder assignFindByUuidOrName(boolean theFindByUuidOrName) {
    
    this.findByUuidOrName = theFindByUuidOrName;
    
    return this;
  }

  /**
   * find the stem
   * @return the stem or null
   */
  public AttributeDefName findAttributeName() {
    Set<AttributeDefName> attributeDefNames = this.findAttributeNames();
    
    return GrouperUtil.setPopOne(attributeDefNames);
  }

  /**
   * Find an attributeDefName within the registry by ID index.
   * @param idIndex id index of attributeDefName to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param queryOptions 
   * @return  A {@link AttributeDefName}
   * @throws AttributeDefNameNotFoundException if not found an exceptionIfNotFound is true
   */
  public static AttributeDefName findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound,  QueryOptions queryOptions) 
      throws AttributeDefNameNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(GrouperSession.staticGrouperSession());
    AttributeDefName a = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdIndexSecure(idIndex, exceptionIfNotFound, queryOptions);
    return a;
  }

  /**
   * find an attributeDefName by name.  This is a secure method, a GrouperSession must be open
   * @param name of attributeDefName
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def name or null
   * @throws AttributeDefNameNotFoundException
   */
  public static AttributeDefName findByName(String name, boolean exceptionIfNull) {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(name, exceptionIfNull);
  }
  
  /**
   * multikey is the sourceId of user in grouper session, subject id in grouper session, and name of attribute def name, 
   * result is an array of size one or empty array if not found
   */
  private static GrouperCache<MultiKey, AttributeDefName[]> findByNameCache = new GrouperCache<MultiKey, AttributeDefName[]>(
      AttributeDefNameFinder.class.getName() + ".findByNameCache", 2000, false, 60, 60, false);
  //TODO remove defaults in 2.3+
  
  /**
   * find an attributeDefName by name.  This is a secure method, a GrouperSession must be open.  This will cache the result
   * @param name of attributeDefName
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def name or null
   * @throws AttributeDefNameNotFoundException
   */
  public static AttributeDefName findByNameCache(String name, boolean exceptionIfNull) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    Subject grouperSessionSubject = grouperSession.getSubject();
    MultiKey key = new MultiKey(grouperSessionSubject.getSourceId(), grouperSessionSubject.getId(), name);
    AttributeDefName[] resultArray = findByNameCache.get(key);
    AttributeDefName result = null;
    
    //see if not in cache, do the query
    if (resultArray == null) {
      result = findByName(name, false);
      resultArray = new AttributeDefName[]{result};
      //put new value in cache
      findByNameCache.put(key, resultArray);
    }

    if (GrouperUtil.length(resultArray) == 1) {
      result = resultArray[0];
    }
    if (result == null && exceptionIfNull) {
      throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with name: '" + name + "'");
    }
    return result;
  }
  
  /**
   * search for attributeDefName by name, display name, or description.  This is a secure method, a GrouperSession must be open.
   * You need to add %'s to it for wildcards
   * @param searchField substring to search for
   * @param searchInAttributeDefIds ids to search in or null for all
   * @param queryOptions 
   * @return the attribute def names or empty set
   */
  public static Set<AttributeDefName> findAll(String searchField, Set<String> searchInAttributeDefIds, QueryOptions queryOptions) {
    
    return GrouperDAOFactory.getFactory().getAttributeDefName().findAllSecure(searchField, searchInAttributeDefIds, queryOptions);
  }
  
}
