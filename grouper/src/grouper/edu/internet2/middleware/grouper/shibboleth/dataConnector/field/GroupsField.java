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

package edu.internet2.middleware.grouper.shibboleth.dataConnector.field;

import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.shibboleth.filter.GroupQueryFilter;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;

/**
 * A representation of an attribute consisting of Groups.
 */
public class GroupsField extends BaseMembershipField {

  /** the first element of the identifier */
  public static final String NAME = "groups";

  /**
   * @see edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField#constructor(String id)
   */
  public GroupsField(String id) throws GrouperException {
    super(id);
  }

  /**
   * Get the resultant attribute whose values are the {@link Group}s that the given {@link Member} belongs to.
   * 
   * @param member
   *          the member
   * @return the attribute consisting of groups or <tt>null</tt> if the member does not belong to any groups
   */
  public BaseAttribute<Group> getAttribute(Member member) {
    return getAttribute(member, null);
  }

  /**
   * Get the resultant attribute whose values are the {@link Group}s that the given {@link Member} belongs to. If the
   * {@link GroupQueryFilter} is not null, then only the groups which match the filter are returned.
   * 
   * @param member
   *          the member
   * @param groupQueryFilter
   *          the group filter
   * @return the attribute consisting of groups or <tt>null</tt> if the member does not belong to any groups
   */
  public BaseAttribute<Group> getAttribute(Member member, GroupQueryFilter groupQueryFilter) {

    // FUTURE make sorting optional ?

    Set<Group> groups = new TreeSet<Group>(this.getMemberFilter().getGroups(member, this.getField()));

    if (groups.isEmpty()) {
      return null;
    }

    // filter groups if appropriate
    if (groupQueryFilter != null) {
      Set<Group> newGroups = new TreeSet<Group>();
      for (Group group : groups) {
        if (groupQueryFilter.matchesGroup(group)) {
          newGroups.add(group);
        }
      }
      if (newGroups.isEmpty()) {
        return null;
      }
      groups = newGroups;
    }

    BasicAttribute<Group> list = new BasicAttribute<Group>(getId());
    list.setValues(groups);
    return list;
  }
}
