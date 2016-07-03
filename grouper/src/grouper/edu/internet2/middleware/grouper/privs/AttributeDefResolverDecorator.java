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
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.subject.Subject;


/**
 * Decorator for {@link AttributeDefResolver}.
 * 
 * @author  blair christensen.
 * @version $Id: AttributeDefResolverDecorator.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public abstract class AttributeDefResolverDecorator implements AttributeDefResolver {

  /** */
  private AttributeDefResolver  decorated;
  
  /** */
  private ParameterHelper param;


 
  /**
   * @param   resolver  <i>AttributeDefResolver</i> to decorate.
   * @throws  IllegalArgumentException if <i>resolver</i> is null.
   * @since   1.2.1
   */
  public AttributeDefResolverDecorator(AttributeDefResolver resolver) 
    throws  IllegalArgumentException
  {
    this.param      = new ParameterHelper();
    this.param.notNullAttrDefResolver(resolver);
    this.decorated  = resolver;
  }


  /**
   * @return  Decorated <i>AttributeDefResolver</i>.
   * @throws  IllegalStateException if no decorated <i>AttributeDefResolver</i>.
   * @since   1.2.1
   */
  public AttributeDefResolver getDecoratedResolver() 
    throws  IllegalStateException
  {
    if (this.decorated == null) { 
      throw new IllegalStateException("null decorated AttrDefResolver");
    }
    return this.decorated;
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#flushCache()
   */
  public void flushCache() {
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getAttributeDefsWhereSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectHasPrivilege(Subject subject,
      Privilege privilege) throws IllegalArgumentException {
    return this.getDecoratedResolver().getAttributeDefsWhereSubjectHasPrivilege(subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    return this.getDecoratedResolver().getGrouperSession();
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef,
      Subject subject) throws IllegalArgumentException {
    return this.getDecoratedResolver().getPrivileges(attributeDef, subject);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(AttributeDef attributeDef,
      Privilege privilege) throws IllegalArgumentException {
    return this.getDecoratedResolver().getSubjectsWithPrivilege(attributeDef, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#grantPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPrivilege(AttributeDef attributeDef, Subject subject,
      Privilege privilege, String uuid) throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().grantPrivilege(attributeDef, subject, privilege, uuid);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject,
      Privilege privilege) throws IllegalArgumentException {
    return this.getDecoratedResolver().hasPrivilege(attributeDef, subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hqlTables, StringBuilder hqlWhereClause, String attributeDefColumn, Set<Privilege> privInSet) {
    return this.getDecoratedResolver().hqlFilterAttrDefsWhereClause(subject, hqlQuery, hqlTables, hqlWhereClause, attributeDefColumn, privInSet);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterAttrDefs(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs,
      Subject subject, Set<Privilege> privInSet) {
    return this.getDecoratedResolver().postHqlFilterAttrDefs(attributeDefs, subject, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterPermissions(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PermissionEntry> postHqlFilterPermissions(Subject subject,
      Set<PermissionEntry> permissionsEntries) {
    return this.getDecoratedResolver().postHqlFilterPermissions(subject, permissionsEntries);
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterAttributeAssigns(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(Subject subject,
      Set<AttributeAssign> attributeDefs) {
    return this.getDecoratedResolver().postHqlFilterAttributeAssigns(subject, attributeDefs);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterPITAttributeAssigns(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PITAttributeAssign> postHqlFilterPITAttributeAssigns(Subject subject,
      Set<PITAttributeAssign> pitAttributeAssigns) {
    return this.getDecoratedResolver().postHqlFilterPITAttributeAssigns(subject, pitAttributeAssigns);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#privilegeCopy(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(AttributeDef attributeDef1, AttributeDef attributeDef2,
      Privilege priv) throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().privilegeCopy(attributeDef1, attributeDef2, priv);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().revokePrivilege(attributeDef, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Subject subject,
      Privilege privilege) throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().revokePrivilege(attributeDef, subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#retrievePrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, java.util.Set, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.internal.dao.QueryPaging, java.util.Set)
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(AttributeDef attributeDef,
      Set<Privilege> privileges, MembershipType membershipType, QueryPaging queryPaging,
      Set<Member> additionalMembers) {
    return this.getDecoratedResolver().retrievePrivileges(attributeDef, privileges, 
        membershipType, queryPaging, additionalMembers);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#stop()
   */
  public void stop() {
    this.getDecoratedResolver().stop();
  }

  /**
   * @see AttributeDefResolver#getAttributeDefsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.getDecoratedResolver().getAttributeDefsWhereSubjectDoesntHavePrivilege(stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hqlFilterAttributeDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege)
   */
  public boolean hqlFilterAttributeDefsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject) {
    return this.getDecoratedResolver().hqlFilterAttributeDefsNotWithPrivWhereClause(subject, hqlQuery, hql, groupColumn, privilege, considerAllSubject);
  }
  
}

