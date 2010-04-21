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
 * An abstract conditional group query filter.
 */
public abstract class ConditionalGroupQueryFilter extends BaseGroupQueryFilter {

  /** group filter 0 */
  private GroupQueryFilter groupFilter0;

  /** group filter 1 */
  private GroupQueryFilter groupFilter1;

  /**
   * @return Returns the groupFilter0.
   */
  public GroupQueryFilter getGroupFilter0() {
    return groupFilter0;
  }

  /**
   * @param groupFilter0
   *          The groupFilter0 to set.
   */
  public void setGroupFilter0(GroupQueryFilter groupFilter0) {
    this.groupFilter0 = groupFilter0;
  }

  /**
   * @return Returns the groupFilter1.
   */
  public GroupQueryFilter getGroupFilter1() {
    return groupFilter1;
  }

  /**
   * @param groupFilter1
   *          The groupFilter1 to set.
   */
  public void setGroupFilter1(GroupQueryFilter groupFilter1) {
    this.groupFilter1 = groupFilter1;
  }
}
