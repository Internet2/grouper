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

public class AndGroupFilter extends BaseGroupQueryFilter {

  private GroupQueryFilter groupFilter0;

  private GroupQueryFilter groupFilter1;

  public AndGroupFilter(GroupQueryFilter groupFilter0, GroupQueryFilter groupFilter1) {
    this.groupFilter0 = groupFilter0;
    this.groupFilter1 = groupFilter1;
    this.setQueryFilter(new IntersectionFilter(groupFilter0, groupFilter1));
  }

  public void setGroupFilter0(GroupQueryFilter groupFilter0) {
    this.groupFilter0 = groupFilter0;
  }

  public void setGroupFilter1(GroupQueryFilter groupFilter1) {
    this.groupFilter1 = groupFilter1;
  }

  public boolean matchesGroup(Group group) {
    return groupFilter0.matchesGroup(group) && groupFilter1.matchesGroup(group);
  }
}
