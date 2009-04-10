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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * This is the base grouper implementation which implements the required
 * access adapter methods, but not the db specific ones.  This should be
 * slower and more explicit than the GrouperAccessAdapter (subclass)
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNonDbAccessAdapter.java,v 1.1.2.1 2009-04-10 18:44:21 mchyzer Exp $
 */
public class GrouperNonDbAccessAdapter extends BaseAccessAdapter implements AccessAdapter {

  /** */
  protected final static Map<Privilege, String> priv2list;

  // STATIC //
  static {
    Map<Privilege, String> map = new HashMap<Privilege, String>();
    map.put(  AccessPrivilege.ADMIN , "admins"    );
    map.put(  AccessPrivilege.OPTIN , "optins"    );
    map.put(  AccessPrivilege.OPTOUT, "optouts"   );
    map.put(  AccessPrivilege.READ  , "readers"   );
    map.put(  AccessPrivilege.UPDATE, "updaters"  );
    map.put(  AccessPrivilege.VIEW  , "viewers"   );
    priv2list = Collections.unmodifiableMap(new HashMap(map));
  } // static


  // PUBLIC INSTANCE METHODS //

  /**
   * Get all subjects with this privilege on this group.
   * <pre class="eg">
   * try {
   *   Set admins = ap.getSubjectsWithPriv(s, g, AccessPrivilege.ADMIN);
   * }
   * catch (SchemaException eS) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   g     Get privileges on this group.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Subject} objects.
   * @throws  SchemaException
   */
  public Set getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    return MembershipFinder.internal_findSubjects(
      s, g, FieldFinder.find( (String) priv2list.get(priv) )
    );
  } // public Set getSubjectsWithpriv(s, g, priv)

  /**
   * Get all groups where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isAdmin = ap.getGroupsWhereSubjectHasPriv(
   *     s, subj, AccessPrivilege.ADMIN
   *   );
   * }
   * catch (SchemaException eS) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Group} objects.
   * @throws  SchemaException
   */
  public Set getGroupsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
      // This subject
      groups.addAll( 
        GrouperPrivilegeAdapter.internal_getGroupsWhereSubjectHasPriv( s, MemberFinder.findBySubject(s, subj), f ) 
      );
      // The ALL subject
      if ( !( SubjectHelper.eq(subj, SubjectFinder.findAllSubject() ) ) ) {
        groups.addAll( 
          GrouperPrivilegeAdapter.internal_getGroupsWhereSubjectHasPriv( s, MemberFinder.internal_findAllMember(), f ) 
        );
      }
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.GAA_GNF + eGNF.getMessage();
      LOG.error(msg);
    }
    return groups;
  } // public Set getGroupsWhereSubjectHasPriv(s, subj, priv)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperNonDbAccessAdapter.class);

  /**
   * Get all privileges held by this subject on this group.
   * <pre class="eg">
   * Set privs = ap.getPrivs(s, g, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   g     Get privileges on this group.
   * @param   subj  Get privileges for this member.
   * @return  Set of privileges.
   */
  public Set getPrivs(GrouperSession s, Group g, Subject subj) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member        m     = MemberFinder.findBySubject(s, subj);

      MembershipDAO dao   = GrouperDAOFactory.getFactory().getMembership();
      Iterator      it;

		
	   //2007-11-02 Gary Brown
	   //Avoid doing 6 queries - get everything at once
	   //Also don't add GropuperAll privs - do that in 
	   //GrouperAllAccessResolver
        it  = dao.findAllByOwnerAndMember( g.getUuid(), m.getUuid()).iterator(); 
        privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, g,subj, m, null, it) );
        /*
         * Done through GrouperAllAccessAdapter
         * if (!m.equals(all)) {
          it  = dao.findAllByOwnerAndMemberAndField( g.getUuid(), ( (MemberDTO) all.getDTO() ).getUuid(), f ).iterator();
          privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, subj, all, p, it) );
        }*/
     
    }
    catch (SchemaException eS) {
      LOG.error( eS.getMessage());
    }
    return privs;
  } // public Set getPrivs(s, g, subj)

  /**
   * Grant the privilege to the subject on this group.
   * <pre class="eg">
   * try {
   *   ap.grantPriv(s, g, subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Unable to grant the privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to grant the privilege
   * }
   * </pre>
   * @param   s     Grant privilege in this session context.
   * @param   g     Grant privilege on this group.
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.   
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(
    GrouperSession s, final Group g, final Subject subj, final Privilege priv)
    throws  GrantPrivilegeException, InsufficientPrivilegeException, SchemaException {
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            GrouperSession.validate(grouperSession);
            Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
            if ( !FieldType.ACCESS.equals( f.getType() ) ) {
              throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
            }
            if ( !g.internal_canWriteField( grouperSession.getSubject(), f ) ) {
              throw new GrouperSessionException(new InsufficientPrivilegeException());
            }
            Membership.internal_addImmediateMembership(grouperSession, g, subj, f);
          } catch (MemberAddException eMA) {
            if (eMA instanceof MemberAddAlreadyExistsException) {
              throw new GrouperSessionException(new GrantPrivilegeAlreadyExistsException(eMA.getMessage(), eMA));
            }
            throw new GrouperSessionException(new GrantPrivilegeException(eMA.getMessage(), eMA));
          } catch (SchemaException se) {
            throw new GrouperSessionException(se);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GrantPrivilegeException) {
        throw (GrantPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      throw gse;
    }
  } // public void grantPriv(s, g, subj, priv)

  /**
   * Check whether the subject has this privilege on this group.
   * <pre class="eg">
   * try {
   *   ap.hasPriv(s, g, subject, AccessPrivilege.ADMIN);
   * }
   * catch (SchemaException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   g     Check privilege on this group.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @return if has priv
   * @throws  SchemaException
   */
  public boolean hasPriv(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    boolean rv = false;
    Member m = MemberFinder.findBySubject(s, subj);
    rv = m.isMember(g, GrouperPrivilegeAdapter.internal_getField(priv2list, priv) );
    return rv;
  } // public boolean hasPriv(s, g, subj, priv)

  /**
   * Revoke this privilege from everyone on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, AccessPrivilege.ADMIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   g     Revoke privilege on this group.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
    if ( !FieldType.ACCESS.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !g.internal_canWriteField( s.getSubject(), f ) ) {
      throw new InsufficientPrivilegeException();
    }
    g.internal_setModified();
    try {
      GrouperDAOFactory.getFactory().getGroup().revokePriv( g, Membership.internal_deleteAllField(s, g, f) );
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, g, priv)

  /**
   * Revoke the privilege from the subject on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, subj, AccessPrivilege.ADMIN);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to grant the privilege
   * }
   * catch (RevokePrivilegeException e2) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   g     Revoke privilege on this group.
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
    if ( !FieldType.ACCESS.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !g.internal_canWriteField( s.getSubject(), f ) ) {
      throw new InsufficientPrivilegeException();
    }
    try {
      DefaultMemberOf mof = Membership.internal_delImmediateMembership(s, g, subj, f);
      g.internal_setModified();
      GrouperDAOFactory.getFactory().getGroup().revokePriv( g, mof);
    } catch (MemberDeleteAlreadyDeletedException eMD) {
      throw new RevokePrivilegeAlreadyRevokedException( eMD.getMessage(), eMD );
    } catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, g, subj, priv)

} // public class GrouperAccessAdapter 

