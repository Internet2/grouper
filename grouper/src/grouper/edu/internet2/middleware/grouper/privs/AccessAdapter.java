/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.subject.Subject;


/** 
 * Grouper Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Group} class.
 * </p>
 * If you are implementing your own access adapter, you should probably extend
 * BaseAccessAdapter
 * 
 * @author  blair christensen.
 * @version $Id: AccessAdapter.java,v 1.6 2009-08-29 15:57:59 shilen Exp $
 */
public interface AccessAdapter {

  // Public Instance Methods

  /**
   * Get all subjects with this privilege on this group.
   * <pre class="eg">
   * Set admins = ap.getSubjectsWithPriv(s, g, AccessPrivilege.ADMIN);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   g     Get privileges on this group.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Subject} objects.
   * @throws  SchemaException
   */
  Set<Subject> getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv)
    throws  SchemaException;

  /**
   * Get all groups where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isAdmin = ap.getGroupsWhereSubjectHasPriv(
   *     s, subj, AccessPrivilege.ADMIN
   *   );
   * }
   * catch (SchemaException eS) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Group} objects.
   * @throws  SchemaException
   */
  Set<Group> getGroupsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException;

  /**
   * find the groups which do not have a certain privilege
   * @param grouperSession
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param considerAllSubject
   * @param sqlLikeString 
   * @return the groups
   */
  Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString);
  
  /**
   * get stems where a group exists where the subject has privilege
   * @param grouperSession
   * @param subject
   * @param privilege
   * @return the stems
   */
  Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(
      GrouperSession grouperSession, Subject subject, Privilege privilege);
  
  /**
   * Get all privileges held by this subject on this group.
   * <pre class="eg">
   * Set privs = ap.getPrivs(s, g, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   g     Get privileges on this group.
   * @param   subj  Get privileges for this member.
   * @return  Set of privileges.
   */
  Set<AccessPrivilege> getPrivs(GrouperSession s, Group g, Subject subj);

  /**
   * Grant the privilege to the subject on this group.
   * <pre class="eg">
   * try {
   *   ap.grantPriv(s, g, subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Unable to grant the privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to grant the privilege
   * }
   * catch (SchemaException e2) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Grant privilege in this session context.
   * @param   g     Grant privilege on this group.
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.   
   * @param uuid is uuid or null if generated
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  void grantPriv(GrouperSession s, Group g, Subject subj, Privilege priv, String uuid)
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
            ; 

  /**
   * Check whether the subject has this privilege on this group.
   * <pre class="eg">
   * try {
   *   ap.hasPriv(s, g, subject, AccessPrivilege.ADMIN);
   * }
   * catch (SchemaException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   g     Check privilege on this group.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @return if has priv
   * @throws  SchemaException
   */
  boolean hasPriv(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws SchemaException;

  /**
   * Revoke this privilege from everyone on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, AccessPrivilege.ADMIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   g     Revoke privilege on this group.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
            ;

  /**
   * Revoke the privilege from the subject on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, subj, AccessPrivilege.ADMIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   g     Revoke privilege on this group.
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  void revokePriv(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
            ;
  
  /**
   * Copies privileges for subjects that have the specified privilege on g1 to g2.
   * @param s 
   * @param g1 
   * @param g2 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
   void privilegeCopy(GrouperSession s, Group g1, Group g2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException;
  
  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has ADMIN privilege to Group x, this method will result with subj2
   * having ADMIN privilege to Group x.
   * @param s 
   * @param subj1
   * @param subj2
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
   void privilegeCopy(GrouperSession s, Subject subj1, Subject subj2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException;

  /**
   * after HQL is run, filter groups.  If you are filtering in HQL, then dont filter here
   * @param grouperSession 
   * @param groups
   * @param subject which needs view access to the groups
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in AccessAdapter
   * @return the set of filtered groups
   */
  public Set<Group> postHqlFilterGroups(GrouperSession grouperSession, 
      Set<Group> groups, Subject subject, Set<Privilege> privInSet);
  
  /**
   * after HQL is run, filter stems with groups.  If you are filtering in HQL, then dont filter here
   * @param grouperSession
   * @param stems
   * @param subject
   * @param inPrivSet
   * @return the stems
   */
  public Set<Stem> postHqlFilterStemsWithGroups(GrouperSession grouperSession, 
      Set<Stem> stems, Subject subject, Set<Privilege> inPrivSet);
  
  /**
   * for a group query, check to make sure the subject can see the records (if filtering HQL, you can do 
   * the postHqlFilterGroups instead if you like).  Note, this joins to tables, so the queries should
   * probably be "distinct"
   * @param grouperSession 
   * @param subject which needs view access to the groups
   * @param hql is the select and part part (hql prefix)
   * @param hqlQuery 
   * @param groupColumn is the name of the group column to join to
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in AccessPrivilege
   * @return if the query was changed
   */
  public boolean hqlFilterGroupsWhereClause(GrouperSession grouperSession, 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, 
      String groupColumn, Set<Privilege> privInSet);

  /**
   * for a group query, check to make sure the subject cant see the records (if filtering HQL, you can do 
   * the postHqlFilterGroups instead if you like).
   * @param grouperSession 
   * @param subject which needs view access to the groups
   * @param hql is the select and part part (hql prefix)
   * @param hqlQuery 
   * @param groupColumn is the name of the group column to join to
   * @param privilege find a privilege which is in this set 
   * (e.g. for view, send view).  
   * @param considerAllSubject if true, then consider GrouperAll when seeing if doesnt have privilege, else do consider
   * @return if the query was changed
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(GrouperSession grouperSession, 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, 
      String groupColumn, Privilege privilege, boolean considerAllSubject);

  /**
   * filter memberships for things the subject can see
   * @param grouperSession 
   * @param memberships
   * @param subject
   * @return the memberships
   */
  public Set<Membership> postHqlFilterMemberships(GrouperSession grouperSession, 
      Subject subject, Set<Membership> memberships);
  

  /**
   * Revoke all access privileges that this subject has.
   * @param grouperSession
   * @param subject
   */
  public void revokeAllPrivilegesForSubject(GrouperSession grouperSession, Subject subject);

  /**
   * get a list of privilege subjects, there are no results with the same subject/privilege combination
   * @param grouperSession grouper session
   * @param group to search on
   * @param privileges if blank, get all
   * @param membershipType if immediate, effective, or blank for all
   * @param queryPaging if a certain page should be returned based on subject
   * @param additionalMembers additional members to query that the user is finding or adding
   * @return the privilege subject combinations
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(GrouperSession grouperSession, 
      Group group, Set<Privilege> privileges, 
      MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers);
}

