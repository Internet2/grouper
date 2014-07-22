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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.QueryException;

/**
 * Selects an object by exact name.
 */
public class NameFilter extends AbstractFilter<String> {

  /** The name. */
  private String name;

  /**
   * Constructor.
   * 
   * @param name the name
   */
  public NameFilter(String name) {
    this.name = name;

  }

  /** {@inheritDoc} */
  public Set<String> getResults(GrouperSession s) throws QueryException {
    Stem stem = StemFinder.findByName(getGrouperSession(), name, false);

    if (stem == null) {
      return Collections.EMPTY_SET;
    }

    Set<String> set = new HashSet<String>();
    set.add(stem.getName());
    return set;
  }

  /**
   * Returns true if the given name equals the configured name.
   * 
   * {@inheritDoc}
   */
  public boolean matches(Object name) {
    return this.name.equals(name);
  }
}
