/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;

import  java.io.Serializable;
import  java.util.*;


/** 
 * Perform arbitrary queries against the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperQuery.java,v 1.1.2.2 2005-10-20 21:13:19 blair Exp $
 */
public class GrouperQuery implements Serializable {

  // Constructors

  private GrouperQuery() {
    // Nothing
  }

  // Public Class Methods

  /**
   * Query the Groups Registry.
   * <pre class="eg">
   * GrouperQuery gq = GrouperQuery.createQuery(
   *   s, 
   *   new AndFilter(
   *     new GroupCreatedAfterFilter(date, stem),
   *     new GroupAttributeFilter(attr, value, stem) 
   *   )
   * );
   * </pre>
   * @param   s       Query within this session context.
   * @param   filter  A {@link QueryFilter} specification.
   * @return  A {@link GrouperQuery} object.
   */
  public static GrouperQuery createQuery(GrouperSession s, QueryFilter filter) {
    throw new RuntimeException("Not implemented");
  }

  // Public Instance Methods

  /**
   * Get groups matching query filter.
   * <pre class="eg">
   * Set groups = gq.getGroups();
   * </pre>
   * @return  Set of matching {@link Group} objects.
   */
  public Set getGroups() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get members matching query filter.
   * <pre class="eg">
   * Set members = gq.getMembers();
   * </pre>
   * @return  Set of matching {@link Member} objects.
   */
  public Set getMembers() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get memberships matching query filter.
   * <pre class="eg">
   * Set memberships = gq.getMemberships();
   * </pre>
   * @return  Set of matching {@link Membership} objects.
   */
  public Set getMemberships() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get stems matching query filter.
   * <pre class="eg">
   * Set stems = gq.getStems();
   * </pre>
   * @return  Set of matching {@link Stem} objects.
   */
  public Set getStems() {
    throw new RuntimeException("Not implemented");
  }

}

