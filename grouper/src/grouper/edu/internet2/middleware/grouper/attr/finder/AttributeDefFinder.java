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

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


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
  
  
  
}
