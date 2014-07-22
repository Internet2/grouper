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
import edu.internet2.middleware.grouper.filter.StemNameExactFilter;

/**
 * Selects a {@link Stem} by exact name.
 */
public class StemNameFilter extends AbstractFilter<Stem> {

  /** The stem name. */
  private String name;

  /**
   * Creates a {@link StemNameExactFilter} which returns a {@link Stem} with the given name.
   * 
   * @param name the stem name
   */
  public StemNameFilter(String name) {
    this.name = name;
    this.setQueryFilter(new edu.internet2.middleware.grouper.filter.StemNameExactFilter(name));
  }

  /**
   * Returns true if the given {@link Stem} has the configured name.
   * 
   * {@inheritDoc}
   */
  public boolean matches(Object stem) {
    if (!(stem instanceof Stem)) {
      return false;
    }
    return ((Stem) stem).getName().equals(name);
  }
}
