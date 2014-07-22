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
 * @author mchyzer
 * $Id: AttributeDefScopeDAO.java,v 1.1 2009-06-29 15:58:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.exception.AttributeDefScopeNotFoundException;

/**
 * attribute assign value data access methods
 */
public interface AttributeDefScopeDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def scope object 
   * @param attributeDefScope 
   */
  public void saveOrUpdate(AttributeDefScope attributeDefScope);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the attribute assign value or null if not there
   * @throws AttributeDefScopeNotFoundException 
   */
  public AttributeDefScope findById(String id, boolean exceptionIfNotFound)
    throws AttributeDefScopeNotFoundException;

  /**
   * @param id 
   * @param idsToIgnore
   * @param attributeDefId 
   * @param attributeDefScopeType 
   * @param exceptionIfNull 
   * @param scopeString is for matching (if id doesnt match)
   * @return the attribute def scope or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  AttributeDefScope findByUuidOrKey(Collection<String> idsToIgnore,
      String id, String attributeDefId, String attributeDefScopeType, boolean exceptionIfNull, String scopeString) throws GrouperDAOException;

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeDefScope
   */
  public void saveUpdateProperties(AttributeDefScope attributeDefScope);
  
  /**
   * find all the scopes for a def
   * @param attributeDefId
   * @param queryOptions 
   * @return the attribute def scopes
   */
  public Set<AttributeDefScope> findByAttributeDefId(String attributeDefId, QueryOptions queryOptions);

  /** 
   * delete an attribute def scope object 
   * @param attributeDefScope
   */
  public void delete(AttributeDefScope attributeDefScope);

}
