/**
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
 */
/*
 * @author mchyzer
 * $Id: AttributeDefDAO.java,v 1.6 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;

/**
 * attribute def data access methods
 */
public interface AttributeDefDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def object 
   * @param attributeDef 
   */
  public void saveOrUpdate(AttributeDef attributeDef);
  
  /**
   * find by id.  This is a secure method, a grouperSession needs to be open
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound);
  
  /**
   * find by ids secure
   * @param ids
   * @param queryOptions
   * @return the attributeDefs or null or exception
   */
  Set<AttributeDef> findByIdsSecure(Collection<String> ids, QueryOptions queryOptions);

  /**
   * find by id.  This is a secure method, a grouperSession needs to be open
   * @param id
   * @param exceptionIfNotFound
   * @param queryOptions 
   * @return the attribute def or null if not there
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound, QueryOptions queryOptions);
  
  /**
   * find by id.  This is NOT a secure method, a grouperSession does not need to be open
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound);
  
  /**
   * find by id.  This is NOT a secure method, a grouperSession does not need to be open
   * @param id
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return the attribute def or null if not there
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound, QueryOptions queryOptions);
  
  /**
   * find by attributeDefNameId.  This is a secure method, a grouperSession needs to be open
   * @param attributeDefNameId
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findByAttributeDefNameIdSecure(String attributeDefNameId, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def by name.  this is a secure method, a grouperSession needs to be open
   * @param name 
   * @param exceptionIfNotFound 
   * @return attribute def
   * @throws GrouperDAOException 
   * @throws AttributeDefNotFoundException 
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNotFoundException;
  
  /**
   * find an attribute def by name.  this is a secure method, a grouperSession needs to be open
   * @param name 
   * @param exceptionIfNotFound 
   * @param queryOptions
   * @return attribute def
   * @throws GrouperDAOException 
   * @throws AttributeDefNotFoundException 
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws GrouperDAOException, AttributeDefNotFoundException;
  
  /**
   * Find all that have the given stem id.
   * @param id
   * @return set of stems
   */
  public Set<AttributeDef> findByStem(String id);
  
  /**
   * delete the attribute def
   * @param attributeDef
   */
  public void delete(AttributeDef attributeDef);
  
  /**
   * @since   2.2
   * @param idIndex
   * @param exceptionIfNotFound
   * @param queryOptions
   *
   */
  AttributeDef findByIdIndex(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws AttributeDefNotFoundException;

  /**
   * @since   2.2
   * @param idIndex
   * @param exceptionIfNotFound
   * @param queryOptions
   *
   */
  AttributeDef findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws AttributeDefNotFoundException;

  /**
   * search for an attribute def by id or name
   * @param id
   * @param name
   * @param exceptionIfNotFound
   * @return the attribute def or null
   */
  public AttributeDef findByUuidOrName(String id, String name, boolean exceptionIfNotFound);

  /**
   * search for an attribute def by id or name
   * @param id
   * @param name
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return the attribute def or null
   */
  public AttributeDef findByUuidOrName(String id, String name, boolean exceptionIfNotFound,
      QueryOptions queryOptions);

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeDef
   */
  public void saveUpdateProperties(AttributeDef attributeDef);

  /**
   * get all attribute defs secure
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @return attribute defs
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions);
  
  /**
   * get all attribute defs secure
   * @param scope
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @return set of attribute defs
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions);
  
  /**
   * get all attribute defs secure, split the scope by whitespace
   * @param scope
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @param attributeAssignType 
   * @param attributeDefType
   * @return set of attribute defs
   */
  public Set<AttributeDef> getAllAttributeDefsSplitScopeSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType);
  
  /**
   * see which attributeDefs do not have this privilege
   * @param grouperSession
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param queryOptions
   * @param considerAllSubject
   * @param sqlLikeString
   * @return the attributeDefs
   */
  public Set<AttributeDef> findAttributeDefsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString);

  /**
   * do a query based on various params
   * @param scope
   * @param splitScope
   * @param subject
   * @param privileges
   * @param queryOptions
   * @param parentStemId
   * @param stemScope
   * @param findByUuidOrName
   * @param attributeDefIds
   * @return the result set
   */
  public Set<AttributeDef> findAllAttributeDefsSecure(String scope, boolean splitScope, 
      Subject subject, Set<Privilege> privileges, 
      QueryOptions queryOptions, String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Collection<String> attributeDefIds);

}
