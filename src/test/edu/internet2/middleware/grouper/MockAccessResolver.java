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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.grouper.privs.AccessResolver;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/**
 * Mock {@link AccessResolver}.
 * @author  blair christensen.
 * @version $Id: MockAccessResolver.java,v 1.2 2007-08-24 19:42:50 blair Exp $
 * @since   @HEAD@
 */
public class MockAccessResolver implements AccessResolver {


  private static final GrouperRuntimeException E = new GrouperRuntimeException("not implemented");



  /**
   * @return  New <code>MockAccessResolver</code>.
   * @since   @HEAD@
   */
  public MockAccessResolver() {
    super();
  }



  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public String getConfig(String property) 
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public Set<Privilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }
            

  /**
   * Not implemented.
   * @throws  GrouperRuntimeException
   * @since   @HEAD@
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }            

}

