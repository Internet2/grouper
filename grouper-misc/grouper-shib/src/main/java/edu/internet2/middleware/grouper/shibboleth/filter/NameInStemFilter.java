/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Selects objects in a name with {@link Scope}.
 */
public class NameInStemFilter extends AbstractFilter<String> {

  /** The stem name. */
  private String name;

  /** The stem scope. */
  private Scope scope;

  /**
   * Constructor.
   * 
   * @param name the name
   * @param scope the scope
   */
  public NameInStemFilter(String name, String scope) {
    this.name = name;
    this.scope = Scope.valueOf(scope);

  }

  /** {@inheritDoc} */
  public Set<String> getResults(GrouperSession s) throws QueryException {
    Stem stem = StemFinder.findByName(getGrouperSession(), name, false);

    if (stem == null) {
      return Collections.EMPTY_SET;
    }

    Set<String> set = new LinkedHashSet<String>();

    for (Stem child : stem.getChildStems(scope)) {
      set.add(child.getName());
    }

    for (Group group : stem.getChildGroups(scope)) {
      set.add(group.getName());
    }

    return set;
  }

  /**
   * Returns true if the name is a child of the configured name with the configured scope.
   * 
   * {@inheritDoc}
   */
  public boolean matches(Object name) {
    if (!(name instanceof String)) {
      return false;
    }

    // the parent stem name
    String parentStemName = GrouperUtil.parentStemNameFromName((String) name);

    // if the parent stem is the root stem and the stem to match is the root stem, return false
    if (parentStemName == null && ((String) name).isEmpty()) {
      return false;
    }

    // if stem to match is at top level, then return true only if parent stem is root
    if (parentStemName == null) {
      if (this.name.isEmpty()) {
        return true;
      } else {
        return false;
      }
    }

    if (scope.equals(Scope.SUB)) {
      return parentStemName.startsWith(this.name);
    } else if (scope.equals(Scope.ONE)) {
      return parentStemName.equals(this.name);
    } else {
      throw new GrouperException("Unknown scope " + scope);
    }
  }
}
