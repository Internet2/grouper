/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: BaseAccessAdapter.java,v 1.2 2009-04-13 16:53:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Base class for access adapter
 */
public abstract class BaseAccessAdapter implements AccessAdapter {

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#postHqlFilterGroups(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(GrouperSession grouperSession, Set<Group> inputGroups, 
      Subject subject, Set<Privilege> privInSet) {
    

    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0 || GrouperUtil.length(inputGroups) == 0) {
      return inputGroups;
    }

    Set<Group>  groups  = new LinkedHashSet();
    for ( Group child : inputGroups ) {
      
      if ( PrivilegeHelper.hasPrivilege(
          GrouperSession.staticGrouperSession().internal_getRootSession(), child, subject, privInSet ) ) {
        groups.add(child);
      }
    }
    return groups;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#postHqlFilterStemsWithGroups(edu.internet2.middleware.grouper.GrouperSession, java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStemsWithGroups(GrouperSession grouperSession,
      Set<Stem> stems, Subject subject, Set<Privilege> inPrivSet) {
    //no privs no filter
    if (GrouperUtil.length(inPrivSet) == 0 || GrouperUtil.length(stems) == 0) {
      return stems;
    }

    Set<Stem>  result  = new LinkedHashSet();
    for ( Stem childStem : stems ) {
      Set<Group> theGroups = GrouperDAOFactory.getFactory().getGroup().getAllGroupsMembershipSecure(
          null, grouperSession.internal_getRootSession(), subject, inPrivSet, 
          null, false, childStem, Scope.ONE);
      if ( GrouperUtil.length(theGroups) > 0 ) {
        result.add(childStem);
      }
    }
    return result;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#hqlFilterGroupsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {
    //by default dont change the HQL
    return false;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#hqlFilterGroupsNotWithPrivWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject) {
    //by default dont change the HQL
    return false;
  }

  
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#postHqlFilterMemberships(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(
      GrouperSession grouperSession, Subject subject,
      Set<Membership> memberships) {

    return PrivilegeHelper.canViewMemberships(grouperSession, memberships);
  }


}
