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
import  edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import  edu.internet2.middleware.subject.*;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/** 
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingAdapter.java,v 1.66 2008-06-25 05:46:05 mchyzer Exp $
 */
public class GrouperNamingAdapter implements NamingAdapter {

  
  private static Map<Privilege, String> priv2list = new HashMap<Privilege, String>();


  // STATIC //
  static {
    priv2list.put(  NamingPrivilege.CREATE, "creators"  );
    priv2list.put(  NamingPrivilege.STEM  , "stemmers"  );
  } // static


  // PUBLIC INSTANCE METHODS //

  /**
   * Get all subjects with this privilege on this stem.
   * <pre class="eg">
   * try {
   *   Set stemmers = np.getSubjectsWithPriv(s, ns, NamingPrivilege.STEM);
   * }
   * catch (SchemaException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   ns    Get privileges on this stem.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Subject} objects.
   * @throws  SchemaException
   */
  public Set getSubjectsWithPriv(GrouperSession s, Stem ns, Privilege priv) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    return MembershipFinder.internal_findSubjectsNoPriv(
      s, ns, GrouperPrivilegeAdapter.internal_getField(priv2list, priv)
    );
  } // public Set getSubjectsWithPriv(s, ns, priv)

  /**
   * Get all stems where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isStemmer = np.getStemsWhereSubjectHasPriv(
   *     s, subj, NamingPrivilege.STEM
   *   );
   * }
   * catch (SchemaException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Stem} objects.
   * @throws  SchemaException
   */
  public Set getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<Stem> stems = new LinkedHashSet<Stem>();
    try {
      Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
      // This subject
      stems.addAll( 
        GrouperPrivilegeAdapter.internal_getStemsWhereSubjectHasPriv( s, MemberFinder.findBySubject(s, subj), f ) 
      );
      // The ALL subject
      if ( !( SubjectHelper.eq(subj, SubjectFinder.findAllSubject() ) ) ) {
        stems.addAll( 
          GrouperPrivilegeAdapter.internal_getStemsWhereSubjectHasPriv( s, MemberFinder.internal_findAllMember(), f ) 
        );
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.GPA_MNF + eMNF.getMessage();
      ErrorLog.error(GrouperNamingAdapter.class, msg);
    }
    catch (StemNotFoundException eSNF) {
      String msg = E.GNA_SNF + eSNF.getMessage();
      ErrorLog.error(GrouperNamingAdapter.class, msg);
    }
    return stems;
  } // public Set getStemsWhereSubjectHasPriv(s, subj, priv)

  /**
   * Get all privileges held by this subject on this stem.
   * <p/>
   * <pre class="eg">
   * Set privs = np.getPrivs(s, ns, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   ns    Get privileges on this stem.
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link NamingPrivilege} objects.
   */
  public Set getPrivs(GrouperSession s, Stem ns, Subject subj) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member        m     = MemberFinder.findBySubject(s, subj);
      Member        all   = MemberFinder.internal_findAllMember();     
      MembershipDAO dao   = GrouperDAOFactory.getFactory().getMembership();
      Privilege     p;
      Field         f;
      Iterator      it;
      Iterator      iterP = Privilege.getNamingPrivs().iterator();
      while (iterP.hasNext()) {
        p   = (Privilege) iterP.next();
        f   = GrouperPrivilegeAdapter.internal_getField(priv2list, p);   
        it  = dao.findAllByOwnerAndMemberAndField( ns.getUuid(), m.getUuid(), f ).iterator();
        privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, ns,subj, m, p, it) );
        if (!m.equals(all)) {
          it = dao.findAllByOwnerAndMemberAndField( ns.getUuid(), all.getUuid(), f ).iterator();
          privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, ns,subj, all, p, it) );
        }
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.GPA_MNF + eMNF.getMessage();
      ErrorLog.error(GrouperNamingAdapter.class, msg);
    }
    catch (SchemaException eS) {
      ErrorLog.error(GrouperNamingAdapter.class, eS.getMessage());
    }
    return privs;
  } // public Set getPrivs(s, ns, subj)

  /**
   * Grant the privilege to the subject on this stem.
   * <pre class="eg">
   * try {
   *   np.grantPriv(s, ns, subj, NamingPrivilege.STEM);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Unable to grant the privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to grant the privilege
   * }
   * </pre>
   * @param   s     Grant privilege in this session context.
   * @param   ns    Grant privilege on this stem.
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.   
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(
    GrouperSession s, final Stem ns, final Subject subj, final Privilege priv
  )
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
            PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), ns, grouperSession.getSubject(), f.getWritePriv() );
            if (!f.getType().equals(FieldType.NAMING)) {
              throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
            }  
            Membership.internal_addImmediateMembership(grouperSession, ns, subj, f);
          } catch (SchemaException se) {
            throw new GrouperSessionException(se);
          } catch (InsufficientPrivilegeException ipe) {
            throw new GrouperSessionException(ipe);
          } catch (MemberAddException eMA) {
            throw new GrouperSessionException(new GrantPrivilegeException(eMA.getMessage(), eMA));
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GrantPrivilegeException) {
        throw (GrantPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)gse.getCause();
      }
      throw gse;
    }
  } // public void grantPriv(s, ns, subj, priv)

  /**
   * Check whether the subject has this privilege on this stem.
   * <pre class="eg">
   * try {
   *   np.hasPriv(s, ns, subj, NamingPrivilege.STEM);
   * }
   * catch (SchemaException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   ns    Check privilege on this stem.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @throws  SchemaException
   */
  public boolean hasPriv(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws  SchemaException 
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      rv = m.isMember( ns.getUuid(), GrouperPrivilegeAdapter.internal_getField(priv2list, priv) );
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.GPA_MNF + eMNF.getMessage();
      ErrorLog.error(GrouperNamingAdapter.class, msg);
    }
    return rv;
  } // public boolean hasPriv(s, ns, subj, priv) 

  /**
   * Revoke this privilege from everyone on this stem.
   * <pre class="eg">
   * try {
   *   np.revokePriv(s, ns, NamingPrivilege.STEM);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   ns    Revoke privilege on this stem.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
    PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), ns, s.getSubject(), f.getWritePriv() );
    if (!f.getType().equals(FieldType.NAMING)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }  
    ns.internal_setModified();
    try {
      GrouperDAOFactory.getFactory().getStem().revokePriv( ns, Membership.internal_deleteAllField(s, ns, f) );
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, ns, priv)

  /**
   * Revoke the privilege from the subject on this stem.
   * <pre class="eg">
   * try {
   *   np.revokePriv(s, ns, subj, NamingPrivilege.STEM);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to grant the privilege
   * }
   * catch (RevokePrivilegeException e2) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   ns    Revoke privilege on this stem.
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = GrouperPrivilegeAdapter.internal_getField(priv2list, priv);
    PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), ns, s.getSubject(), f.getWritePriv() );
    if (!f.getType().equals(FieldType.NAMING)) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }  
    try {
      DefaultMemberOf mof = Membership.internal_delImmediateMembership(s, ns, subj, f);
      ns.internal_setModified();
      GrouperDAOFactory.getFactory().getStem().revokePriv( ns, mof );
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  } // public void revokePriv(s, ns, subj, priv)

} // public class GrouperNamingAdapter

