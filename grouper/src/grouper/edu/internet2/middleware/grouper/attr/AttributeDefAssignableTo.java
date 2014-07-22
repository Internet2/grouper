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
 * 
 */
package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * types of objects which attributes can be assigned to
 *
 */
public enum AttributeDefAssignableTo {

  /** attribute assigned to a group */
  group, 
  
  /** attribute assigned to a stem */
  stem, 

  /** attribute assigned to a membership */
  membership, 
  
  /** attribute assigned to a member */
  member, 
  
  /** attribute assigned to a group attribute */
  groupAttribute, 

  /** attribute assigned to a stem attribute */
  stemAttribute, 
  
  /** attribute assigned to a membership attribute */
  membershipAttribute, 

  /** attribute assigned to a member attribute */
  memberAttribute;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefAssignableTo valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefAssignableTo.class, 
        string, exceptionOnNull);

  }
  
}
