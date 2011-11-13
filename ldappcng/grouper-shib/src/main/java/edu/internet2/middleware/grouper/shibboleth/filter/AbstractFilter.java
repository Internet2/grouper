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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.filter.QueryFilter;

/**
 * Base class for a {@link Filter}.
 */
public abstract class AbstractFilter<T> implements Filter<T> {

  /** The grouper session. */
  private GrouperSession grouperSession;

  /** The underlying query filter. */
  private QueryFilter<T> queryFilter;

  /** {@inheritDoc} */
  public Set<T> getResults(GrouperSession s) throws QueryException {
    return getQueryFilter().getResults(s);
  }

  /**
   * Get the grouper session. Re-uses the same session. A grouper session must have been started already in the jvm.
   * 
   * @return the grouper session
   */
  public GrouperSession getGrouperSession() {
    return grouperSession;
  }

  /**
   * Get the query filter.
   * 
   * @return the QueryFilter
   */
  public QueryFilter<T> getQueryFilter() {
    return queryFilter;
  }

  /**
   * Set the query filter.
   * 
   * @param queryFilter
   */
  public void setQueryFilter(QueryFilter<T> queryFilter) {
    this.queryFilter = queryFilter;
  }

  /**
   * Set the grouper session.
   * 
   * @param grouperSession
   */
  public void setGrouperSession(GrouperSession grouperSession) {
    this.grouperSession = grouperSession;
  }

}
