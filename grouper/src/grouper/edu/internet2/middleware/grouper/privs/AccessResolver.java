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
import  edu.internet2.middleware.grouper.AccessAdapter;
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/** 
 * Facade for the {@link AccessAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessResolver.java,v 1.2 2007-08-27 15:53:53 blair Exp $
 * @since   1.2.1
 */
public interface AccessResolver {

  /**
   * @return  Configuration value.
   * @throws  IllegalStateException if any parameter is null.
   */
  String getConfig(String key) throws IllegalStateException;

  /**
   * Get all groups where <i>subject</i> has <i>privilege</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getGroupsWhereSubjectHasPriv(GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Get all privileges <i>subject</i> has on <i>group</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getPrivs(GrouperSession, Group, Subject)
   * @since   1.2.1
   */
  Set<Privilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException;

  /**
   * Get all subjects with <i>privilege</i> on <i>group</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#getSubjectsWithPriv(GrouperSession, Group, Privilege)
   * @since   1.2.1
   */
  Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Grant <i>privilege</i> to <i>subject</i> on <i>group</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be granted.
   * @see     AccessAdapter#grantPriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Check whether <i>subject</i> has <i>privilege</i> on <i>group</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @see     AccessAdapter#hasPriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException;

  /**
   * Revoke <i>privilege</i> from all subjects on <i>group</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     AccessAdapter#revokePriv(GrouperSession, Group, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

  /**
   * Revoke <i>privilege</i> from <i>subject</i> on <i>group</i>.
   * <p/>
   * @throws  IllegalArgumentException if any parameter is null.
   * @throws  UnableToPerformException if the privilege could not be revoked.
   * @see     AccessAdapter#revokePriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
            ;

}

