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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Perform arbitrary queries against the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperQuery.java,v 1.3 2009-08-11 20:18:09 mchyzer Exp $
 */
public class GrouperQuery {

  // PRIVATE INSTANCE VARIABLES //
  private GrouperSession  s;
  private QueryFilter     filter;


  // CONSTRUCTORS //
  private GrouperQuery(GrouperSession s, QueryFilter filter) {
    this.s      = s;
    this.filter = filter;
  } // private GrouperQuery(s, filter)


  // PUBLIC CLASS METHODS //

  /**
   * Create a query for searching the Groups Registry.
   * <pre class="eg">
   * GrouperQuery gq = GrouperQuery.createQuery(
   *   s, 
   *   new AndFilter(
   *     new GroupCreatedAfterFilter(date, stem),
   *     new GroupAttributeFilter(attr, value, stem) 
   *   )
   * );
   * </pre>
   * <p>
   * This method defines a query but <b>does not</b> execute the query.  Evaulation
   * takes place in the {@link #getGroups()}, {@link #getMembers()}, 
   * {@link #getMemberships()} and {@link #getStems()} methods.  Those methods
   * all operate in the same manner.  They first execute the query filter.  This
   * returns a set of candidate results.  Each method then iterates through the
   * candidate set, extracting objects of the the appropriate time to return.
   * Several of the methods also convert between object types in order to return
   * results.  See each method for more details.
   * </p>
   * <p>
   * All query filters implement the {@link QueryFilter} interface.  See that
   * class for information on the query filters supplied by Grouper as well as
   * information on creating custom query filters.
   * </p>
   * @param   s       Query within this session context.
   * @param   filter  A {@link QueryFilter} specification.
   * @return  A {@link GrouperQuery} object.
   * @throws  QueryException
   */
  public static GrouperQuery createQuery(GrouperSession s, QueryFilter filter) 
    throws QueryException
  {
    GrouperSession.validate(s);
    return new GrouperQuery(s, filter);
  } // public static GrouperQuery createQuery(s, filter)


  // PUBLIC INSTANCE METHODS //

  /**
   * Get groups matching query filter.
   * <pre class="eg">
   * Set groups = gq.getGroups();
   * </pre>
   * <p>
   * This method (currently) performs no candidate object conversion.  Only
   * {@link Group} objects in the candidate set will be returned.
   * </p>
   * @return  Set of matching {@link Group} objects.
   * @throws  QueryException
   */
  public Set getGroups() 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    Set       groups      = new LinkedHashSet();
    Set       candidates  = this.filter.getResults(this.s);
    Object    o;
    Iterator  iter        = candidates.iterator();
    while (iter.hasNext()) {
      o = iter.next();
      if (o!=null) {
        if      (o.getClass().equals(Group.class)) {
          Group g = (Group) o;
          groups.add(g);
        }
        else {
          LOG.error(E.NI + E.Q_G + o.getClass());
        }
      }
    }
    return groups;
  } // public Set getGroups()

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperQuery.class);

  /**
   * Get members matching query filter.
   * <pre class="eg">
   * Set members = gq.getMembers();
   * </pre>
   * <p>
   * This method calls {@link #getMemberships()} internally.  Each {@link Membership}'s
   * {@link Member} is then extracted and returned.  
   * </p>
   * @return  Set of matching {@link Member} objects.
   * @throws  QueryException
   */
  public Set getMembers() 
    throws QueryException
  {
    Set<Member> members = new LinkedHashSet<Member>();
    Membership  ms;
    // Retrieve Memberships found by this query and then retrieve Member from each
    try {
      Iterator it = this.getMemberships().iterator();
      while (it.hasNext()) {
        ms = (Membership) it.next();
        members.add( ms.getMember() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new QueryException("unable to retrieve members: " + eMNF.getMessage(), eMNF);
    }
    return members;
  } // public Set getMembers()

  /**
   * Get memberships matching query filter.
   * <pre class="eg">
   * Set memberships = gq.getMemberships();
   * </pre>
   * <p>
   * If this method finds a {@link Group} in the candidate set it will add all 
   * {@link Membership}s returned by calling {@link Group#getMemberships()} to
   * the result set.
   * </p>
   * @return  Set of matching {@link Membership} objects.
   * @throws  QueryException
   */
  public Set getMemberships() 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    Set<Membership>       mships      = new LinkedHashSet<Membership>();
    Set       candidates  = this.filter.getResults(this.s);
    Object    o;
    Iterator  iter        = candidates.iterator();
    while (iter.hasNext()) {
      o = iter.next();
      if      (o.getClass().equals(Group.class))      {
        mships.addAll( ( (Group) o ).getMemberships() );
      }
      else if (o.getClass().equals(Membership.class)) {
        mships.add( (Membership) o );
      }
      else {
        LOG.error(E.NI + E.Q_MS + o.getClass());
      }
    }
    return mships;
  } // public Set getMemberships()

  /**
   * Get stems matching query filter.
   * <pre class="eg">
   * Set stems = gq.getStems();
   * </pre>
   * <p>
   * This method (currently) performs no candidate object conversion.  Only
   * {@link Stem} objects in the candidate set will be returned.
   * </p>
   * @return  Set of matching {@link Stem} objects.
   * @throws  QueryException
   */
  public Set<Stem> getStems() 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    Set       stems       = new LinkedHashSet();
    Set       candidates  = this.filter.getResults(this.s);
    Object    o;
    Iterator  iter        = candidates.iterator();
    while (iter.hasNext()) {
      o = iter.next();
      if (o.getClass().equals(Stem.class)) {
        stems.add( (Stem) o );
      }
      else {
        LOG.error(E.NI + E.Q_S + o.getClass());
      }
    }
    return stems;
  } // public Set getStems()

} // public class GrouperQuery

