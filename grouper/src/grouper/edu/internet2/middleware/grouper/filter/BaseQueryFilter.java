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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Base {@link QueryFilter} that all other query filters should extend.
 * <p/>
 * @author  blair christensen.
 * @version $Id: BaseQueryFilter.java,v 1.4 2008-11-05 16:18:46 shilen Exp $
 * @param <ValueType> Group, Membership, Stem
 */
public class BaseQueryFilter<ValueType> implements QueryFilter<ValueType> {

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
  private static final Log LOG = GrouperUtil.getLog(BaseQueryFilter.class);

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

  /**
   * Get the scope as a string.
   *
   * @param ns
   * @return the scope
   */
  protected String getStringForScope(Stem ns) {
    return ns.getName() + Stem.DELIM;
  }

  protected Set<Stem> removeRootStem(Set<Stem> candidates) {
    Set<Stem> results = new LinkedHashSet<Stem>();
    Iterator<Stem> it = candidates.iterator();
    while (it.hasNext()) {
      Stem next = it.next();
      if (!next.isRootStem()) {
        results.add(next);
      }
    }

    return results;
  }

}

