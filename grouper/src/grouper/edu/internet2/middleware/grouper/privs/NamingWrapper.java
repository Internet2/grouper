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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
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
 * Class implementing wrapper around {@link NamingAdapter} interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NamingWrapper.java,v 1.18 2009-10-03 13:47:13 shilen Exp $
 * @since   1.2.1
 */
public class NamingWrapper implements NamingResolver {

  /**
   * @see NamingResolver#getStemsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Stem> getStemsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.naming.getStemsWhereSubjectDoesntHavePrivilege(this.s, stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#flushCache()
   */
  public void flushCache() {
  }            

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    return this.s;
  }

  /** */
  private NamingAdapter   naming;
  
  /** */
  private ParameterHelper param;
  
  /** */
  private GrouperSession  s;



  /**
   * Facade around {@link NamingAdapter} that implements {@link NamingResolver}.
   * @param session 
   * @param naming 
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public NamingWrapper(GrouperSession session, NamingAdapter naming) 
    throws  IllegalArgumentException
  {
    this.param  = new ParameterHelper();
    this.param.notNullGrouperSession(session).notNullNamingAdapter(naming);
    this.s      = session;
    this.naming = naming;
  }



  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @see     NamingAdapter#getStemsWhereSubjectHasPriv(GrouperSession, Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    try {
      return this.naming.getStemsWhereSubjectHasPriv(this.s, subject, privilege);
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); 
    }
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @see     NamingAdapter#getPrivs(GrouperSession, Stem, Subject)
   * @since   1.2.1
   */
  public Set<NamingPrivilege> getPrivileges(Stem stem, Subject subject)
    throws  IllegalArgumentException
  {
    return this.naming.getPrivs(this.s, stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @see     NamingAdapter#getSubjectsWithPriv(GrouperSession, Stem, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException
  {
    try {
      return this.naming.getSubjectsWithPriv(this.s, stem, privilege);
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); 
    }
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege, String)
   * @see     NamingAdapter#grantPriv(GrouperSession, Stem, Subject, Privilege, String)
   * @since   1.2.1
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege, String uuid)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    try {
      this.naming.grantPriv(this.s, stem, subject, privilege, uuid);
    }
    catch (GrantPrivilegeAlreadyExistsException eGrant) {
      throw new UnableToPerformAlreadyExistsException( eGrant.getMessage(), eGrant );
    }
    catch (GrantPrivilegeException eGrant) {
      throw new UnableToPerformException( eGrant.getMessage(), eGrant );
    }
    catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); 
    }
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @see     NamingAdapter#hasPriv(GrouperSession, Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    try {
      return this.naming.hasPriv(this.s, stem, subject, privilege);
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); 
    }
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @see     NamingAdapter#revokePriv(GrouperSession, Stem, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    try {
      this.naming.revokePriv(this.s, stem, privilege);
    }
    catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    }
    catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException( eRevoke.getMessage(), eRevoke );
    }
    catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); 
    }
  }
            

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @see     NamingAdapter#revokePriv(GrouperSession, Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    try {
      this.naming.revokePriv(this.s, stem, subject, privilege);
    } catch (InsufficientPrivilegeException ePrivs) {
      throw new UnableToPerformException( ePrivs.getMessage(), ePrivs );
    } catch (RevokePrivilegeAlreadyRevokedException eRevoke) {
      throw new UnableToPerformAlreadyExistsException( eRevoke.getMessage(), eRevoke );
    } catch (RevokePrivilegeException eRevoke) {
      throw new UnableToPerformException( eRevoke.getMessage(), eRevoke );
    } catch (SchemaException eSchema) {
      throw new GrouperException("unexpected condition", eSchema); 
    }
  }     
  
  /**
   * @see   NamingResolver#privilegeCopy(Stem, Stem, Privilege)
   * @see   NamingAdapter#privilegeCopy(GrouperSession, Stem, Stem, Privilege)
   */
  public void privilegeCopy(Stem stem1, Stem stem2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    try {
      this.naming.privilegeCopy(this.s, stem1, stem2, priv);
    } catch (GrantPrivilegeAlreadyExistsException e) {
      throw new UnableToPerformAlreadyExistsException(e.getMessage(), e);
    } catch (GrantPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (InsufficientPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (SchemaException e) {
      throw new GrouperException("unexpected condition", e);
    }
  }

  /**
   * @see   NamingResolver#privilegeCopy(Subject, Subject, Privilege)
   * @see   NamingAdapter#privilegeCopy(GrouperSession, Subject, Subject, Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    try {
      this.naming.privilegeCopy(this.s, subj1, subj2, priv);
    } catch (GrantPrivilegeAlreadyExistsException e) {
      throw new UnableToPerformAlreadyExistsException(e.getMessage(), e);
    } catch (GrantPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (InsufficientPrivilegeException e) {
      throw new UnableToPerformException(e.getMessage(), e);
    } catch (SchemaException e) {
      throw new GrouperException("unexpected condition", e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#stop()
   */
  public void stop() {
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterStemsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Set<Privilege> privInSet) {
    return this.naming.hqlFilterStemsWhereClause(this.s, subject, hqlQuery, hql, stemColumn, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#postHqlFilterStems(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(Set<Stem> stems, Subject subject,
      Set<Privilege> privInSet) {
    return this.naming.postHqlFilterStems(this.s, stems, subject, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.naming.revokeAllPrivilegesForSubject(this.s, subject);
  }            

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsNotWithPrivWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege, boolean)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {
    return this.naming.hqlFilterStemsNotWithPrivWhereClause(this.s, subject, hqlQuery, hql, stemColumn, privilege, considerAllSubject);
  }

}

