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

public class ExactAttributeGroupFilter extends BaseGroupQueryFilter {

  private String name;

  private String value;

  public ExactAttributeGroupFilter(String name, String value) {
    this.name = name;
    this.value = value;
    this.setQueryFilter(new GroupAttributeFilter(name, value, StemFinder.findRootStem(getGrouperSession())));
  }

  public boolean matchesGroup(Group group) {
    return value.equals(group.getAttributeOrFieldValue(name, true, false));
  }
}
