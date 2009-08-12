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

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.filter.QueryFilter;

public abstract class BaseGroupQueryFilter implements GroupQueryFilter {

  // TODO really, anothe grouper session ?
  private GrouperSession grouperSession;

  private QueryFilter<Group> queryFilter;

  public Set<Group> getResults(GrouperSession s) throws QueryException {
    return getQueryFilter().getResults(s);
  }

  public GrouperSession getGrouperSession() {

    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession();
    }

    return grouperSession;
  }

  public void setGrouperSession(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
  }

  public QueryFilter<Group> getQueryFilter() {
    return queryFilter;
  }

  public void setQueryFilter(QueryFilter<Group> queryFilter) {
    this.queryFilter = queryFilter;
  }
}
