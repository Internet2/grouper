/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package edu.internet2.middleware.grouper;

import  java.util.*;


/** 
 * Query by groups created after the specified date.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupCreatedAfterFilter.java,v 1.1.2.1 2005-11-09 23:20:03 blair Exp $
 */
public class GroupCreatedAfterFilter extends BaseQueryFilter {

  // Private Instance Variables
  private Date  d;      
  private Stem  ns;


  // Constructors

  /**
   * {@link QueryFilter} that returns groups created after the
   * specified date. 
   * <p/>
   * @param   d   Find groups created after this date.
   * @param   ns  Restrict results to within this stem.
   */
  public GroupCreatedAfterFilter(Date d, Stem ns) {
    this.d  = d;
    this.ns = ns;
  } // public GroupCreatedAfterFilter(d, ns)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    Set candidates  = GroupFinder.findByCreatedAfter(s, this.d);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}

