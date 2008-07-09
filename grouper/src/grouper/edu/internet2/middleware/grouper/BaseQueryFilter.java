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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Base {@link QueryFilter} that all other query filters should extend.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseQueryFilter.java,v 1.16 2008-07-09 05:28:17 mchyzer Exp $
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
    Set filtered = new LinkedHashSet();
    for ( Object o : candidates ) {
      if      ( o instanceof Group ) {
        Group g = (Group) o;
        if ( ns.isChildGroup(g) ) {
          filtered.add(g);
        }
      }
      else if ( o instanceof Membership ) {
        Membership ms = (Membership) o;
        try {
          if ( ns.isChildGroup( ms.getGroup() ) ) {
            filtered.add(ms);
          }
        }
        catch (GroupNotFoundException eGNF) {
          // Ignore
        } 
      }
      else if ( o instanceof Stem ) {
        Stem stem = (Stem) o;
        if ( ns.isChildStem(stem) ) {
          filtered.add(stem);
        }
      }
      else {
        LOG.error(E.FILTER_SCOPE + o.getClass());
      }
    }
    return filtered;
  } 

  /** logger */
  private static final Log LOG = LogFactory.getLog(BaseQueryFilter.class);

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

