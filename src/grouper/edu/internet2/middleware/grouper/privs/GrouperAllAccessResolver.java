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
import  edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import  edu.internet2.middleware.subject.Subject;

import java.util.HashSet;
import java.util.Iterator;
import  java.util.Set;


/**
 * Decorator that provides <i>GrouperAll</i> privilege resolution for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperAllAccessResolver.java,v 1.10 2009-03-24 17:12:07 mchyzer Exp $
 * @since   1.2.1
 */
public class GrouperAllAccessResolver extends AccessResolverDecorator {

  /** */
  private Subject all;



  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperAllAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.all = SubjectFinder.findAllSubject();
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
    Set<Group> groups = super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
    groups.addAll( super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(this.all, privilege) );
    return groups;
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    // TODO 20070820 include GrouperAll privs?
    //2007-11-02 Gary Brown
    //I assume this is what blair intended - have removed
    //the All privileges from the GrouperAccessAdapter
    
	  Set<AccessPrivilege> allPrivs = fixPrivs(super.getDecoratedResolver().getPrivileges(group, this.all),subject);
	  allPrivs.addAll(super.getDecoratedResolver().getPrivileges(group, subject));
    return allPrivs;
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
    if ( super.getDecoratedResolver().hasPrivilege(group, this.all, privilege) ) {
      return true;
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
   * 
   * @param privs
   * @param subj
   * @return the set, never null
   */
  private Set<AccessPrivilege> fixPrivs(Set<AccessPrivilege> privs,Subject subj) {
	Set<AccessPrivilege> fixed = new HashSet<AccessPrivilege>();
	Iterator<AccessPrivilege> it = privs.iterator();
	AccessPrivilege oldPriv;
	AccessPrivilege newPriv;
	while(it.hasNext()) {
		oldPriv=it.next();
		newPriv= new AccessPrivilege(
					oldPriv.getGroup(),
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
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }


  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(g1, g2, priv);
  }


  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }

}

