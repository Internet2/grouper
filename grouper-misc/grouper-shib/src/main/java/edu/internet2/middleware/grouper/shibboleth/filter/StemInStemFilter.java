/**
 * Copyright 2012 Internet2
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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.filter.StemsInStemFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Selects child {@link Stem}s in a {@link Stem} with {@link Scope}.
 */
public class StemInStemFilter extends AbstractFilter<Stem> {

  /** The stem name. */
  private String name;

  /** The stem scope. */
  private Scope scope;

  /**
   * Creates a {@link StemsInStemFilter} which returns the child {@link Stem}s which are under the given {@link Stem}
   * name and scope.
   * 
   * @param name the stem name
   * @param scope the stem scope
   */
  public StemInStemFilter(String name, String scope) {
    this.name = name;
    this.scope = Scope.valueOf(scope);
    this.setQueryFilter(new StemsInStemFilter(name, this.scope, true));
  }

  /**
   * Returns true if the stem is a child of the configured stem with the configured scope.
   * 
   * {@inheritDoc}
   */
  public boolean matches(final Object stem) {
    if (!(stem instanceof Stem)) {
      return false;
    }

    // find the configured parent stem
    Stem parentStem = StemFinder.findByName(getGrouperSession(), name, false);

    // unable to find parent stem, so return false
    if (parentStem == null) {
      return false;
    }

    // if the parent stem is the root stem and the stem to match is the root stem, return false
    if (parentStem.isRootStem() && ((Stem) stem).isRootStem()) {
      return false;
    }

    // if stem to match is at top level, then return true only if parent stem is root
    if (GrouperUtil.parentStemNameFromName(((Stem) stem).getName()) == null) {
      if (parentStem.isRootStem()) {
        return true;
      } else {
        return false;
      }
    }

    if (scope.equals(Scope.SUB)) {
      return GrouperUtil.parentStemNameFromName(((Stem) stem).getName(), false).startsWith(parentStem.getName());
    } else if (scope.equals(Scope.ONE)) {
      return GrouperUtil.parentStemNameFromName(((Stem) stem).getName(), false).equals(parentStem.getName());
    } else {
      throw new GrouperException("Unknown scope " + scope);
    }
  }
}
