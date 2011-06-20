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
 * An abstract conditional {@link MatchQueryFilter}.
 */
public abstract class ConditionalMatchQueryFilter<ValueType> extends BaseMatchQueryFilter<ValueType> {

  /** filter 0 */
  private MatchQueryFilter filter0;

  /** filter 1 */
  private MatchQueryFilter filter1;

  /**
   * @return Returns filter0.
   */
  public MatchQueryFilter getFilter0() {
    return filter0;
  }

  /**
   * @param filter0 The filter to set.
   */
  public void setFilter0(MatchQueryFilter filter0) {
    this.filter0 = filter0;
  }

  /**
   * @return Returns filter1.
   */
  public MatchQueryFilter getFilter1() {
    return filter1;
  }

  /**
   * @param filter1 The filter to set.
   */
  public void setFilter1(MatchQueryFilter filter1) {
    this.filter1 = filter1;
  }
}
