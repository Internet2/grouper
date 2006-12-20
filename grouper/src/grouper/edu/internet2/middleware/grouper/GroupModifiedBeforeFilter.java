/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * Query by groups modified before the specified date.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupModifiedBeforeFilter.java,v 1.3 2006-12-20 17:13:37 blair Exp $
 * @since   1.1.0
 */
public class GroupModifiedBeforeFilter extends BaseQueryFilter {

  // PRIVATE INSTANCE VARIABLES //
  private Date  d;      
  private Stem  ns;


  // CONSTRUCTORS //

  /**
   * {@link QueryFilter} that returns groups modified before the
   * specified date. 
   * <p/>
   * @param   d   Find groups modified before this date.
   * @param   ns  Restrict results to within this stem.
   */
  public GroupModifiedBeforeFilter(Date d, Stem ns) {
    this.d  = (Date) d.clone();
    this.ns = ns;
  } // public GroupModifiedBeforeFilter(d, ns)


  // PUBLIC INSTANCE METHODS //

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSessionValidator.validate(s);
    Set candidates  = GroupFinder.internal_findAllByModifiedBefore(s, this.d);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

} // public class GroupModifiedBeforeFilter

