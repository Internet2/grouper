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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides <i>GrouperAll</i> privilege resolution for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperAllAccessResolver.java,v 1.15 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class GrouperAllAccessResolver extends AccessResolverDecorator {


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }

  /** */
  private Subject all;

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    super.getDecoratedResolver().stop();
  }

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public GrouperAllAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.all = SubjectFinder.findAllSubject();
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    Set<Group> groups = super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(
        subject, privilege);
//   this is done further down in dao
//    groups.addAll(super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(
//        this.all, privilege));
    return groups;
  }

  /**
   * @see AccessResolver#getGroupsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    Set<Group> groups = super.getDecoratedResolver().getGroupsWhereSubjectDoesntHavePrivilege(
        stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
    return groups;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolverDecorator#getStemsWhereGroupThatSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  @Override
  public Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(Subject subject,
      Privilege privilege) throws IllegalArgumentException {
    Set<Stem> stems = super.getDecoratedResolver().getStemsWhereGroupThatSubjectHasPrivilege(
        subject, privilege);
    return stems;
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
      throws IllegalArgumentException {
    // TODO 20070820 include GrouperAll privs?
    //2007-11-02 Gary Brown
    //I assume this is what blair intended - have removed
    //the All privileges from the GrouperAccessAdapter

    Set<AccessPrivilege> allPrivs = fixPrivs(super.getDecoratedResolver().getPrivileges(
        group, this.all), subject);
    allPrivs.addAll(super.getDecoratedResolver().getPrivileges(group, subject));
    return allPrivs;
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    if (super.getDecoratedResolver().hasPrivilege(group, this.all, privilege)) {
      return true;
    }
    return super.getDecoratedResolver().hasPrivilege(group, subject, privilege);
  }

  /**
   * 
   * @param privs
   * @param subj
   * @return the set, never null
   */
  private Set<AccessPrivilege> fixPrivs(Set<AccessPrivilege> privs, Subject subj) {
    Set<AccessPrivilege> fixed = new HashSet<AccessPrivilege>();
    Iterator<AccessPrivilege> it = privs.iterator();
    AccessPrivilege oldPriv;
    AccessPrivilege newPriv;
    while (it.hasNext()) {
      oldPriv = it.next();
      newPriv = new AccessPrivilege(
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

}
