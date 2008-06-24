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

package edu.internet2.middleware.grouper.queryFilter;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.BaseQueryFilter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.QueryException;
import edu.internet2.middleware.grouper.QueryFilter;


/** 
 * Query by exact group name.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupNameExactFilter.java,v 1.2 2008-06-24 06:07:03 mchyzer Exp $
 */
public class GroupNameExactFilter extends BaseQueryFilter {

  // Private Instance Variables
  /** exact name of group to find */
  private String  name;

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * name exactly.
   * @param   name1  Find groups matching this name.
   */
  public GroupNameExactFilter(String name1) {
    this.name = name1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
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
      group = GroupFinder.findByName(s, this.name);
      candidates.add(group);
    } catch (GroupNotFoundException gnfe) {
      return candidates;
      //ignore
    }
    return candidates;
  } 
}

