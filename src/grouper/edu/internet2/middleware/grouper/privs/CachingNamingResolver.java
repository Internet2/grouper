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
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.cache.CacheStats;
import  edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;
import  net.sf.ehcache.Element;
import  org.apache.commons.collections.keyvalue.MultiKey;


/**
 * Decorator that provides caching for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingNamingResolver.java,v 1.5 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.1
 */
public class CachingNamingResolver extends NamingResolverDecorator {
  // TODO 20070816 DRY caching w/ subject caching
  // TODO 20070820 DRY w/ access resolution

 
  public  static final  String            CACHE_HASPRIV = CachingNamingResolver.class.getName() + ".HasPrivilege";
  private               EhcacheController cc;


 
  /**
   * @since   1.2.1
   */
  public CachingNamingResolver(NamingResolver resolver) {
    super(resolver);
    this.cc = new EhcacheController();
  }



  /**
   * @see     NamingResolver#getConfig(String)
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
   * @since   1.2.1
   */
  private Boolean getFromHasPrivilegeCache(Stem ns, Subject subj, Privilege priv) {
    // TODO 20070823 are these the right element keys to use?
    Element el = this.cc.getCache(CACHE_HASPRIV).get( new MultiKey( ns.getUuid(), subj, priv ) );
    if (el != null) {
      return (Boolean) el.getObjectValue();
    }
    return null;
  }

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    return this.cc.getStats(cache);
  }

  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @since   1.2.1
   */
  public Set<Privilege> getPrivileges(Stem stem, Subject subject)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getPrivileges(stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getSubjectsWithPrivilege(stem, privilege);
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    // TODO 20070816 add caching
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege);
    this.cc.flushCache();
    this.putInHasPrivilegeCache(stem, subject, privilege, Boolean.TRUE);
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    Boolean rv = this.getFromHasPrivilegeCache(stem, subject, privilege);
    if (rv == null) {
      rv = super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
      this.putInHasPrivilegeCache(stem, subject, privilege, rv);
    }
    return rv;
  }

  /**
   * Put boolean into cache for <code>hasPrivilege(...)</code>.
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Stem ns, Subject subj, Privilege priv, Boolean rv) {
    this.cc.getCache(CACHE_HASPRIV).put( new Element( new MultiKey( ns.getUuid(), subj, priv ), rv ) );
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(stem, privilege);
    this.cc.flushCache();
  }
            

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    // TODO 20070816 add caching
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
    this.cc.flushCache();
  }            

}

