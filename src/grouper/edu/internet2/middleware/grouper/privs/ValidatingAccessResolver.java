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
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/**
 * Decorator that provides parameter validation for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingAccessResolver.java,v 1.1 2007-08-24 14:18:16 blair Exp $
 * @since   @HEAD@
 */
public class ValidatingAccessResolver extends AccessResolverDecorator {

  private ParameterHelper param;



  /**
   * @see     AccessResolverDecorator(AccessResolver)
   * @since   @HEAD@
   */
  public ValidatingAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.param = new ParameterHelper();
  }




  /**
   * @see     AccessResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    this.param.notNullString(key);
    return super.getDecoratedResolver().getConfig(key);
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   @HEAD@
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   @HEAD@
   */
  public Set<Privilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    this.param.notNullGroup(group).notNullSubject(subject);
    return super.getDecoratedResolver().getPrivileges(group, subject);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   @HEAD@
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullGroup(group).notNullPrivilege(privilege);
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
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   @HEAD@
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().hasPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    this.param.notNullGroup(group).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(group, privilege);
  }
            

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
  }            

}

