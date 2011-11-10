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

import edu.internet2.middleware.grouper.filter.UnionFilter;

/**
 * Selects Grouper objects that match either of two {@link Filter}s, e.g. the union of two
 * {@link Filter}s. An object matches this filter if it matches either {@link Filter}s.
 * 
 */
public class OrFilter<T> extends AbstractSetOperationFilter<T> {

  /**
   * Constructor. Creates a {@link UnionFilter} of the given {@link Filter}s.
   * 
   * @param filter0 Matcher
   * @param filter1 Matcher
   */
  public OrFilter(Filter filter0, Filter filter1) {
    this.setFilter0(filter0);
    this.setFilter1(filter1);
    this.setQueryFilter(new UnionFilter(filter0, filter1));
  }

  /**
   * Returns true if the object matches the first filter or the second filter.
   * 
   * {@inheritDoc}
   */
  public boolean matches(T t) {
    return this.getFilter0().matches(t) || this.getFilter1().matches(t);
  }
}
