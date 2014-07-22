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
package edu.internet2.middleware.grouper.entity;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * utility methods for grouper entities
 * @author mchyzer
 *
 */
public class EntityUtils {

  /**
   * return the stem name where the entity attributes go, without colon on end
   * @return stem name
   */
  public static String attributeEntityStemName() {
    String rootStemName = GrouperCheckConfig.attributeRootStemName();
    
    //namespace this separate from other builtins
    rootStemName += ":entities";
    return rootStemName;
  }

  /** Attribute name of entity subject identifier  */
  public static final String ATTR_DEF_EXTENSION_ENTITY_SUBJECT_IDENTIFIER = "entitySubjectIdentifier";

  /** attribute def name of entity subject identifier */
  private static String entitySubjectIdentifierName;

  /**
   * attribute def name of entity subject identifier
   * @return name
   */
  public static String entitySubjectIdentifierName() {
    if (entitySubjectIdentifierName == null) {
      entitySubjectIdentifierName = attributeEntityStemName() + ":" + ATTR_DEF_EXTENSION_ENTITY_SUBJECT_IDENTIFIER;
    }
    return entitySubjectIdentifierName;
  }
  
  /**
   * return attribute def name for entity subject identifier
   * @return attribute def name
   */
  public static AttributeDefName entitySubjectIdentifierAttributeDefName() {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(entitySubjectIdentifierName(), true);
  }


  
  
}
