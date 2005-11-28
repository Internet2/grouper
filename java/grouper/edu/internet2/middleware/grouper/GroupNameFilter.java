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
 * Query by group name.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupNameFilter.java,v 1.2 2005-11-28 18:13:18 blair Exp $
 */
public class GroupNameFilter extends BaseQueryFilter {

  // Private Instance Variables
  private String  name;
  private Stem    ns;


  // Constructors

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * name.
   * <p>
   * This performs a substring, lowercased query against <i>name</i>,
   * <i>displayName</i>, <i>extension</i> and <i>displayExtension</i>.
   * </p>
   * @param   name  Find groups matching this name.
   * @param   ns    Restrict results to within this stem.
   */
  public GroupNameFilter(String name, Stem ns) {
    this.name = name;
    this.ns   = ns;
  } // public GroupNameFilter(name, ns)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set candidates  = GroupFinder.findByApproximateName(s, this.name);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}

