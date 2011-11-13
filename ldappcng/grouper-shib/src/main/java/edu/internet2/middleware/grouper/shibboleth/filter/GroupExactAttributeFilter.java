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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.BaseGrouperDataConnector;

/**
 * Selects {@link Group}s by attribute name and value.
 */
public class GroupExactAttributeFilter extends AbstractFilter<Group> {

  /** The attribute name. */
  private String name;

  /** The attribute value. */
  private String value;

  /**
   * Creates a {@link GroupAttributeFilter} which returns groups with the given attribute name and value. Groups are not
   * restricted by stem.
   * 
   * @param name the attribute name
   * @param value the attribute value
   */
  public GroupExactAttributeFilter(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * If the query filter is null, create a new {@link GroupAttributeFilter}. As this filter relies upon a
   * {@link GrouperSession}, this method should be called after the session has been started in the parent
   * {@link BaseGrouperDataConnector}.
   * 
   * {@inheritDoc}
   */
  public QueryFilter<Group> getQueryFilter() {
    if (super.getQueryFilter() == null) {
      this.setQueryFilter(new GroupAttributeFilter(name, value, StemFinder.findRootStem(getGrouperSession())));
    }
    return super.getQueryFilter();
  }

  /**
   * Returns true if the group has an attribute with the configured name and value.
   * 
   * {@inheritDoc}
   */
  public boolean matches(Group group) {
    try {
      return value.equals(group.getAttributeOrFieldValue(name, true, false));
    } catch (AttributeNotFoundException e) {
      return false;
    }
  }
}
