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

package edu.internet2.middleware.grouper;
import  java.util.*;

/** 
 * Query by memberships created before the specified date.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipCreatedBeforeFilter.java,v 1.5 2007-02-28 17:40:44 blair Exp $
 * @since   1.1.0
 */
public class MembershipCreatedBeforeFilter extends BaseQueryFilter {

  // PRIVATE INSTANCE VARIABLES //
  private Date  d;
  private Field f;
  private Stem  ns;


  // CONSTRUCTORS //

  /**
   * {@link QueryFilter} that returns memberships created before the
   * specified date. 
   * <p/>
   * @param   d   Find memberships created before this date.
   * @param   ns  Restrict results to within this stem.
   */
  public MembershipCreatedBeforeFilter(Date d, Stem ns) {
    this.d  = (Date) d.clone();
    this.f  = Group.getDefaultList();
    this.ns = ns;
  } // public MembershipCreatedBeforeFilter(d, ns)


  // PUBLIC INSTANCE METHODS //

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set candidates  = MembershipFinder.internal_findAllByCreatedBefore(s, this.d, this.f);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

} // public class MembershipCreatedBeforeFilter

