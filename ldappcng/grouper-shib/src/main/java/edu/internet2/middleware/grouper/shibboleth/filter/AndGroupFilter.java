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
import edu.internet2.middleware.grouper.filter.IntersectionFilter;

/**
 * Selects groups that match two other GroupQueryFilters, e.g. an intersection of two
 * group query filters. A group matches this filter if and only if it matches both group
 * query filters.
 */
public class AndGroupFilter extends ConditionalGroupQueryFilter {

  /**
   * Constructor. Creates an IntersectionFilter of the given GroupQueryFilters.
   * 
   * @param groupFilter0
   *          GroupQueryFilter
   * @param groupFilter1
   *          GroupQueryFilter
   */
  public AndGroupFilter(GroupQueryFilter groupFilter0, GroupQueryFilter groupFilter1) {
    this.setGroupFilter0(groupFilter0);
    this.setGroupFilter1(groupFilter1);
    this.setQueryFilter(new IntersectionFilter(groupFilter0, groupFilter1));
  }

  /** {@inheritDoc} */
  public boolean matchesGroup(Group group) {
    return this.getGroupFilter0().matchesGroup(group) && this.getGroupFilter1().matchesGroup(group);
  }
}
