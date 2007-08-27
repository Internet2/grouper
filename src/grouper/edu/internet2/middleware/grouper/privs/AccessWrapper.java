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
import  edu.internet2.middleware.grouper.GrantPrivilegeException;
import  edu.internet2.middleware.grouper.Group;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  edu.internet2.middleware.grouper.GrouperSession;
import  edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.RevokePrivilegeException;
import  edu.internet2.middleware.grouper.SchemaException;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/** 
 * Class implementing wrapper around {@link AccessAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessWrapper.java,v 1.4 2007-08-27 15:53:53 blair Exp $
 * @since   1.2.1
 */
public class AccessWrapper implements AccessResolver {


  private AccessAdapter   access;
  private GrouperSession  s;
  private ParameterHelper param;



  /**
   * Facade around {@link AccessAdapter} that implements {@link AccessResolver}.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public AccessWrapper(GrouperSession session, AccessAdapter access) 
    throws  IllegalArgumentException
  {
    this.param  = new ParameterHelper();
    this.param.notNullGrouperSession(session).notNullAccessAdapter(access);
    this.s      = session;
    this.access = access;
  }



  /**
   * @see     AccessResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    return this.s.getConfig(key);
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @see     AccessAdapter#getGroupsWhereSubjectHasPriv(GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    try {
      return this.access.getGroupsWhereSubjectHasPriv(this.s, subject, privilege);
    }
    catch (SchemaException eSchema) {
      throw new GrouperRuntimeException("unexpected condition"); // TODO 20070726 log?  throw IllegalStateException?
    }
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @see     AccessAdapter#getPrivs(GrouperSession, Group, Subject)
   * @since   1.2.1
   */
  public Set<Privilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    return this.access.getPrivs(this.s, group, subject);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @see     AccessAdapter#getSubjectsWithPriv(GrouperSession, Group, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    try {
      return this.access.getSubjectsWithPriv(this.s, group, privilege);
    }
    catch (SchemaException eSchema) {
      throw new GrouperRuntimeException("unexpected condition"); // TODO 20070726 log?  throw IllegalStateException?
    }
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege)
   * @see     AccessAdapter#grantPriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    try {
      this.access.grantPriv(this.s, group, subject, privilege);
    }
    catch (GrantPrivilegeException eGrant) {
      throw new UnableToPerformException( eGrant.getMessage(), eGrant );
    }
    catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    }
    catch (SchemaException eSchema) {
      throw new GrouperRuntimeException("unexpected condition"); // TODO 20070726 log?  throw IllegalStateException?
    }
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @see     AccessAdapter#hasPriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    try {
      return this.access.hasPriv(this.s, group, subject, privilege);
    }
    catch (SchemaException eSchema) {
      throw new GrouperRuntimeException("unexpected condition"); // TODO 20070727 log?  throw IllegalStateException?
    }
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @see     AccessAdapter#revokePriv(GrouperSession, Group, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    try {
      this.access.revokePriv(this.s, group, privilege);
    }
    catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    }
    catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException( eRevoke.getMessage(), eRevoke );
    }
    catch (SchemaException eSchema) {
      throw new GrouperRuntimeException("unexpected condition"); // TODO 20070727 log?  throw IllegalStateException?
    }
  }
            

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @see     AccessAdapter#revokePriv(GrouperSession, Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    try {
      this.access.revokePriv(this.s, group, subject, privilege);
    }
    catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    }
    catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException( eRevoke.getMessage(), eRevoke );
    }
    catch (SchemaException eSchema) {
      throw new GrouperRuntimeException("unexpected condition"); // TODO 20070727 log?  throw IllegalStateException?
    }
  }            

}

