/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.filter;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.filter.QueryFilter;

/**
 * An extension to QueryFilter which provides the ability to determine if a Group matches
 * (would be returned by) the filter.
 */
public interface GroupQueryFilter extends QueryFilter<Group> {

  /**
   * Returns true if the Group would be returned by the filter. False otherwise.
   * 
   * @param group
   *          the Group
   * @return if the group matches or not
   */
  public boolean matchesGroup(Group group);

  /**
   * Set the grouper session
   * 
   * @param grouperSession
   *          the {@link GrouperSession}
   */
  public void setGrouperSession(GrouperSession grouperSession);

}
