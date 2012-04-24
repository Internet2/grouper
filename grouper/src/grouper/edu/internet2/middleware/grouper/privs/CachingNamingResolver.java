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

import java.util.Set;

import net.sf.ehcache.Element;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cache.CacheStats;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides caching for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingNamingResolver.java,v 1.12 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class CachingNamingResolver extends NamingResolverDecorator {

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#flushCache()
   */
  public void flushCache() {
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).flush();
  }

  // TODO 20070816 DRY caching w/ subject caching
  // TODO 20070820 DRY w/ access resolution

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    NamingResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.getGrouperSession();
  }

  /** */
  public static final String CACHE_HASPRIV = CachingNamingResolver.class.getName()
      + ".HasPrivilege";

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public CachingNamingResolver(NamingResolver resolver) {
    super(resolver);
  }

  /**
   * Retrieve boolean from cache for <code>hasPrivilege(...)</code>.
   * @param ns 
   * @param subj 
   * @param priv 
   * @return  Cached return value or null.
   * @since   1.2.1
   */
  private Boolean getFromHasPrivilegeCache(Stem ns, Subject subj, Privilege priv) {
    // TODO 20070823 are these the right element keys to use?
    Element el = EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).get(
        new MultiKey(ns.getUuid(), subj.getSourceId(), subj.getId(), priv));
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * @param cache 
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    return EhcacheController.ehcacheController().getStats(CACHE_HASPRIV);
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege, String)
   * @since   1.2.1
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege, uuid);
    this.flushCache();
    this.putInHasPrivilegeCache(stem, subject, privilege, Boolean.TRUE);
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    Boolean rv = this.getFromHasPrivilegeCache(stem, subject, privilege);
    if (rv == null) {
      rv = super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
      this.putInHasPrivilegeCache(stem, subject, privilege, rv);
    }
    return rv;
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @param ns 
   * @param subj 
   * @param priv 
   * @param rv 
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Stem ns, Subject subj, Privilege priv, Boolean rv) {
    EhcacheController.ehcacheController().getCache(CACHE_HASPRIV).put(
        new Element(new MultiKey(ns.getUuid(), subj.getSourceId(), subj.getId(), priv), rv));
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(stem, privilege);
    this.flushCache();
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
    this.flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Stem stem1, Stem stem2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(stem1, stem2, priv);
    this.flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
    this.flushCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#postHqlFilterStems(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(Set<Stem> stems, Subject subject,
      Set<Privilege> privInSet) {
    Set<Stem> filteredStems = super.getDecoratedResolver().postHqlFilterStems(stems,
        subject, privInSet);

    //add to cache
    for (Stem stem : stems) {
      putInHasPrivilegeCache(stem, subject, AccessPrivilege.VIEW, filteredStems
          .contains(stem));
    }

    //return filtered groups
    return filteredStems;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#stop()
   */
  public void stop() {
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
    this.flushCache();
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause(
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn,
      Privilege privilege, boolean considerAllSubject) {
  
    NamingResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterStemsNotWithPrivWhereClause(subject, hqlQuery, hql,
        stemColumn, privilege, considerAllSubject);
  }

}
