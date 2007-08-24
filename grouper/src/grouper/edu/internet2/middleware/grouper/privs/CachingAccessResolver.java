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
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.SubjectFinder;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.grouper.cache.CacheStats;
import  edu.internet2.middleware.grouper.cache.EhcacheController;
import  edu.internet2.middleware.grouper.cache.EhcacheStats;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;
import  net.sf.ehcache.Cache;
import  net.sf.ehcache.CacheManager;
import  net.sf.ehcache.Element;
import  net.sf.ehcache.Statistics;
import  org.apache.commons.collections.keyvalue.MultiKey;


/**
 * Decorator that provides caching for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingAccessResolver.java,v 1.1 2007-08-24 14:18:16 blair Exp $
 * @since   @HEAD@
 */
public class CachingAccessResolver extends AccessResolverDecorator {
    // TODO 20070816 DRY caching w/ subject caching

 
  public  static final  String            CACHE_HASPRIV = CachingAccessResolver.class.getName() + ".HasPrivilege";
  private               EhcacheController cc;


 
  /**
   * @see     AccessResolverDecorator(AccessResolver)
   * @since   @HEAD@
   */
  public CachingAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.cc = new EhcacheController();
  }



  /**
   * @see     AccessResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    return super.getDecoratedResolver().getConfig(key);
  }

  /**
   * Retrieve boolean from cache for <code>hasPrivilege(...)</code>.
   * @return  Cached return value or null.
   * @since   @HEAD@
   */
  private Boolean getFromHasPrivilegeCache(Group g, Subject subj, Privilege priv) {
    // TODO 20070823 are these the right element keys to use?
    Element el = this.cc.getCache(CACHE_HASPRIV).get( new MultiKey( g.getUuid(), subj, priv ) );
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   @HEAD@
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   @HEAD@
   */
  public Set<Privilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getPrivileges(group, subject);
  }

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   @HEAD@
   */
  public CacheStats getStats(String cache) {
    return this.cc.getStats(cache);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   @HEAD@
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getSubjectsWithPrivilege(group, privilege);
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege)
   * @since   @HEAD@
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    // TODO 20070816 add caching
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege);
    this.cc.flushCache();
    this.putInHasPrivilegeCache(group, subject, privilege, Boolean.TRUE);
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   @HEAD@
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    Boolean rv = this.getFromHasPrivilegeCache(group, subject, privilege);
    if (rv == null) {
      rv = super.getDecoratedResolver().hasPrivilege(group, subject, privilege);
      this.putInHasPrivilegeCache(group, subject, privilege, rv);
    }
    return rv;
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @since   @HEAD@
   */
  private void putInHasPrivilegeCache(Group g, Subject subj, Privilege priv, Boolean rv) {
    this.cc.getCache(CACHE_HASPRIV).put( new Element( new MultiKey( g.getUuid(), subj, priv ), rv ) );
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(group, privilege);
    this.cc.flushCache();
  }
            

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
    this.cc.flushCache();
  }            

}

