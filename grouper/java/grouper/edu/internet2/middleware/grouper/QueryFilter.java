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

import  java.util.*;


/** 
 * Interface for querying the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: QueryFilter.java,v 1.1.2.1 2005-10-20 20:41:11 blair Exp $
 */
public interface QueryFilter {

  // Public Instance Methods

  /**
   * Get groups matching query filter.
   * <pre class="eg">
   * Set groups = gq.getGroups();
   * </pre>
   * @return  Set of matching {@link Group} objects.
   */
  public Set getGroups();

  /**
   * Get members matching query filter.
   * <pre class="eg">
   * Set members = gq.getMembers();
   * </pre>
   * @return  Set of matching {@link Member} objects.
   */
  public Set getMembers();

  /**
   * Get memberships matching query filter.
   * <pre class="eg">
   * Set memberships = gq.getMemberships();
   * </pre>
   * @return  Set of matching {@link Membership} objects.
   */
  public Set getMemberships();

  /**
   * Get stems matching query filter.
   * <pre class="eg">
   * Set stems = gq.getStems();
   * </pre>
   * @return  Set of matching {@link Stem} objects.
   */
  public Set getStems();

}

