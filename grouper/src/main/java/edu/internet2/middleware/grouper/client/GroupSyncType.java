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
package edu.internet2.middleware.grouper.client;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * group sync type is for syncing groups to/from other groupers
 */
public enum GroupSyncType {
  
  /** periodically push group list to another grouper */
  push,
  
  /** periodically pull group list from another grouper */
  pull,
  
  /** push changes as they happen to the group list to the other grouper */
  incremental_push;

  /**
   * see if incremental
   * @return true if incremental
   */
  public boolean isIncremental() {
    return this == incremental_push;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GroupSyncType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GroupSyncType.class, 
        string, exceptionOnNull);
  
  }
}
