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

import net.sf.ehcache.Element;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cache.CacheStats;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITPermissionAllView;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides caching for {@link AttributeDefResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingAttrDefResolver.java,v 1.2 2009-09-28 05:06:46 mchyzer Exp $
 * @since   1.2.1
 */
public class CachingAttrDefResolver extends AttributeDefResolverDecorator {

  /** */
  public static final String CACHE_HASPRIV = CachingAttrDefResolver.class.getName()
      + ".HasPrivilege";

  /** */
  private EhcacheController cc;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public CachingAttrDefResolver(AttributeDefResolver resolver) {
    super(resolver);
    this.cc = new EhcacheController();
  }

  /**
   * 
   * @param attributeDef
   * @param subj
   * @param priv
   * @return if has priv, or null if not known
   */
  private Boolean getFromHasPrivilegeCache(AttributeDef attributeDef, Subject subj,
      Privilege priv) {
    // TODO 20070823 are these the right element keys to use?
    Element el = this.cc.getCache(CACHE_HASPRIV).get(
        new MultiKey(attributeDef.getId(), subj, priv));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * 
   * @param permissionEntry
   * @param subj
   * @param priv
   * @return if has priv, or null if not known
   */
  @SuppressWarnings("unused")
  private Boolean getFromHasPrivilegeCache(PermissionEntry permissionEntry, Subject subj,
      Privilege priv) {
    // TODO 20070823 are these the right element keys to use?
    Element el = this.cc.getCache(CACHE_HASPRIV).get(
        new MultiKey(permissionEntry.getAttributeDefId(), permissionEntry.getRoleId(), subj, priv));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * @see     AttributeDefResolver#getAttributeDefsWhereSubjectHasPrivilege(Subject, Privilege)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectHasPrivilege(Subject subject,
      Privilege privilege)
      throws IllegalArgumentException {

    return super.getDecoratedResolver().getAttributeDefsWhereSubjectHasPrivilege(subject,
        privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getPrivileges(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivileges(AttributeDef attributeDef,
      Subject subject)
      throws IllegalArgumentException {
    //2007-11-02 Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-30
    //Needs to return actual privileges but also
    //cache true/false for each possible Privilege
    Set<AttributeDefPrivilege> privs = super.getDecoratedResolver().getPrivileges(
        attributeDef, subject);
    Set<String> privsSet = new HashSet<String>();
    AttributeDefPrivilege ap = null;
    Iterator it = privs.iterator();
    while (it.hasNext()) {
      ap = (AttributeDefPrivilege) it.next();
      privsSet.add(ap.getName());
    }
    Set<Privilege> attrDefPrivs = Privilege.getAttributeDefPrivs();
    Iterator<Privilege> attributeDefPrivsIterator = attrDefPrivs.iterator();
    Privilege p = null;
    while (attributeDefPrivsIterator.hasNext()) {
      p = attributeDefPrivsIterator.next();
      putInHasPrivilegeCache(attributeDef, subject, p, 
          new Boolean(privsSet.contains(p.getName())));
    }
    return privs;
  }

  /**
   * @param cache 
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    return this.cc.getStats(cache);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getSubjectsWithPrivilege(attributeDef, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#grantPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().grantPrivilege(attributeDef, subject, privilege, uuid);
    this.cc.flushCache();
    //there is a problem where if this action happens in root session, the
    //normal session doesnt get flushed
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAttributeDefResolver().flushCache();
    this.putInHasPrivilegeCache(attributeDef, subject, privilege, Boolean.TRUE);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hasPrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    Boolean rv = this.getFromHasPrivilegeCache(attributeDef, subject, privilege);
    if (rv == null) {
      //2007-11-02 Gary Brown
      //https://bugs.internet2.edu/jira/browse/GRP-30
      //Get all the privileges - which will then be cached
      //on the assumption we will be checking other privileges
      getPrivileges(attributeDef, subject);
      //must be in the cache now
      rv = this.getFromHasPrivilegeCache(attributeDef, subject, privilege);
    }
    //Hopefully redundant
    if (rv == null) {
      rv = super.getDecoratedResolver().hasPrivilege(attributeDef, subject, privilege);
      this.putInHasPrivilegeCache(attributeDef, subject, privilege, rv);
    }
    return rv;
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @param attributeDef 
   * @param subj 
   * @param priv 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(AttributeDef attributeDef, Subject subj, Privilege priv, Boolean rv) {
    this.putInHasPrivilegeCache(attributeDef.getId(), subj, priv, rv);
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @param attributeDefId 
   * @param subj 
   * @param priv 
   * @param rv 
   */
  private void putInHasPrivilegeCache(String attributeDefId, Subject subj, Privilege priv,
      Boolean rv) {
    this.cc.getCache(CACHE_HASPRIV).put(
        new Element(new MultiKey(attributeDefId, subj, priv), rv));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    super.getDecoratedResolver().revokePrivilege(attributeDef, privilege);
    this.cc.flushCache();
    //there is a problem where if this action happens in root session, the
    //normal session doesnt get flushed
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAttributeDefResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokePrivilege(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(AttributeDef attributeDef, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(attributeDef, subject, privilege);
    this.cc.flushCache();
    //there is a problem where if this action happens in root session, the
    //normal session doesnt get flushed
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAttributeDefResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#privilegeCopy(edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(AttributeDef attributeDef1, AttributeDef attributeDef2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(attributeDef1, attributeDef2, priv);
    this.cc.flushCache();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAttributeDefResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
    this.cc.flushCache();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAttributeDefResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#flushCache()
   */
  public void flushCache() {
    this.cc.flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterAttrDefs(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeDef> postHqlFilterAttrDefs(Set<AttributeDef> attributeDefs, Subject subject,
      Set<Privilege> privInSet) {

    Set<AttributeDef> filteredAttrDefs = super.getDecoratedResolver().postHqlFilterAttrDefs(attributeDefs,
        subject, privInSet);

    //add to cache
    for (AttributeDef attributeDef : attributeDefs) {
      putInHasPrivilegeCache(attributeDef, subject, AttributeDefPrivilege.ATTR_VIEW, filteredAttrDefs
          .contains(attributeDef));
    }

    //return filtered groups
    return filteredAttrDefs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterAttrDefsWhereClause(
      Subject subject, HqlQuery hqlQuery,     StringBuilder hqlTables, StringBuilder hqlWhereClause, String attrDefColumn,
      Set<Privilege> privInSet) {

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAttributeDefResolver
    return decoratedResolver.hqlFilterAttrDefsWhereClause(subject, hqlQuery, hqlTables, hqlWhereClause,
        attrDefColumn, privInSet);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.getGrouperSession();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterAttributeAssigns(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(Subject subject,
      Set<AttributeAssign> attributeAssigns) {

    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();

    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAttributeDefResolver
    Set<AttributeAssign> filteredAttributeAssigns = decoratedResolver.postHqlFilterAttributeAssigns(
        subject, attributeAssigns);

    for (AttributeAssign attributeAssign : attributeAssigns) {
      putInHasPrivilegeCache(attributeAssign.getId(), subject, AttributeDefPrivilege.ATTR_VIEW,
          filteredAttributeAssigns.contains(attributeAssign));
    }

    return filteredAttributeAssigns;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterPITAttributeAssigns(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PITAttributeAssign> postHqlFilterPITAttributeAssigns(Subject subject,
      Set<PITAttributeAssign> pitAttributeAssigns) {

    return super.getDecoratedResolver().postHqlFilterPITAttributeAssigns(
        subject, pitAttributeAssigns);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#stop()
   */
  public void stop() {
    if (this.cc != null) {
      this.cc.stop();
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
    this.cc.flushCache();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAttributeDefResolver().flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#postHqlFilterPermissions(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PermissionEntry> postHqlFilterPermissions(Subject subject,
      Set<PermissionEntry> permissionsEntries) {
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();

    Set<PermissionEntry> filteredPermissions = decoratedResolver.postHqlFilterPermissions(
        subject, permissionsEntries);

    for (PermissionEntry permissionEntry : permissionsEntries) {
      putInHasPrivilegeCache(permissionEntry, subject, AttributeDefPrivilege.ATTR_VIEW,
          filteredPermissions.contains(permissionEntry));
    }

    return filteredPermissions;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolverDecorator#postHqlFilterPITPermissions(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<PITPermissionAllView> postHqlFilterPITPermissions(Subject subject,
      Set<PITPermissionAllView> pitPermissionsEntries) {

    return super.getDecoratedResolver().postHqlFilterPITPermissions(subject, pitPermissionsEntries);
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @param permissionEntry 
   * @param subj 
   * @param priv 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(PermissionEntry permissionEntry, Subject subj, Privilege priv, Boolean rv) {

    //we care about the def id, and the roleId
    this.cc.getCache(CACHE_HASPRIV).put(
        new Element(new MultiKey(
            permissionEntry.getAttributeDefId(), permissionEntry.getRoleId(), subj, priv), rv));
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefResolver#hqlFilterAttributeDefsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege)
   */
  public boolean hqlFilterAttributeDefsNotWithPrivWhereClause(
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String attributeDefColumn,
      Privilege privilege, boolean considerAllSubject) {
  
    AttributeDefResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterAttributeDefsNotWithPrivWhereClause(subject, hqlQuery, hql,
        attributeDefColumn, privilege, considerAllSubject);
  }

}
