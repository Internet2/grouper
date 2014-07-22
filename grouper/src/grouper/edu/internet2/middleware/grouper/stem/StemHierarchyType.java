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
package edu.internet2.middleware.grouper.stem;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Type of assignment: immediate for direct assignment (immediate child), 
 * effective for an assignment due to another assignment, 
 * or self for each stem (so we can do a simple join)
 * @author shilen
 * $Id$
 */
public enum StemHierarchyType {

  /** immediate for a direct assignment (immediate child) */
  immediate,
  
  /** row for self so we can do a simple join */
  self,
  
  /** 
   * effective for an assignment due to another assignment
   */
  effective;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static StemHierarchyType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(StemHierarchyType.class, 
        string, exceptionOnNull);

  }
}
