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

package edu.internet2.middleware.grouper.privs;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.subject.Subject;


/** 
 * Facade for the {@link AccessAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessResolver.java,v 1.12 2009-08-29 15:57:59 shilen Exp $
 * @since   1.2.1
 */
public interface AccessResolver {

  /** clean up  resources, session is stopped */
  public void stop();
  
  /**
   * get a reference to the session
   * @return the session
   */
  public GrouperSession getGrouperSession();
  
  /**
   * flush cache if caching resolver
   */
  public void flushCache();
  
  /**
   * Get all groups where <i>subject</i> has <i>privilege</i>.
   * <p/>
   * @param subject 
   * @param privilege 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getGroupsWhereSubjectHasPriv(edu.internet2.middleware.grouper.GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * find the groups which do not have a certain privilege
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param considerAllSubject 
   * @param sqlLikeString
   * @return the groups
   */
  Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString);
  
  /**
   * Get all stems which have groups where <i>subject</i> has <i>privilege</i>.
   * <p/>
   * @param subject 
   * @param privilege 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getGroupsWhereSubjectHasPriv(edu.internet2.middleware.grouper.GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Get all privileges <i>subject</i> has on <i>group</i>.
   * <p/>
   * @param group 
   * @param subject 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getPrivs(GrouperSession, Group, Subject)
   * @since   1.2.1
   */
  Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException;

  /**
   * Get all subjects with <i>privilege</i> on <i>group</i>.
   * <p/>
   * @param group 
   * @param privilege 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getSubjectsWithPriv(GrouperSession, Group, Privilege)
   * @since   1.2.1
   */
  Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Grant <i>privilege</i> to <i>subject</i> on <i>group</i>.
   * <p/>
   * @param group 
   * @param subject 
   * @param privilege 
   * @param uuid send uuid if known, else null
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be granted.
   * @see     AccessAdapter#grantPriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  void grantPrivilege(Group group, Subject subject, Privilege privilege, String uuid)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Check whether <i>subject</i> has <i>privilege</i> on <i>group</i>.
   * <p/>
   * @param group 
   * @param subject 
   * @param privilege 
   * @return boolean
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#hasPriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Revoke <i>privilege</i> from all subjects on <i>group</i>.
   * <p/>
   * @param group 
   * @param privilege 
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     AccessAdapter#revokePriv(GrouperSession, Group, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Revoke <i>privilege</i> from <i>subject</i> on <i>group</i>.
   * <p/>
   * @param group 
   * @param subject 
   * @param privilege 
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     AccessAdapter#revokePriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Copies privileges for subjects that have the specified privilege on g1 to g2.
   * @param g1 
   * @param g2 
   * @param priv 
   * @throws IllegalArgumentException
   * @throws UnableToPerformException 
   */
   void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException;
  
  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has ADMIN privilege to Group x, this method will result with subj2
   * having ADMIN privilege to Group x.
   * @param subj1
   * @param subj2
   * @param priv 
   * @throws IllegalArgumentException
   * @throws UnableToPerformException 
   */
   void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException;

  /**
   * after HQL is run, filter groups.  If you are filtering in HQL, then dont filter here
   * @param groups
   * @param subject which needs view access to the groups
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in AccessAdapter
   * @return the set of filtered groups
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject, 
      Set<Privilege> privInSet);

  /**
   * after HQL is run, filter stems that have groups with privs.  If you are filtering
   * HQL, then dont filter here.
   * @param stems
   * @param subject
   * @param inPrivSet
   * @return  the set of filtered stems
   */
  public Set<Stem> postHqlFilterStemsWithGroups(
      Set<Stem> stems, Subject subject, Set<Privilege> inPrivSet);
  
  /**
   * for a group query, check to make sure the subject can see the records (if filtering HQL, you can do 
   * the postHqlFilterGroups instead if you like)
   * @param subject which needs view access to the groups
   * @param hqlQuery 
   * @param hql the select and current from part
   * @param groupColumn is the name of the group column to join to
   * @param privInSet find a privilege which is in this set (e.g. for view, send all access privs)
   * @return if the statement was changed
   */
  public boolean hqlFilterGroupsWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Set<Privilege> privInSet);

  /**
   * for a group query, check to make sure the subject cant see the records
   * @param subject which needs view access to the groups
   * @param hqlQuery 
   * @param hql the select and current from part
   * @param groupColumn is the name of the group column to join to
   * @param privilege find a privilege which is in this set (e.g. for view, send all access privs)
   * @param considerAllSubject if true, then consider GrouperAll when seeign if subject has priv, else do not
   * @return if the statement was changed
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject);
  
  /**
   * filter memberships for things the subject can see
   * @param memberships
   * @param subject
   * @return the memberships
   */
  public Set<Membership> postHqlFilterMemberships(
      Subject subject, Set<Membership> memberships);


  /**
   * Revoke all access privileges that this subject has.
   * @param subject
   */
  public void revokeAllPrivilegesForSubject(Subject subject);

  /**
   * get a list of privilege subjects, there are no results with the same subject
   * @param group to search on
   * @param privileges if blank, get all
   * @param membershipType if immediate, effective, or blank for all
   * @param queryPaging if a certain page should be returned, based on subject
   * @param additionalMembers additional members to query that the user is finding or adding
   * @return the privilege subject combinations
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(
      Group group, Set<Privilege> privileges, 
      MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers);
}

