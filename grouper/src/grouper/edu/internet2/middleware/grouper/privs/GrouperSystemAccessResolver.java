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
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.subject.Subject;


/**
 * Decorator that provides <i>GrouperSystem</i> privilege resolution for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSystemAccessResolver.java,v 1.8.2.2 2009-08-11 20:16:27 mchyzer Exp $
 * @since   1.2.1
 */
public class GrouperSystemAccessResolver extends AccessResolverDecorator {

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    super.getDecoratedResolver().flushCache();
  }

  /**
   * 
   */
  private Subject root;



  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperSystemAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.root = SubjectFinder.findRootSubject();
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
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  { 
    //2007-11-02 Gary Brown
    //If you are the root user you automatically have
    //all Access privileges exception OPTIN / OPTOUT
	  if ( SubjectHelper.eq( this.root, subject ) ) {
	      Set<Privilege> privs = Privilege.getAccessPrivs();
	      Set<AccessPrivilege> accessPrivs = new HashSet<AccessPrivilege>();
	      AccessPrivilege ap = null;
	      for(Privilege p : privs) {
	    	//Not happy about the klass but will do for now in the absence of a GrouperSession
	    	if(!p.equals(AccessPrivilege.OPTIN) && !p.equals(AccessPrivilege.OPTOUT)) {
	    		ap = new AccessPrivilege(group,subject,subject,p,GrouperConfig.getProperty("privileges.access.interface"),false);
	    		accessPrivs.add(ap);
	    	}
	      }
	      
	      return accessPrivs;
	    }
    return super.getDecoratedResolver().getPrivileges(group, subject);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
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
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    if ( SubjectHelper.eq( this.root, subject )) {
    	if(!privilege.equals(AccessPrivilege.OPTIN) 
    		&& !privilege.equals(AccessPrivilege.OPTOUT)) {
    	  return true;
    	}
  		return false;
    }
    return super.getDecoratedResolver().hasPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(group, privilege);
  }
            

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject, Set<Privilege> privInSet) {
    if ( SubjectHelper.eq( this.root, subject ) ) {
      return groups;
    }
    return super.getDecoratedResolver().postHqlFilterGroups(groups, subject, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, String, Set)
   */
  public boolean hqlFilterGroupsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {

    if ( SubjectHelper.eq( this.root, subject ) ) {
      return false;
    }

    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsWhereClause(subject, hqlQuery, hql, groupColumn, privInSet);
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
    
    if ( SubjectHelper.eq( this.root, subject ) ) {
      return memberships;
    }
    
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.postHqlFilterMemberships(subject, memberships);
  }

}

