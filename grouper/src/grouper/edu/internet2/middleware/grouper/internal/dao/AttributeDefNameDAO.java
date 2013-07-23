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
 * @author mchyzer
 * $Id: AttributeDefNameDAO.java,v 1.6 2009-10-20 14:55:50 shilen Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.subject.Subject;

/**
 * attribute def name data access methods
 */
public interface AttributeDefNameDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def name object 
   * @param attributeDefName
   */
  public void saveOrUpdate(AttributeDefName attributeDefName);
  
  /**
   * find by ids secure
   * @param ids
   * @param queryOptions
   * @return the attributeDefNames or null or exception
   */
  Set<AttributeDefName> findByIdsSecure(Collection<String> ids, QueryOptions queryOptions);

  /**
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def name or null if not there
   */
  public AttributeDefName findByIdSecure(String id, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def name by name
   * @param name 
   * @param exceptionIfNotFound 
   * @return  name
   * @throws GrouperDAOException 
   * @throws AttributeDefNameNotFoundException 
   */
  public AttributeDefName findByNameSecure(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNameNotFoundException;

  /**
   * find an attribute def name by name
   * @param name 
   * @param exceptionIfNotFound 
   * @param queryOptions 
   * @return  name
   * @throws GrouperDAOException 
   * @throws AttributeDefNameNotFoundException 
   */
  public AttributeDefName findByNameSecure(String name, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws GrouperDAOException, AttributeDefNameNotFoundException;

  /**
   * delete this attribute def name
   * @param attributeDefName 
   */
  public void delete(AttributeDefName attributeDefName);
  
  /**
   * Find all that have the given stem id.
   * @param id
   * @return set of attribute def names
   */
  public Set<AttributeDefName> findByStem(String id);
 
  /**
   * Find all that have the given attribute def id.
   * @param id
   * @return set of attribute def names
   */
  public Set<AttributeDefName> findByAttributeDef(String id);
 
  /**
   * find a record by uuid or name
   * @param id
   * @param name
   * @param exceptionIfNotFound
   * @return the attribute def name
   */
  public AttributeDefName findByUuidOrName(String id, String name, boolean exceptionIfNotFound);

  /**
   * find a record by uuid or name
   * @param id
   * @param name
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return the attribute def name
   */
  public AttributeDefName findByUuidOrName(String id, String name, boolean exceptionIfNotFound, 
      QueryOptions queryOptions);

  /**
   * @since   2.2
   * @param idIndex
   * @param exceptionIfNotFound
   * @param queryOptions
   */
  AttributeDefName findByIdIndex(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws AttributeDefNameNotFoundException;

  /**
   * @since   2.2
   * @param idIndex
   * @param exceptionIfNotFound
   * @param queryOptions
   */
  AttributeDefName findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws AttributeDefNameNotFoundException;

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeDefName
   */
  public void saveUpdateProperties(AttributeDefName attributeDefName);

  /**
   * search for attributeDefName by name, display name, or description.  This is a secure method, a GrouperSession must be open.
   * Note, you should add the % signs before calling this method
   * @param searchField substring to search for
   * @param searchInAttributeDefIds ids to search in or null for all
   * @param queryOptions 
   * @return the attribute def names or empty set
   */
  public Set<AttributeDefName> findAllSecure(String searchField, Set<String> searchInAttributeDefIds, QueryOptions queryOptions);

  /**
   * search for all by attribute def id, and like string
   * @param attributeDefId 
   * @param likeString 
   * @return the attribute def names or empty set
   */
  public Set<AttributeDefName> findByAttributeDefLike(String attributeDefId, String likeString);

  /**
   * get all attribute names secure, split the scope by whitespace
   * @param scope
   * @param attributeDefId optional if filtering by names in a certain attribute definition
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @param attributeAssignType
   * @param attributeDefType
   * @return set of attribute defs
   */
  public Set<AttributeDefName> findAllAttributeNamesSplitScopeSecure(String scope, 
      GrouperSession grouperSession, String attributeDefId, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType);

  /**
   * get all attribute names secure, split the scope by whitespace
   * @param scope
   * @param splitScope 
   * @param attributeDefId optional if filtering by names in a certain attribute definition
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @param attributeAssignType
   * @param attributeDefType
   * @return set of attribute defs
   */
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope, boolean splitScope,
      GrouperSession grouperSession, String attributeDefId, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType);


  /**
   * get all attribute names secure, split the scope by whitespace
   * @param scope
   * @param splitScope 
   * @param attributeDefId optional if filtering by names in a certain attribute definition
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @param attributeAssignType
   * @param attributeDefType
   * @param anyServiceRole will see if the user has any role in a service, and return those services
   * @return set of attribute defs
   */
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope, boolean splitScope,
      GrouperSession grouperSession, String attributeDefId, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType, ServiceRole serviceRole, boolean anyServiceRole);

}
