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
import edu.internet2.middleware.grouper.AccessPrivilege;
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.grouper.cache.CacheStats;
import  edu.internet2.middleware.grouper.cache.EhcacheController;
import  edu.internet2.middleware.subject.Subject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import  java.util.Set;
import  net.sf.ehcache.Element;
import  org.apache.commons.collections.keyvalue.MultiKey;


/**
 * Decorator that provides caching for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingAccessResolver.java,v 1.5 2007-11-02 10:45:04 isgwb Exp $
 * @since   1.2.1
 */
public class CachingAccessResolver extends AccessResolverDecorator {
    // TODO 20070816 DRY caching w/ subject caching

 
  public  static final  String            CACHE_HASPRIV = CachingAccessResolver.class.getName() + ".HasPrivilege";
  private               EhcacheController cc;


 
  /**
   * @since   1.2.1
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
   * @since   1.2.1
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
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<Privilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    //2007-11-02 Gary Brown
    //https://bugs.internet2.edu/jira/browse/GRP-30
    //Needs to return actual privileges but also
    //cache true/false for each possible Privilege
	Set<Privilege> privs = super.getDecoratedResolver().getPrivileges(group, subject);
	Map<String, Object> privsMap = new HashMap<String, Object>();
	AccessPrivilege ap = null;
	Iterator it = privs.iterator();
	while(it.hasNext()) {
		ap = (AccessPrivilege) it.next();
		privsMap.put(ap.getName(), null);
	}
	Set<Privilege> accessPrivs = Privilege.getAccessPrivs();
	Iterator<Privilege> accessPrivsIterator = accessPrivs.iterator();
	Privilege p=null;
	while(accessPrivsIterator.hasNext()) {
		p=accessPrivsIterator.next();
		putInHasPrivilegeCache(group, subject, p, new Boolean(privsMap.containsKey(p.getName())));
	}
    return privs;
  }

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public CacheStats getStats(String cache) {
    return this.cc.getStats(cache);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    // TODO 20070816 add caching
    return super.getDecoratedResolver().getSubjectsWithPrivilege(group, privilege);
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
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
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    Boolean rv = this.getFromHasPrivilegeCache(group, subject, privilege);
    if (rv == null) {
      //2007-11-02 Gary Brown
      //https://bugs.internet2.edu/jira/browse/GRP-30
      //Get all the privileges - which will then be cached
      //on the assumption we will be checking other privileges
      getPrivileges(group, subject);
      //must be in the cache now
      rv = this.getFromHasPrivilegeCache(group, subject, privilege);
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
   * @since   1.2.1
   */
  private void putInHasPrivilegeCache(Group g, Subject subj, Privilege priv, Boolean rv) {
    this.cc.getCache(CACHE_HASPRIV).put( new Element( new MultiKey( g.getUuid(), subj, priv ), rv ) );
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   1.2.1
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
   * @since   1.2.1
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

