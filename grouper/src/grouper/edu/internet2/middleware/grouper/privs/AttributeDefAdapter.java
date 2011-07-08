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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.subject.Subject;


/** 
 * Grouper AttributeDef Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link AttributeDef} class.
 * </p>
 * If you are implementing your own attribute def adapter, you should probably extend
 * BaseAccessAdapter
 * 
 * @author  blair christensen.
 * @version $Id: AttributeDefAdapter.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 */
public interface AttributeDefAdapter {

  // Public Instance Methods

  /**
   * Get all subjects with this privilege on this attribute definition.
   * <pre class="eg">
   * Set admins = ap.getSubjectsWithPriv(s, attrDef, AccessPrivilege.ADMIN);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   attributeDef     Get privileges on this attribute definition.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Subject} objects.
   * @throws  SchemaException
   */
  Set<Subject> getSubjectsWithPriv(GrouperSession s, AttributeDef attributeDef, Privilege priv)
    throws  SchemaException;

  /**
   * Get all attribute defs where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isAdmin = ap.getAttributeDefsWhereSubjectHasPriv(
   *     s, subj, AccessPrivilege.ADMIN
   *   );
   * }
   * catch (SchemaException eS) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   grouperSession     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link AttributeDef} objects.
   * @throws  SchemaException
   */
  Set<AttributeDef> getAttributeDefsWhereSubjectHasPriv(
    GrouperSession grouperSession, Subject subj, Privilege priv
  ) 
    throws  SchemaException;

  /**
   * Get all privileges held by this subject on this attribute definition.
   * <pre class="eg">
   * Set privs = ap.getPrivs(s, g, subj);
   * </pre>
   * @param   grouperSession     Get privileges within this session context.
   * @param   attributeDef     Get privileges on this attrDef.
   * @param   subj  Get privileges for this member.
   * @return  Set of privileges.
   */
  Set<AttributeDefPrivilege> getPrivs(GrouperSession grouperSession, AttributeDef attributeDef, Subject subj);

  /**
   * Grant the privilege to the subject on this attrDef.
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
   * @param   grouperSession     Grant privilege in this session context.
   * @param   attributeDef     Grant privilege on this attrDef.
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.   
   * @param uuid is uuid or null if assign one
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  void grantPriv(GrouperSession grouperSession, AttributeDef attributeDef, Subject subj, Privilege priv, String uuid)
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
            ; 

  /**
   * Check whether the subject has this privilege on this attrDef.
   * <pre class="eg">
   * try {
   *   ap.hasPriv(s, g, subject, AccessPrivilege.ADMIN);
   * }
   * catch (SchemaException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   grouperSession     Check privilege in this session context.
   * @param   attributeDef     Check privilege on this attrDef.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @return if has priv
   * @throws  SchemaException
   */
  boolean hasPriv(GrouperSession grouperSession, AttributeDef attributeDef, Subject subj, Privilege priv)
    throws SchemaException;

  /**
   * Revoke this privilege from everyone on this attrDef.
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
   * @param   grouperSession     Revoke privilege in this session context.
   * @param   attributeDef     Revoke privilege on this group.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  void revokePriv(GrouperSession grouperSession, AttributeDef attributeDef, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
            ;

  /**
   * Revoke the privilege from the subject on this attrDef.
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
   * @param   grouperSession     Revoke privilege in this session context.
   * @param   attributeDef     Revoke privilege on this attrDef.
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  void revokePriv(GrouperSession grouperSession, AttributeDef attributeDef, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
            ;
  
  /**
   * Copies privileges for subjects that have the specified privilege on g1 to g2.
   * @param grouperSession 
   * @param attributeDef1 
   * @param attributeDef2 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
   void privilegeCopy(GrouperSession grouperSession, AttributeDef attributeDef1, AttributeDef attributeDef2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException;
  
  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has ADMIN privilege to AttributeDef x, this method will result with subj2
   * having ADMIN privilege to AttributeDef x.
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
   * after HQL is run, filter attributeDefs.  If you are filtering in HQL, then dont filter here
   * @param grouperSession 
   * @param attributeDefs
   * @param subject which needs view access to the groups
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in AccessAdapter
   * @return the set of filtered groups
   */
  public Set<AttributeDef> postHqlFilterAttributeDefs(GrouperSession grouperSession, 
      Set<AttributeDef> attributeDefs, Subject subject, Set<Privilege> privInSet);
  
  /**
   * for an attrDef query, check to make sure the subject can see the records (if filtering HQL, you can do 
   * the postHqlFilterAttrDefs instead if you like).  Note, this joins to tables, so the queries should
   * probably be "distinct"
   * @param grouperSession 
   * @param subject which needs view access to the attrDefs
   * @param hqlTables is the select and part part (hql prefix)
   * @param hqlWhereClause is there where clause part of the query
   * @param hqlQuery 
   * @param attrDefColumn is the name of the attrDef column to join to
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all access privs).  There are pre-canned sets in AccessPrivilege
   * @return if the query was changed
   */
  public boolean hqlFilterAttrDefsWhereClause(GrouperSession grouperSession, 
      Subject subject, HqlQuery hqlQuery, StringBuilder hqlTables, StringBuilder hqlWhereClause, 
      String attrDefColumn, Set<Privilege> privInSet);

  /**
   * filter attribute assignments for things the subject can see, assume underlying assignments are ok to view
   * @param grouperSession 
   * @param attributeAssigns
   * @param subject
   * @return the memberships
   */
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(GrouperSession grouperSession, 
      Subject subject, Set<AttributeAssign> attributeAssigns);
  
  /**
   * filter pit attribute assignments for things the subject can see, assume underlying assignments are ok to view
   * @param grouperSession 
   * @param pitAttributeAssigns
   * @param subject
   * @return the pit attribute assignments
   */
  public Set<PITAttributeAssign> postHqlFilterPITAttributeAssigns(GrouperSession grouperSession, 
      Subject subject, Set<PITAttributeAssign> pitAttributeAssigns);
  

  /**
   * Revoke all access privileges that this subject has.
   * @param grouperSession
   * @param subject
   */
  public void revokeAllPrivilegesForSubject(GrouperSession grouperSession, Subject subject);

  /**
   * filter permissionEntries for things the subject can see, assume underlying assignments are ok to view
   * @param grouperSession 
   * @param permissionEntries
   * @param subject
   * @return the memberships
   */
  public Set<PermissionEntry> postHqlFilterPermissions(GrouperSession grouperSession, 
      Subject subject, Set<PermissionEntry> permissionEntries);
  
  /**
   * find the attributeDefs which do not have a certain privilege
   * @param grouperSession
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param considerAllSubject
   * @param sqlLikeString
   * @return the attributeDefs
   */
  Set<AttributeDef> getAttributeDefsWhereSubjectDoesntHavePrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString);

  /**
   * for an attributeDef query, check to make sure the subject cant see the records (if filtering HQL, you can do 
   * the postHqlFilterAttributeDefs instead if you like).
   * @param grouperSession 
   * @param subject which needs view access to the groups
   * @param hql is the select and part part (hql prefix)
   * @param hqlQuery 
   * @param attributeDefColumn is the name of the attributeDef column to join to
   * @param privilege find a privilege which is in this set 
   * (e.g. attributeDef privs).  
   * @param considerAllSubject if true, then consider GrouperAll when seeing if doesnt have privilege, else do consider
   * @return if the query was changed
   */
  public boolean hqlFilterAttributeDefsNotWithPrivWhereClause(GrouperSession grouperSession, 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, 
      String attributeDefColumn, Privilege privilege, boolean considerAllSubject);

  /**
   * get a list of privilege subjects, there are no results with the same subject/privilege combination
   * @param grouperSession grouper session
   * @param attributeDef to search on
   * @param privileges if blank, get all
   * @param membershipType if immediate, effective, or blank for all
   * @param queryPaging if a certain page should be returned based on subject
   * @param additionalMembers additional members to query that the user is finding or adding
   * @return the privilege subject combinations
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(GrouperSession grouperSession, 
      AttributeDef attributeDef, Set<Privilege> privileges, 
      MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers);
  

}

