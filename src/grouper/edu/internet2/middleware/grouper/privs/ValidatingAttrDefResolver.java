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

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides parameter validation for {@link AttributeDefResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingAttrDefResolver.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class ValidatingAttrDefResolver extends AttributeDefResolverDecorator {

  /** */
  private ParameterHelper param;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public ValidatingAttrDefResolver(AttributeDefResolver resolver) {
    super(resolver);
    this.param = new ParameterHelper();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#getAttributeDefsWhereSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getAttributeDefsWhereSubjectHasPrivilege(subject,
        privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef, Subject subject)
      throws IllegalArgumentException {
    this.param.notNullAttributeDef(attributeDef).notNullSubject(subject);
    return super.getDecoratedResolver().getPrivileges(attributeDef, subject);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullAttributeDef(attributeDef).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getSubjectsWithPrivilege(attributeDef, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#grantPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void grantPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullAttributeDef(attributeDef).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().grantPrivilege(attributeDef, subject, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullAttributeDef(attributeDef).notNullSubject(subject).notNullPrivilege(privilege);
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.hasPrivilege(attributeDef, subject, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullAttributeDef(attributeDef).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(attributeDef, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttrDefs(Set, Subject, Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs, Subject subject,
      Set<Privilege> privInSet) {
    this.param.notNullSubject(subject);
    return super.getDecoratedResolver().postHqlFilterAttrDefs(attributeDefs, subject, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullAttributeDef(attributeDef).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(attributeDef, subject, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#privilegeCopy(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(AttributeDef attributeDef1, AttributeDef attributeDef2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullAttributeDef(attributeDef1).notNullAttributeDef(attributeDef2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(attributeDef1, attributeDef2, priv);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullSubject(subj1).notNullSubject(subj2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String attrDefColumn, Set<Privilege> privInSet) {

    this.param.notNullSubject(subject).notNullHqlQuery(hqlQuery);

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.hqlFilterAttrDefsWhereClause(subject, hqlQuery, hql,
        attrDefColumn, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttrDefs(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Subject subject,
      Set<AttributeDef> attributeDefs) {

    this.param.notNullSubject(subject);

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.postHqlFilterAttrDefs(subject, attributeDefs);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.param.notNullSubject(subject);
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }

}
