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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.filter.GroupsInStemFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Selects {@link Group}s in a {@link Stem} with {@link Scope}.
 */
public class GroupInStemFilter extends AbstractFilter<Group> {

  /** The stem name. */
  private String name;

  /** The stem scope. */
  private Scope scope;

  /**
   * Creates a {@link GroupsInStemFilter} which returns the {@link Group}s which are under the given {@link Stem} name
   * and scope.
   * 
   * @param name the stem name
   * @param scope the stem scope
   */
  public GroupInStemFilter(String name, String scope) {
    this.name = name;
    this.scope = Scope.valueOf(scope);
    this.setQueryFilter(new GroupsInStemFilter(name, this.scope, true));
  }

  /**
   * Returns true if the group is a child of the configured stem with the configured scope.
   * 
   * {@inheritDoc}
   */
  public boolean matches(Group group) {
    Stem stem = StemFinder.findByName(getGrouperSession(), name, false);
    if (stem == null) {
      return false;
    }
    
    if (scope.equals(Scope.SUB)) {
      return GrouperUtil.parentStemNameFromName(group.getName()).startsWith(stem.getName());
    } else if (scope.equals(Scope.ONE)) {
      return GrouperUtil.parentStemNameFromName(group.getName()).equals(stem.getName());
    } else {
      throw new GrouperException("Unknown scope " + scope);
    }
  }
}
