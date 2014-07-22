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
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * can be used for generic method for attribute assignments
 */
public enum AttributeAssignValueOperation {
  
  /** if the value is there, leave it alone, else change an existing value for it */
  assign_value,
  
  /** whether or not the attribute is already assigned, assign it again */
  add_value,
  
  /** remove all assignments of this attribute value */
  remove_value,
  
  /** take the new values, and replace the existing values */ 
  replace_values;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeAssignValueOperation valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeAssignValueOperation.class, 
        string, exceptionOnNull);
  }

}
