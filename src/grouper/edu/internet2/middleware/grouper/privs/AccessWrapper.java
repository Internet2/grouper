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
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.subject.Subject;


/** 
 * Class implementing wrapper around {@link AccessAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessWrapper.java,v 1.16 2009-08-11 20:34:18 mchyzer Exp $
 * @since   1.2.1
 */
public class AccessWrapper implements AccessResolver {

  /** */
  private AccessAdapter   access;

  /** */
  private GrouperSession  s;

  /** */
  private ParameterHelper param;



  /**
   * Facade around {@link AccessAdapter} that implements {@link AccessResolver}.
   * @param session 
   * @param access 
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
      throw new GrouperException("unexpected condition"); // TODO 20070726 log?  throw IllegalStateException?
    }
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @see     AccessAdapter#getPrivs(GrouperSession, Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
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
      throw new GrouperException("unexpected condition"); // TODO 20070726 log?  throw IllegalStateException?
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
      if (eGrant instanceof GrantPrivilegeAlreadyExistsException) {
        throw new UnableToPerformAlreadyExistsException( eGrant.getMessage(), eGrant);
      }
      throw new UnableToPerformException( eGrant.getMessage(), eGrant );
    }
    catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); // TODO 20070726 log?  throw IllegalStateException?
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
      throw new GrouperException("unexpected condition"); // TODO 20070727 log?  throw IllegalStateException?
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
      throw new GrouperException("unexpected condition"); // TODO 20070727 log?  throw IllegalStateException?
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
    } catch (RevokePrivilegeAlreadyRevokedException eRevoke) {
      throw new UnableToPerformAlreadyExistsException( eRevoke.getMessage(), eRevoke );
    } catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException( eRevoke.getMessage(), eRevoke );
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition"); // TODO 20070727 log?  throw IllegalStateException?
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject, Set<Privilege> privInSet) {
    return this.access.postHqlFilterGroups(this.s, groups, subject, privInSet);
  }

  /**
   * @see   AccessResolver#privilegeCopy(Group, Group, Privilege)
   * @see   AccessAdapter#privilegeCopy(GrouperSession, Group, Group, Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    try {
      this.access.privilegeCopy(this.s, g1, g2, priv);
    } catch (InsufficientPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (GrantPrivilegeAlreadyExistsException e) {
      throw new UnableToPerformAlreadyExistsException(e.getMessage(), e);
    } catch (GrantPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (SchemaException e) {
      throw new GrouperException("unexpected condition");
    }
  }
  
  /**
   * @see   AccessResolver#privilegeCopy(Subject, Subject, Privilege)
   * @see   AccessAdapter#privilegeCopy(GrouperSession, Subject, Subject, Privilege)
   */
   public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
     try {
       this.access.privilegeCopy(this.s, subj1, subj2, priv);
     } catch (InsufficientPrivilegeException e) {
       throw new UnableToPerformException(e.getMessage(), e);
     } catch (GrantPrivilegeAlreadyExistsException e) {
       throw new UnableToPerformAlreadyExistsException(e.getMessage(), e);
     } catch (GrantPrivilegeException e) {
       throw new UnableToPerformException(e.getMessage(), e);
     } catch (SchemaException e) {
       throw new GrouperException("unexpected condition");
     } 
   }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
  }            

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {
    return this.access.hqlFilterGroupsWhereClause(this.s, subject, hqlQuery, hql, groupColumn, privInSet);
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    return this.s;
  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterMemberships(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(Subject subject,
      Set<Membership> memberships) {
    return this.access.postHqlFilterMemberships(this.s, subject, memberships);
  }            



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
  }

}

