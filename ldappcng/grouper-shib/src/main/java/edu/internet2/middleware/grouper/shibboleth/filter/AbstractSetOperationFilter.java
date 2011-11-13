/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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

/**
 * An abstract conditional {@link Filter} consisting of two {@link Filter}s for use in union,
 * intersection, and complement filters.
 */
public abstract class AbstractSetOperationFilter<T> extends AbstractFilter<T> {

  /** Filter 0. */
  private Filter filter0;

  /** Filter 1. */
  private Filter filter1;

  /**
   * @return Returns the first filter.
   */
  public Filter getFilter0() {
    return filter0;
  }

  /**
   * @param filter0 The first filter to set.
   */
  public void setFilter0(Filter filter0) {
    this.filter0 = filter0;
  }

  /**
   * @return Returns the second filter.
   */
  public Filter getFilter1() {
    return filter1;
  }

  /**
   * @param filter1 The second filter to set.
   */
  public void setFilter1(Filter filter1) {
    this.filter1 = filter1;
  }

}
