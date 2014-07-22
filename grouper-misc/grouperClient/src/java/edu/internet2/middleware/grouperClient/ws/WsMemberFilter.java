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
package edu.internet2.middleware.grouperClient.ws;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * member filter for retrieving members.
 * 
 * @author mchyzer
 * 
 */
public enum WsMemberFilter {
  /** retrieve all members (immediate, effective and composite) */
  All,

  /** 
   * retrieve members which exist due a group as a member of another group (for composite
   * groups, this will not return anything) 
   */
  Effective,

  /** 
   * return only direct members of a group (for composite groups this will not return anything) 
   */
  Immediate,

  /** 
   * return only non direct members of a group (will return effective, composite, etc) 
   */
  NonImmediate,

  /**
   * if this is a composite group, then return all the memberships that match the 
   * composite operator (union, intersection, complement).  This will be the same as
   * All for composite groups.
   */
  Composite;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsMemberFilter valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(WsMemberFilter.class, string, false);
  }
}
