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
import java.util.Set;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides <i>GrouperSystem</i> privilege resolution for {@link AttributeDefResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSystemAttrDefResolver.java,v 1.2 2009-09-28 05:06:46 mchyzer Exp $
 * @since   1.2.1
 */
public class GrouperSystemAttrDefResolver extends AttributeDefResolverDecorator {

  /**
   * 
   */
  private Subject root;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperSystemAttrDefResolver(AttributeDefResolver resolver) {
    super(resolver);
    this.root = SubjectFinder.findRootSubject();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef, Subject subject)
      throws IllegalArgumentException {
    //2007-11-02 Gary Brown
    //If you are the root user you automatically have
    //all Access privileges exception OPTIN / OPTOUT
    if (SubjectHelper.eq(this.root, subject)) {
      Set<Privilege> privs = Privilege.getAttributeDefPrivs();
      Set<AttributeDefPrivilege> attributeDefPrivs = new HashSet<AttributeDefPrivilege>();
      AttributeDefPrivilege ap = null;
      for (Privilege p : privs) {
        //Not happy about the klass but will do for now in the absence of a GrouperSession
        if (!p.equals(AttributeDefPrivilege.ATTR_OPTIN) && !p.equals(AttributeDefPrivilege.ATTR_OPTOUT)) {
          ap = new AttributeDefPrivilege(attributeDef, subject, subject, p, GrouperConfig
              .getProperty("privileges.attributeDef.interface"), false, null);
          attributeDefPrivs.add(ap);
        }
      }

      return attributeDefPrivs;
    }
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.getPrivileges(attributeDef, subject);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    if (SubjectHelper.eq(this.root, subject)) {
      if (!privilege.equals(AttributeDefPrivilege.ATTR_OPTIN)
          && !privilege.equals(AttributeDefPrivilege.ATTR_OPTOUT)) {
        return true;
      }
      return false;
    }
    return super.getDecoratedResolver().hasPrivilege(attributeDef, subject, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttrDefs(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs, Subject subject,
      Set<Privilege> privInSet) {
    if (SubjectHelper.eq(this.root, subject)) {
      return attributeDefs;
    }
    return super.getDecoratedResolver().postHqlFilterAttrDefs(attributeDefs, subject, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {

    if (SubjectHelper.eq(this.root, subject)) {
      return false;
    }

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.hqlFilterAttrDefsWhereClause(subject, hqlQuery, hql,
        groupColumn, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttrDefs(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Subject subject,
      Set<AttributeDef> attributeDefs) {

    if (SubjectHelper.eq(this.root, subject)) {
      return attributeDefs;
    }

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.postHqlFilterAttrDefs(subject, attributeDefs);
  }

}
