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
 * Perform arbitrary queries against the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperQuery.java,v 1.19 2006-09-11 18:14:47 blair Exp $
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
   * Query the Groups Registry.
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
   * Grouper includes several default filters:
   * <p>
   * <ul>
   * <li>{@link ComplementFilter}</li>
   * <li>{@link GroupAnyAttributeFilter}</li>
   * <li>{@link GroupAttributeFilter}</li>
   * <li>{@link GroupCreatedAfterFilter}</li>
   * <li>{@link GroupCreatedBeforeFilter}</li>
   * <li>{@link GroupModifiedAfterFilter}</li>
   * <li>{@link GroupModifiedBeforeFilter}</li>
   * <li>{@link GroupNameFilter}</li>
   * <li>{@link IntersectionFilter}</li>
   * <li>{@link NullFilter}</li>
   * <li>{@link StemCreatedAfterFilter}</li>
   * <li>{@link StemCreatedBeforeFilter}</li>
   * <li>{@link StemNameFilter}</li>
   * <li>{@link UnionFilter}</li>
   * </ul>
   * @param   s       Query within this session context.
   * @param   filter  A {@link QueryFilter} specification.
   * @return  A {@link GrouperQuery} object.
   * @throws  QueryException
   */
  public static GrouperQuery createQuery(GrouperSession s, QueryFilter filter) 
    throws QueryException
  {
    GrouperSessionValidator.validate(s);
    return new GrouperQuery(s, filter);
  } // public static GrouperQuery createQuery(s, filter)


  // PUBLIC INSTANCE METHODS //

  /**
   * Get groups matching query filter.
   * <pre class="eg">
   * Set groups = gq.getGroups();
   * </pre>
   * @return  Set of matching {@link Group} objects.
   * @throws  QueryException
   */
  public Set getGroups() 
    throws QueryException
  {
    Set       groups      = new LinkedHashSet();
    Set       candidates  = this.filter.getResults(this.s);
    Object    o;
    Iterator  iter        = candidates.iterator();
    while (iter.hasNext()) {
      o = iter.next();
      if      (o.getClass().equals(Group.class)) {
        Group g = (Group) o;
        groups.add(g);
      }
      else {
        ErrorLog.error(GrouperQuery.class, E.NI + E.Q_G + o.getClass());
      }
    }
    return groups;
  } // public Set getGroups()

  /**
   * Get members matching query filter.
   * <pre class="eg">
   * Set members = gq.getMembers();
   * </pre>
   * @return  Set of matching {@link Member} objects.
   * @throws  QueryException
   */
  public Set getMembers() 
    throws QueryException
  {
    Set       members     = new LinkedHashSet();
    Set       mships      = new LinkedHashSet();
    Set       candidates  = this.filter.getResults(this.s);
    Object    o;
    Iterator  iter        = candidates.iterator();
    while (iter.hasNext()) {
      o = iter.next();
      if      (o.getClass().equals(Group.class))      {
        mships.addAll( ( (Group) o ).getMemberships() );
      }
      else if (o.getClass().equals(Membership.class)) {
        try {
          // Add directly to return set
          members.add( ( (Membership) o ).getMember() );
        }
        catch (MemberNotFoundException eMNF) {
          ErrorLog.error(GrouperQuery.class, eMNF.getMessage());
        }
      }
      else {
        ErrorLog.error(GrouperQuery.class, E.NI + E.Q_M + o.getClass());
      }
    }
    // Now extract members from any memberships we found
    try {
      Membership  ms;
      Iterator    iterMS  = mships.iterator();
      while (iterMS.hasNext()) {
        ms = (Membership) iterMS.next();
        members.add( ms.getMember() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new QueryException(
        "unable to retrieve members: " + eMNF.getMessage(), eMNF
      );
    }
    return members;
  } // public Set getMembers()

  /**
   * Get memberships matching query filter.
   * <pre class="eg">
   * Set memberships = gq.getMemberships();
   * </pre>
   * @return  Set of matching {@link Membership} objects.
   * @throws  QueryException
   */
  public Set getMemberships() 
    throws QueryException
  {
    Set       mships      = new LinkedHashSet();
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
        ErrorLog.error(GrouperQuery.class, E.NI + E.Q_MS + o.getClass());
      }
    }
    return mships;
  } // public Set getMemberships()

  /**
   * Get stems matching query filter.
   * <pre class="eg">
   * Set stems = gq.getStems();
   * </pre>
   * @return  Set of matching {@link Stem} objects.
   * @throws  QueryException
   */
  public Set getStems() 
    throws QueryException
  {
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
        ErrorLog.error(GrouperQuery.class, E.NI + E.Q_S + o.getClass());
      }
    }
    return stems;
  } // public Set getStems()

} // public class GrouperQuery

