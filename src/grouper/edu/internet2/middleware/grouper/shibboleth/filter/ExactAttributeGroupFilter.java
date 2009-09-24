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
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;

/**
 * Selects groups possessing an attribute name and value.
 */
public class ExactAttributeGroupFilter extends BaseGroupQueryFilter {

  /** attribute name */
  private String name;

  /** attribute value */
  private String value;

  /**
   * Creates a GroupAttributeFilter which returns groups with the given attribute name and
   * value. Groups are not restricted by stem.
   * 
   * @param name
   *          the attribute name
   * @param value
   *          the attribute value
   */
  public ExactAttributeGroupFilter(String name, String value) {
    this.name = name;
    this.value = value;
    this.setQueryFilter(new GroupAttributeFilter(name, value, StemFinder.findRootStem(getGrouperSession())));
  }

  /** {@inheritDoc} */
  public boolean matchesGroup(Group group) {
    return value.equals(group.getAttributeOrFieldValue(name, true, false));
  }
}
