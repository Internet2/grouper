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
/*
 * @author mchyzer $Id: StemScope.java,v 1.1 2009-12-10 08:54:32 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * scope of groups under a stem.  either all or just one level (immediate)
 */
public enum StemScope {

  /** just direct immediate chlidren */
  ONE_LEVEL,
  
  /** all groups in this folder or subfolders */
  ALL_IN_SUBTREE;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static StemScope valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(StemScope.class, string, false);
  }
}
