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
package edu.internet2.middleware.grouper.ws.rest.attribute;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * if searching for inheritance ancestors or descendents, this is the relation
 * to the current one being searched for
 * 
 * @author mchyzer
 *
 */
public enum WsInheritanceSetRelation {

  /**
   * find values that are implied by this value
   */
  IMPLIED_BY_THIS {

    /**
     * @see WsInheritanceSetRelation#relatedAttributeDefNames(AttributeDefName)
     */
    @Override
    public Set<AttributeDefName> relatedAttributeDefNames(AttributeDefName attributeDefName) {
      return attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis();
    }
  },

  /**
   * find values that are implied this value immediately, i.e. can be directly unassigned
   */
  IMPLIED_BY_THIS_IMMEDIATE {

    /**
     * @see WsInheritanceSetRelation#relatedAttributeDefNames(AttributeDefName)
     */
    @Override
    public Set<AttributeDefName> relatedAttributeDefNames(AttributeDefName attributeDefName) {
      return attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThisImmediate();
    }
  },
  
  /**
   * find values that imply this value
   */
  THAT_IMPLY_THIS {

    /**
     * @see WsInheritanceSetRelation#relatedAttributeDefNames(AttributeDefName)
     */
    @Override
    public Set<AttributeDefName> relatedAttributeDefNames(AttributeDefName attributeDefName) {
      return attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThis();
    }
  },
  
  /**
   * find values that imply this value immediately, i.e. can be directly unassigned
   */
  THAT_IMPLY_THIS_IMMEDIATE {

    /**
     * @see WsInheritanceSetRelation#relatedAttributeDefNames(AttributeDefName)
     */
    @Override
    public Set<AttributeDefName> relatedAttributeDefNames(AttributeDefName attributeDefName) {
      return attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThisImmediate();
    }
  };

  /**
   * get the related attribute def names
   * @param attributeDefName is the object related to... 
   * @return attribute def names
   */
  public abstract Set<AttributeDefName> relatedAttributeDefNames(AttributeDefName attributeDefName);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsInheritanceSetRelation valueOfIgnoreCase(String string) {
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsInheritanceSetRelation.class, string, false);
  }
  
}
