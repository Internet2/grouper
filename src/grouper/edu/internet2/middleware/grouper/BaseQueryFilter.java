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
 * Base {@link QueryFilter} that all other query filters should extend.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseQueryFilter.java,v 1.9 2006-07-03 17:18:48 blair Exp $
 */
public class BaseQueryFilter implements QueryFilter {

  // PRIVATE CLASS CONSTANTS //
  private static final Set RESULTS = new LinkedHashSet();


  // PUBLIC INSTANCE METHODS //

  /**
   * Filter candidates by scope.
   * <p/>
   * @param   ns          Restrict results to this scope.
   * @param   candidates  A Set of candidate objects
   * @return  A set of filtered objects
   */
  public Set filterByScope(Stem  ns, Set candidates) {
    Set       filtered  = new LinkedHashSet();
    Object    o;
    Iterator  iter      = candidates.iterator();
    while (iter.hasNext()) {
      o = iter.next();
      if      (o.getClass().equals(Group.class)) {
        Group g = (Group) o;
        if (StemFinder.isChild(ns, g)) {
          filtered.add(g);
        }
      }
      else if (o.getClass().equals(Stem.class)) {
        Stem stem = (Stem) o;
        if (StemFinder.isChild(ns, stem)) {
          filtered.add(stem);
        }
      }
      else {
        ErrorLog.error(BaseQueryFilter.class, E.FILTER_SCOPE + o.getClass());
      }
    }
    return filtered;
  } // public Set filterByScope(ns, candidates)

  /**
   * Get filter results.
   * <p/>
   * @param   s   Get groups within this session context.
   * @return  Objects that match filter constraints.
   * @throws  QueryException
   */
  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    return RESULTS;
  } // public Set getResults(s)

}

