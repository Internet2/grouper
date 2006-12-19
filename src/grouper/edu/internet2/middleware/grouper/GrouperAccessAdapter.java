/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  net.sf.hibernate.*;


/** 
 * Grouper Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Group} class.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperAccessAdapter.java,v 1.47 2006-12-19 17:37:41 blair Exp $
 */
public class GrouperAccessAdapter implements AccessAdapter {

  // PRIVATE CLASS VARIABLES //
  private static Map priv2list = new HashMap();


  // STATIC //
  static {
    priv2list.put(  AccessPrivilege.ADMIN , "admins"    );
    priv2list.put(  AccessPrivilege.OPTIN , "optins"    );
    priv2list.put(  AccessPrivilege.OPTOUT, "optouts"   );
    priv2list.put(  AccessPrivilege.READ  , "readers"   );
    priv2list.put(  AccessPrivilege.UPDATE, "updaters"  );
    priv2list.put(  AccessPrivilege.VIEW  , "viewers"   );
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
    GrouperSessionValidator.validate(s);
    return MembershipFinder.findSubjects(
      s, g, (Field) FieldFinder.find( (String) priv2list.get(priv) )
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
    GrouperSessionValidator.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Field f = GrouperPrivilegeAdapter.getField(priv2list, priv);
      // This subject
      groups.addAll( 
        GrouperPrivilegeAdapter.internal_getGroupsWhereSubjectHasPriv( s, MemberFinder.findBySubject(s, subj), f ) 
      );
      // The ALL subject
      if ( !( SubjectHelper.eq(subj, SubjectFinder.findAllSubject() ) ) ) {
        groups.addAll( 
          GrouperPrivilegeAdapter.internal_getGroupsWhereSubjectHasPriv( s, MemberFinder.findAllMember(), f ) 
        );
      }
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.GAA_GNF + eGNF.getMessage();
      ErrorLog.error(GrouperAccessAdapter.class, msg);
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.GPA_MNF + eMNF.getMessage();
      ErrorLog.error(GrouperAccessAdapter.class, msg);
    }
    return groups;
  } // public Set getGroupsWhereSubjectHasPriv(s, subj, priv)

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
    GrouperSessionValidator.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      Member    all   = MemberFinder.findAllMember();     
      Privilege p;
      Iterator  iterP = Privilege.getAccessPrivs().iterator();
      while (iterP.hasNext()) {
        p             = (Privilege) iterP.next();
        Field   f     = GrouperPrivilegeAdapter.getField(priv2list, p);
        Iterator  iterM = MembershipFinder.internal_findAllByOwnerAndMemberAndField(g, m, f).iterator();
        privs.addAll( GrouperPrivilegeAdapter.getPrivs(s, subj, m, p, iterM) );
        if (!m.equals(all)) {
          Iterator  iterA = MembershipFinder.internal_findAllByOwnerAndMemberAndField(g, all, f).iterator();
          privs.addAll( GrouperPrivilegeAdapter.getPrivs(s, subj, all, p, iterA) );
        }
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.GPA_MNF + eMNF.getMessage();
      ErrorLog.error(GrouperAccessAdapter.class, msg);
    }
    catch (SchemaException eS) {
      ErrorLog.error(GrouperAccessAdapter.class, eS.getMessage());
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
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
  {
    try {
      GrouperSessionValidator.validate(s);
      Field f = GrouperPrivilegeAdapter.getField(priv2list, priv);
      GroupValidator.isTypeEqual(f, FieldType.ACCESS);
      GroupValidator.canWriteField(g, s.getSubject(), f);
      Membership.addImmediateMembership(s, g, subj, f);
    }
    catch (MemberAddException eMA) {
      throw new GrantPrivilegeException(eMA.getMessage(), eMA);
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
   * @throws  SchemaException
   */
  public boolean hasPriv(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  SchemaException
  {
    GrouperSessionValidator.validate(s);
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      rv = m.isMember(g, GrouperPrivilegeAdapter.getField(priv2list, priv) );
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.GPA_MNF + eMNF.getMessage();
      ErrorLog.error(GrouperAccessAdapter.class, msg);
    }
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
    GrouperSessionValidator.validate(s);
    Field f = GrouperPrivilegeAdapter.getField(priv2list, priv);
    GroupValidator.isTypeEqual(f, FieldType.ACCESS);
    GroupValidator.canWriteField(g, s.getSubject(), f);

    // The objects that will need updating and deleting
    Set saves   = new LinkedHashSet();
    Set deletes = new LinkedHashSet();

    g.setModified();
    saves.add(g);

    try {
      // Find privileges that need to be revoked and then update registry
      deletes = Membership.deleteAllField(s, g, f);
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (Exception e) {
      throw new RevokePrivilegeException(e.getMessage(), e);
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
    try {
      GrouperSessionValidator.validate(s);
      Field f = GrouperPrivilegeAdapter.getField(priv2list, priv);
      GroupValidator.isTypeEqual(f, FieldType.ACCESS);
      GroupValidator.canWriteField(g, s.getSubject(), f);
      MemberOf  mof     = Membership.delImmediateMembership(s, g, subj, f);
      Set       saves   = mof.getSaves();
      Set       deletes = mof.getDeletes();

      g.setModified(); // TODO 20061019 Should this be in _Group_?
      saves.add(g);

      // And then commit changes to registry
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (HibernateException eH) {
      throw new RevokePrivilegeException(eH);
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException(eMD);
    }
  } // public void revokePriv(s, g, subj, priv)

} // public class GrouperAccessAdapter 

