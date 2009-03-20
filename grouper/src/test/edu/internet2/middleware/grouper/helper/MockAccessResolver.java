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

package edu.internet2.middleware.grouper.helper;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;


/**
 * Mock {@link AccessResolver}.
 * @author  blair christensen.
 * @version $Id: MockAccessResolver.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class MockAccessResolver implements AccessResolver {


  private static final GrouperException E = new GrouperException("not implemented");



  /**
   * @return  New <code>MockAccessResolver</code>.
   * @since   1.2.1
   */
  public MockAccessResolver() {
    super();
  }



  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public String getConfig(String property) 
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }
            

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    throw E;
  }


  /**
   * Not implemented.
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    throw E;
  }


  /**
   * Not implemented.
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    throw E;
  }            

}

