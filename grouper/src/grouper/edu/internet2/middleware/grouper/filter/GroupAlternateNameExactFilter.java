/*******************************************************************************
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
 ******************************************************************************/
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.filter;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;


/** 
 * Query by exact group alternate name.
 * <p/>
 * @author shilen
 * @version $Id: GroupAlternateNameExactFilter.java,v 1.1 2009-03-27 19:32:41 shilen Exp $
 */
public class GroupAlternateNameExactFilter extends BaseQueryFilter {

  // Private Instance Variables
  /** exact name of group to find */
  private String  name;

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * alternate name exactly.
   * @param   name1  Find groups matching this alternate name.
   */
  public GroupAlternateNameExactFilter(String name1) {
    this.name = name1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.filter.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   * @return the group in a set, or null if none
   */
  @Override
  public Set<Group> getResults(GrouperSession s) 
    throws QueryException  {

    //note, no need for GrouperSession inverse of control

    GrouperSession.validate(s);
    Set candidates  = new HashSet<Group>();
    Group group = null;
    try {
      group = GroupFinder.findByAlternateName(s, this.name, true);
      candidates.add(group);
    } catch (GroupNotFoundException gnfe) {
      return candidates;
      //ignore
    }
    return candidates;
  } 
}

