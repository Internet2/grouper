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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides <i>GrouperAll</i> privilege resolution for {@link AttributeDefResolver}.
 * <p/>
 * @author  mchyzer
 * @version $Id: GrouperAllAttrDefResolver.java,v 1.2 2009-09-28 05:06:46 mchyzer Exp $
 */
public class GrouperAllAttrDefResolver extends AttributeDefResolverDecorator {


  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }

  /** */
  private Subject all;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperAllAttrDefResolver(AttributeDefResolver resolver) {
    super(resolver);
    this.all = SubjectFinder.findAllSubject();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getAttributeDefsWhereSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectHasPrivilege(Subject subject,
      Privilege privilege)
      throws IllegalArgumentException {
    Set<AttributeDef> attributeDefs = super.getDecoratedResolver().getAttributeDefsWhereSubjectHasPrivilege(
        subject, privilege);
    //this happens further down in GrouperNonDbAttrDefAdapter
    //    attributeDefs.addAll(super.getDecoratedResolver().getAttributeDefsWhereSubjectHasPrivilege(
    //        this.all, privilege));
    return attributeDefs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef, Subject subject)
      throws IllegalArgumentException {
    // TODO 20070820 include GrouperAll privs?
    //2007-11-02 Gary Brown
    //I assume this is what blair intended - have removed
    //the All privileges from the GrouperAccessAdapter

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    Set<AttributeDefPrivilege> allPrivs = fixPrivs(decoratedResolver.getPrivileges(
        attributeDef, this.all), subject);
    allPrivs.addAll(decoratedResolver.getPrivileges(attributeDef, subject));
    return allPrivs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException {
    return super.getDecoratedResolver().getSubjectsWithPrivilege(attributeDef, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    if (super.getDecoratedResolver().hasPrivilege(attributeDef, this.all, privilege)) {
      return true;
    }
    return super.getDecoratedResolver().hasPrivilege(attributeDef, subject, privilege);
  }

  /**
   * 
   * @param privs
   * @param subj
   * @return the set, never null
   */
  private Set<AttributeDefPrivilege> fixPrivs(Set<AttributeDefPrivilege> privs, Subject subj) {
    Set<AttributeDefPrivilege> fixed = new HashSet<AttributeDefPrivilege>();
    Iterator<AttributeDefPrivilege> it = privs.iterator();
    AttributeDefPrivilege oldPriv;
    AttributeDefPrivilege newPriv;
    while (it.hasNext()) {
      oldPriv = it.next();
      newPriv = new AttributeDefPrivilege(
          oldPriv.getAttributeDef(),
          subj,
          oldPriv.getOwner(),
          Privilege.getInstance(oldPriv.getName()),
          oldPriv.getImplementationName(),
          false, oldPriv.getContextId());
      fixed.add(newPriv);
    }
    return fixed;
  }

  /**
   * @see AttributeDefResolver#getAttributeDefsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    Set<AttributeDef> attributeDefs = super.getDecoratedResolver().getAttributeDefsWhereSubjectDoesntHavePrivilege(
        stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
    return attributeDefs;
  }

}
