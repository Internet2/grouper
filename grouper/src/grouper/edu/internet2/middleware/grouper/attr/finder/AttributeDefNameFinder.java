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
 * $Id: AttributeDefNameFinder.java,v 1.1 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * finder methods for attribute def name
 */
public class AttributeDefNameFinder {

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
