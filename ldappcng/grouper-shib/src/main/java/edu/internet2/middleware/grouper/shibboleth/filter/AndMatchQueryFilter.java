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

import edu.internet2.middleware.grouper.filter.IntersectionFilter;

/**
 * Selects objects that match two other {@link MatchQueryFilter}s, e.g. an intersection of the two filters. An object
 * matches this filter if and only if it matches both filters.
 */
public class AndMatchQueryFilter<ValueType> extends ConditionalMatchQueryFilter<ValueType> {

  /**
   * Constructor. Creates an {@link IntersectionFilter} of the given {@link MatchQueryFilter}s.
   * 
   * @param filter0 MatchQueryFilter
   * @param filter1 MatchQueryFilter
   */
  public AndMatchQueryFilter(MatchQueryFilter filter0, MatchQueryFilter filter1) {
    this.setFilter0(filter0);
    this.setFilter1(filter1);
    this.setQueryFilter(new IntersectionFilter(filter0, filter1));
  }

  /**
   * Returns true if the object matches the first filter and the second filter. {@inheritDoc}
   */
  public boolean matches(ValueType valueType) {
    return this.getFilter0().matches(valueType) && this.getFilter1().matches(valueType);
  }
}
