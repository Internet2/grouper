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
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.subject.Subject;


/** 
 * Facade for the {@link AttributeDefAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AttributeDefResolver.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public interface AttributeDefResolver {

  /** clean up resources, session is stopped */
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
   * Get all attributedefs where <i>subject</i> has <i>privilege</i>.
   * <p/>
   * @param subject 
   * @param privilege 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AttributeDefAdapter#getAttributeDefsWhereSubjectHasPriv(GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  Set<AttributeDef> getAttributeDefsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Get all privileges <i>subject</i> has on <i>attributeDef</i>.
   * <p/>
   * @param attributeDef 
   * @param subject 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AttributeDefAdapter#getPrivs(GrouperSession, AttributeDef, Subject)
   * @since   1.2.1
   */
  Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef, Subject subject)
    throws  IllegalArgumentException;

  /**
   * Get all subjects with <i>privilege</i> on <i>attributeDef</i>.
   * <p/>
   * @param attributeDef 
   * @param privilege 
   * @return the set
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AttributeDefAdapter#getSubjectsWithPriv(GrouperSession, AttributeDef, Privilege)
   * @since   1.2.1
   */
  Set<Subject> getSubjectsWithPrivilege(AttributeDef attributeDef, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Grant <i>privilege</i> to <i>subject</i> on <i>attributeDef</i>.
   * <p/>
   * @param attributeDef 
   * @param subject 
   * @param privilege 
   * @param uuid is uuid or null for assigned
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be granted.
   * @see     AttributeDefAdapter#grantPriv(GrouperSession, AttributeDef, Subject, Privilege, String)
   * @since   1.2.1
   */
  void grantPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege, String uuid)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Check whether <i>subject</i> has <i>privilege</i> on <i>attributeDef</i>.
   * <p/>
   * @param attributeDef 
   * @param subject 
   * @param privilege 
   * @return boolean
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AttributeDefAdapter#hasPriv(GrouperSession, AttributeDef, Subject, Privilege)
   * @since   1.2.1
   */
  boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Revoke <i>privilege</i> from all subjects on <i>attributeDef</i>.
   * <p/>
   * @param attributeDef 
   * @param privilege 
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     AttributeDefAdapter#revokePriv(GrouperSession, AttributeDef, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(AttributeDef attributeDef, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Revoke <i>privilege</i> from <i>subject</i> on <i>attributeDef</i>.
   * <p/>
   * @param attributeDef 
   * @param subject 
   * @param privilege 
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     AttributeDefAdapter#revokePriv(GrouperSession, AttributeDef, Subject, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Copies privileges for subjects that have the specified privilege on g1 to g2.
   * @param attributeDef1 
   * @param attributeDef2 
   * @param priv 
   * @throws IllegalArgumentException
   * @throws UnableToPerformException 
   */
   void privilegeCopy(AttributeDef attributeDef1, AttributeDef attributeDef2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException;
  
  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has ATTR_ADMIN privilege to AttributeDef x, this method will result with subj2
   * having ATTR_ADMIN privilege to AttributeDef x.
   * @param subj1
   * @param subj2
   * @param priv 
   * @throws IllegalArgumentException
   * @throws UnableToPerformException 
   */
   void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException;

  /**
   * after HQL is run, filter attributeDefs.  If you are filtering in HQL, then dont filter here
   * @param attributeDefs
   * @param subject which needs view access to the attribute defs
   * @param privInSet find a privilege which is in this set 
   * (e.g. for view, send all attrDef privs).  There are pre-canned sets in AttributeDefAdapter
   * @return the set of filtered attrDefs
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs, Subject subject, 
      Set<Privilege> privInSet);

  /**
   * for an attrDef query, check to make sure the subject can see the records (if filtering HQL, you can do 
   * the postHqlFilterAttDefs instead if you like)
   * @param subject which needs view access to the attrDefs
   * @param hqlQuery 
   * @param hqlTables the select and current from part
   * @param hqlWhereClause is there where clause part of the query
   * @param attributeDefColumn is the name of the attributeDef column to join to
   * @param privInSet find a privilege which is in this set (e.g. for view, send all attrDef privs)
   * @return if the statement was changed
   */
  public boolean hqlFilterAttrDefsWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hqlTables, StringBuilder hqlWhereClause, String attributeDefColumn, Set<Privilege> privInSet);

  /**
   * filter attributeDefs for things the subject can see
   * @param attributeAssigns
   * @param subject
   * @return the memberships
   */
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(
      Subject subject, Set<AttributeAssign> attributeAssigns);
  
  /**
   * filter pit attribute assignments for things the subject can see
   * @param pitAttributeAssigns
   * @param subject
   * @return the pit attribute assignments
   */
  public Set<PITAttributeAssign> postHqlFilterPITAttributeAssigns(
      Subject subject, Set<PITAttributeAssign> pitAttributeAssigns);

  /**
   * filter permissions for things the subject can see
   * @param permissionsEntries
   * @param subject
   * @return the memberships
   */
  public Set<PermissionEntry> postHqlFilterPermissions(
      Subject subject, Set<PermissionEntry> permissionsEntries);
  
  /**
   * Revoke all attrDef privileges that this subject has.
   * @param subject
   */
  public void revokeAllPrivilegesForSubject(Subject subject);
  
  /**
   * find the attributeDefs which do not have a certain privilege
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param considerAllSubject 
   * @param sqlLikeString
   * @return the attributeDefs
   */
  Set<AttributeDef> getAttributeDefsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString);
  
  /**
   * for an attribute def query, check to make sure the subject cant see the records
   * @param subject which needs view access to the groups
   * @param hqlQuery 
   * @param hql the select and current from part
   * @param attributeDefColumn is the name of the attributeDef column to join to
   * @param privilege find a privilege which is in this set (e.g. for view, attr view)
   * @param considerAllSubject if true, then consider GrouperAll when seeign if subject has priv, else do not
   * @return if the statement was changed
   */
  public boolean hqlFilterAttributeDefsNotWithPrivWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String attributeDefColumn, Privilege privilege, boolean considerAllSubject);

  /**
   * get a list of privilege subjects, there are no results with the same subject
   * @param attributeDef to search on
   * @param privileges if blank, get all
   * @param membershipType if immediate, effective, or blank for all
   * @param queryPaging if a certain page should be returned, based on subject
   * @param additionalMembers additional members to query that the user is finding or adding
   * @return the privilege subject combinations
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(
      AttributeDef attributeDef, Set<Privilege> privileges, 
      MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers);
  
}

