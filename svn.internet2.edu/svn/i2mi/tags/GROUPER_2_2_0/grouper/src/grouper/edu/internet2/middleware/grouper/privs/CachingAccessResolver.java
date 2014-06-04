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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cache.CacheStats;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides caching for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingAccessResolver.java,v 1.16 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class CachingAccessResolver extends AccessResolverDecorator {

  // TODO 20070816 DRY caching w/ subject caching

  /** */
  public static final String CACHE_HASPRIV = CachingAccessResolver.class.getName()
      + ".HasPrivilege";

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public CachingAccessResolver(AccessResolver resolver) {
    super(resolver);
  }

  /**
   * Retrieve boolean from cache for <code>hasPrivilege(...)</code>.
   * @param g 
   * @param subj 
   * @param priv 
   * @return  Cached return value or null.
   * @since   1.2.1
   */
  private Boolean getFromHasPrivilegeCache(Group g, Subject subj, Privilege priv) {

    Element el = EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).get(
        new MultiKey(g.getUuid(), subj.getSourceId(), subj.getId(), priv));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
      throws IllegalArgumentException {
    //2007-11-02 Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-30
    //Needs to return actual privileges but also
    //cache true/false for each possible Privilege
    Set<AccessPrivilege> privs = super.getDecoratedResolver().getPrivileges(group,
        subject);
    Set<String> privsSet = new HashSet<String>();
    AccessPrivilege ap = null;
    Iterator it = privs.iterator();
    while (it.hasNext()) {
      ap = (AccessPrivilege) it.next();
      privsSet.add(ap.getName());
    }
    Set<Privilege> accessPrivs = Privilege.getAccessPrivs();
    Iterator<Privilege> accessPrivsIterator = accessPrivs.iterator();
    Privilege p = null;
    while (accessPrivsIterator.hasNext()) {
      p = accessPrivsIterator.next();
      putInHasPrivilegeCache(group, subject, p, new Boolean(privsSet
          .contains(p.getName())));
    }
    return privs;
  }

  /**
   * @param cache 
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    return EhcacheController.ehcacheController().getStats(cache);
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege, String)
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege, uuid);
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
    //there is a problem where if this action happens in root session, the
    //normal session doesnt get flushed
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAccessResolver().flushCache();
    this.putInHasPrivilegeCache(group, subject, privilege, Boolean.TRUE);
    //System.out.println(this.toString() + ", Add to cache: " + true + ", " + group.getName() + ", " + subject.getId() + ", " + privilege.getName());
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    Boolean rv = this.getFromHasPrivilegeCache(group, subject, privilege);
    if (rv == null) {
      //2007-11-02 Gary Brown
      //https://bugs.internet2.edu/jira/browse/GRP-30
      //Get all the privileges - which will then be cached
      //on the assumption we will be checking other privileges
      getPrivileges(group, subject);
      //must be in the cache now
      rv = this.getFromHasPrivilegeCache(group, subject, privilege);
    } else {
      //System.out.println(this.toString() + ", From cache: " + rv + ", " + group.getName() + ", " + subject.getId() + ", " + privilege.getName());
    }
    //Hopefully redundant
    if (rv == null) {
      rv = super.getDecoratedResolver().hasPrivilege(group, subject, privilege);
      this.putInHasPrivilegeCache(group, subject, privilege, rv);
    }
    return rv;
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @param g 
   * @param subj 
   * @param priv 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Group g, Subject subj, Privilege priv, Boolean rv) {
    this.putInHasPrivilegeCache(g.getUuid(), subj, priv, rv);
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @param groupUuid 
   * @param subj 
   * @param priv 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(String groupUuid, Subject subj, Privilege priv,
      Boolean rv) {
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).put(
        new Element(new MultiKey(groupUuid, subj.getSourceId(), subj.getId(), priv), rv));
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(group, privilege);
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
    //there is a problem where if this action happens in root session, the
    //normal session doesnt get flushed
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAccessResolver().flushCache();
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
    //there is a problem where if this action happens in root session, the
    //normal session doesnt get flushed
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAccessResolver().flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(g1, g2, priv);
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAccessResolver().flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAccessResolver().flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject,
      Set<Privilege> privInSet) {

    Set<Group> filteredGroups = super.getDecoratedResolver().postHqlFilterGroups(groups,
        subject, privInSet);

    //add to cache
    for (Group group : groups) {
      putInHasPrivilegeCache(group, subject, AccessPrivilege.VIEW, filteredGroups
          .contains(group));
    }

    //return filtered groups
    return filteredGroups;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause(
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn,
      Set<Privilege> privInSet) {

    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsWhereClause(subject, hqlQuery, hql,
        groupColumn, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.getGrouperSession();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterMemberships(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(Subject subject,
      Set<Membership> memberships) {

    AccessResolver decoratedResolver = super.getDecoratedResolver();

    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    Set<Membership> filteredMemberships = decoratedResolver.postHqlFilterMemberships(
        subject, memberships);

    for (Membership membership : memberships) {
      //TODO change this for 1.5.  Note: this could be a stem, but thats ok
      putInHasPrivilegeCache(membership.getOwnerGroupId(), subject, AccessPrivilege.VIEW,
          filteredMemberships.contains(membership));
    }

    return filteredMemberships;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    grouperSession.getAccessResolver().flushCache();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege)
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn,
      Privilege privilege, boolean considerAllSubject) {
  
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsNotWithPrivWhereClause(subject, hqlQuery, hql,
        groupColumn, privilege, considerAllSubject);
  }

}
