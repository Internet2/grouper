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
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Various types of rolling back
 * @author mchyzer
 *
 */
public enum GrouperRollbackType {
  /** always rollback right now */
  ROLLBACK_NOW,
  
  /** only rollback if this is a new transaction */
  ROLLBACK_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static GrouperRollbackType valueOfIgnoreCase(String string) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperRollbackType.class,string, false );
  }

}
