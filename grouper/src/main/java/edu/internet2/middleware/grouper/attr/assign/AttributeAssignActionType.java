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
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * type of assignment, immediate for a direct assignment, 
 * effective for an assignment due to another assignment (e.g. a set of attributes, 
 * or a role hierarchy), or self for each attribute def (so we can do a simple join)
 * @author mchyzer
 *
 */
public enum AttributeAssignActionType {

  /** immediate for a direct assignment */
  immediate,
  
  /** row for self so we can do a simple join */
  self,
  
  /** 
   * effective for an assignment due to another assignment (e.g. a set of attributes, 
   * or a role hierarchy)
   */
  effective;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignActionType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignActionType.class, 
        string, exceptionOnNull);

  }
}
