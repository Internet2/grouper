/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.privs;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
import edu.internet2.middleware.subject.Subject;

/** 
 * Class implementing wrapper around {@link AccessAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AttributeDefWrapper.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class AttributeDefWrapper implements AttributeDefResolver {

  /** */
  private AttributeDefAdapter attributeDefAdapter;

  /** */
  private GrouperSession grouperSession;

  /** */
  private ParameterHelper parameterHelper;

  /**
   * Facade around {@link AccessAdapter} that implements {@link AccessResolver}.
   * @param session 
   * @param attributeDefAdapter 
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public AttributeDefWrapper(GrouperSession session,
      AttributeDefAdapter attributeDefAdapter)
      throws IllegalArgumentException {
    this.parameterHelper = new ParameterHelper();
    this.parameterHelper.notNullGrouperSession(session).notNullAttrDefAdapter(
        attributeDefAdapter);
    this.grouperSession = session;
    this.attributeDefAdapter = attributeDefAdapter;
  }

  /**
   * @param subject 
   * @param privilege 
   * @return set of attributeDef
   * @throws IllegalArgumentException 
   * @see     AttributeDefResolver#getAttributeDefsWhereSubjectHasPrivilege(Subject, Privilege)
   * @see     AttributeDefAdapter#getAttributeDefsWhereSubjectHasPriv(GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectHasPrivilege(Subject subject,
      Privilege privilege)
      throws IllegalArgumentException {
    try {
      return this.attributeDefAdapter.getAttributeDefsWhereSubjectHasPriv(
          this.grouperSession, subject, privilege);
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef, Subject subject)
      throws IllegalArgumentException {
    return this.attributeDefAdapter.getPrivs(this.grouperSession, attributeDef, subject);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException {
    try {
      return this.attributeDefAdapter.getSubjectsWithPriv(this.grouperSession, attributeDef,
          privilege);
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#grantPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException {
    try {
      this.attributeDefAdapter.grantPriv(this.grouperSession, attributeDef, subject, privilege, uuid);
    } catch (GrantPrivilegeException eGrant) {
      if (eGrant instanceof GrantPrivilegeAlreadyExistsException) {
        throw new UnableToPerformAlreadyExistsException(eGrant.getMessage(), eGrant);
      }
      throw new UnableToPerformException(eGrant.getMessage(), eGrant);
    } catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException(ePrivs.getMessage(), ePrivs);
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    try {
      return this.attributeDefAdapter.hasPriv(this.grouperSession, attributeDef, subject,
          privilege);
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    try {
      this.attributeDefAdapter.revokePriv(this.grouperSession, attributeDef, privilege);
    } catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException(ePrivs.getMessage(), ePrivs);
    } catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException(eRevoke.getMessage(), eRevoke);
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    try {
      this.attributeDefAdapter.revokePriv(this.grouperSession, attributeDef, subject, privilege);
    } catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException(ePrivs.getMessage(), ePrivs);
    } catch (RevokePrivilegeAlreadyRevokedException eRevoke) {
      throw new UnableToPerformAlreadyExistsException(eRevoke.getMessage(), eRevoke);
    } catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException(eRevoke.getMessage(), eRevoke);
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterAttrDefs(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs, Subject subject,
      Set<Privilege> privInSet) {
    return this.attributeDefAdapter.postHqlFilterAttributeDefs(this.grouperSession, attributeDefs,
        subject, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#privilegeCopy(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(AttributeDef attributeDef1, AttributeDef attributeDef2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    try {
      this.attributeDefAdapter.privilegeCopy(this.grouperSession, attributeDef1, attributeDef2, priv);
    } catch (InsufficientPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (GrantPrivilegeAlreadyExistsException e) {
      throw new UnableToPerformAlreadyExistsException(e.getMessage(), e);
    } catch (GrantPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (SchemaException e) {
      throw new GrouperException("unexpected condition", e);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    try {
      this.attributeDefAdapter.privilegeCopy(this.grouperSession, subj1, subj2, priv);
    } catch (InsufficientPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (GrantPrivilegeAlreadyExistsException e) {
      throw new UnableToPerformAlreadyExistsException(e.getMessage(), e);
    } catch (GrantPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (SchemaException e) {
      throw new GrouperException("unexpected condition", e);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#flushCache()
   */
  public void flushCache() {
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(
      Subject subject, HqlQuery hqlQuery, StringBuilder hqlTables, StringBuilder hqlWhereClause, String attrDefColumn,
      Set<Privilege> privInSet) {
    return this.attributeDefAdapter.hqlFilterAttrDefsWhereClause(this.grouperSession,
        subject, hqlQuery, hqlTables, hqlWhereClause, attrDefColumn, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterAttributeAssigns(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(Subject subject,
      Set<AttributeAssign> attributeAssigns) {
    return this.attributeDefAdapter.postHqlFilterAttributeAssigns(this.grouperSession,
        subject, attributeAssigns);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterPITAttributeAssigns(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PITAttributeAssign> postHqlFilterPITAttributeAssigns(Subject subject,
      Set<PITAttributeAssign> pitAttributeAssigns) {
    return this.attributeDefAdapter.postHqlFilterPITAttributeAssigns(this.grouperSession,
        subject, pitAttributeAssigns);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#stop()
   */
  public void stop() {
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.attributeDefAdapter.revokeAllPrivilegesForSubject(this.grouperSession, subject);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterPermissions(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PermissionEntry> postHqlFilterPermissions(Subject subject,
      Set<PermissionEntry> permissionsEntries) {
    return this.attributeDefAdapter.postHqlFilterPermissions(this.grouperSession,
        subject, permissionsEntries);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterPITPermissions(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PITPermissionAllView> postHqlFilterPITPermissions(Subject subject,
      Set<PITPermissionAllView> pitPermissionsEntries) {
    return this.attributeDefAdapter.postHqlFilterPITPermissions(this.grouperSession,
        subject, pitPermissionsEntries);
  }

  /**
   * @see AttributeDefResolver#getAttributeDefsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.attributeDefAdapter.getAttributeDefsWhereSubjectDoesntHavePrivilege(
        this.grouperSession, stemId, scope, subject, privilege, considerAllSubject, 
        sqlLikeString);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hqlFilterAttributeDefsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterAttributeDefsNotWithPrivWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String attributeDefColumn, Privilege privilege, boolean considerAllSubject) {
    return this.attributeDefAdapter.hqlFilterAttributeDefsNotWithPrivWhereClause(this.grouperSession, subject, hqlQuery, hql, attributeDefColumn, privilege, considerAllSubject);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#retrievePrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, java.util.Set, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.internal.dao.QueryPaging, Set)
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(AttributeDef attributeDef,
      Set<Privilege> privileges, MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers) {
    return this.attributeDefAdapter.retrievePrivileges(this.grouperSession, 
        attributeDef, privileges, membershipType, queryPaging, additionalMembers);
  }

}
