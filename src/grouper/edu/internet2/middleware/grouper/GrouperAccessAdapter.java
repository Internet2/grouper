/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * @version $Id: GrouperAccessAdapter.java,v 1.15 2005-12-02 19:28:46 blair Exp $
 */
public class GrouperAccessAdapter implements AccessAdapter {

  // Private Class Variables
  private static Map priv2list = new HashMap();

  static {
    priv2list.put(  AccessPrivilege.ADMIN , "admins"    );
    priv2list.put(  AccessPrivilege.OPTIN , "optins"    );
    priv2list.put(  AccessPrivilege.OPTOUT, "optouts"   );
    priv2list.put(  AccessPrivilege.READ  , "readers"   );
    priv2list.put(  AccessPrivilege.UPDATE, "updaters"  );
    priv2list.put(  AccessPrivilege.VIEW  , "viewers"   );
  } // static

  // Public Instance Methods

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
    GrouperSession.validate(s);
    return MembershipFinder.findSubjects(
      s, g.getUuid(), 
      (Field) FieldFinder.find( (String) priv2list.get(priv) )
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
    GrouperSession.validate(s);
    Set groups = new LinkedHashSet();
    try {
      Member    m   = MemberFinder.findBySubject(s, subj);
      Iterator iter = MembershipFinder.findMemberships(
        m, (Field) FieldFinder.find( (String) priv2list.get(priv) )
      ).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        ms.setSession(s);
        groups.add( ms.getGroup() );
      }
    }
    catch (GroupNotFoundException eGNF) {
      throw new RuntimeException(eGNF.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(eMNF.getMessage());
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
    GrouperSession.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      Iterator  iterP = Privilege.getAccessPrivs().iterator();
      while (iterP.hasNext()) {
        Privilege p = (Privilege) iterP.next();
        Iterator  iterM = MembershipFinder.findMemberships(
          g.getUuid(), m, 
          (Field) FieldFinder.find( (String) priv2list.get(p) )
        ).iterator();
        while (iterM.hasNext()) {
          Membership  ms      = (Membership) iterM.next();
          Subject     owner   = subj;
          // TODO Should take privs into account as well
          boolean     revoke  = true;
          ms.setSession(s);
          try {
            owner   = ms.getViaGroup().toSubject();
            revoke  = false;
          }
          catch (GroupNotFoundException eGNF) {
            // Ignore
          }
          privs.add(
            new AccessPrivilege(
              ms.getGroup(),  subj,               owner,
              p           ,   s.getAccessClass(), revoke
            )
          );
        }
      }
    }
    catch (GroupNotFoundException eGNF) {
      throw new RuntimeException(
        "error getting privs: " + eGNF.getMessage()
      );
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(
        "could not convert subject to member: " + eMNF.getMessage()
      );  
    }
    catch (SchemaException eS) {
      throw new RuntimeException(
        "error getting privs: " + eS.getMessage()
      );
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
   */
  public void grantPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws GrantPrivilegeException, 
           InsufficientPrivilegeException
  {
    GrouperSession.validate(s);
    try {
      g.addMember(
        subj, FieldFinder.find( (String) priv2list.get(priv) ) 
      );
    }
    catch (MemberAddException eMA) {
      throw new GrantPrivilegeException(
        "unable to grant priv: " + eMA.getMessage()
      );
    }
    catch (SchemaException eS) {
      throw new GrantPrivilegeException(
        "unable to grant priv: " + eS.getMessage()
      ); 
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
    GrouperSession.validate(s);
    try {
      Field   f   = FieldFinder.find( (String) priv2list.get(priv));
      Member  m   = MemberFinder.findBySubject(s, subj);
      if (MembershipFinder.findMemberships(g.getUuid(), m, f).size() > 0) {
        return true;
      }
      return false;
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(
        "could not convert subject to member: " + eMNF.getMessage()
      );  
    }
    catch (SchemaException eS) {
      throw new SchemaException("invalid privilege: " + priv);
    }
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
   */
  public void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException 
  {
    GrouperSession.validate(s);
    try {
      // The objects that will need updating and deleting
      Set     saves   = new LinkedHashSet();
      Set     deletes = new LinkedHashSet();

      // Update stem modify time
      g.setModified();
      saves.add(g);

      // Find every subject that needs to have the priv revoked
      Iterator iter = MembershipFinder.findImmediateSubjects(
        s, g.getUuid(), FieldFinder.find( (String) priv2list.get(priv))
      ).iterator();
      while (iter.hasNext()) {
        Subject subj  = (Subject) iter.next();
        Member  m     = MemberFinder.findBySubject(s, subj);

        // This is the immediate privilege that needs to be deleted
        deletes.add(
          MembershipFinder.findImmediateMembership(
            s, g.getUuid(), m, FieldFinder.find( (String) priv2list.get(priv) )
          )
        );

        // As many of the privileges are likely to be transient, we
        // need to retrieve the persistent version of each before
        // passing it along to be deleted by HibernateHelper.  
        Session   hs    = HibernateHelper.getSession();
        Iterator  iterM = MemberOf.doMemberOf(s, g, m).iterator();
        while (iterM.hasNext()) {
          Membership ms = (Membership) iterM.next();
          deletes.add( 
            MembershipFinder.findEffectiveMembership(
              ms.getOwner_id(), ms.getMember_id(), 
              ms.getList(), ms.getVia_id(), ms.getDepth()
            )
          );
        }
        hs.close();
      }

      // And then update the registry
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (GroupNotFoundException eGNF) {
      throw new RevokePrivilegeException(
        "could not revoke privilege: " + eGNF.getMessage()
      );
    }
    catch (HibernateException eH) {
      throw new RevokePrivilegeException(
        "could not revoke privilege: " + eH.getMessage()
      );
    }
    catch (MemberNotFoundException eMNF) {
      throw new RevokePrivilegeException(
        "could not revoke privilege: " + eMNF.getMessage()
      );
    }
    catch (MembershipNotFoundException eMSNF) {
      throw new RevokePrivilegeException(
        "could not revoke privilege: " + eMSNF.getMessage()
      );
    }
    catch (SchemaException eS) {
      throw new RevokePrivilegeException(
        "could not revoke privilege: " + eS.getMessage()
      );
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
   */
  public void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException
  {
    GrouperSession.validate(s);
    try {
      g.deleteMember(
        subj, FieldFinder.find( (String) priv2list.get(priv) ) 
      );
    }
    catch (MemberDeleteException eMA) {
      throw new RevokePrivilegeException(
        "unable to revoke priv: " + eMA.getMessage()
      );
    }
    catch (SchemaException eS) {
      throw new RevokePrivilegeException(
        "unable to revoke priv: " + eS.getMessage()
      ); 
    }
  } // public void revokePriv(s, g, subj, priv)


  // Private Instance Methods
  private void _canFieldDispatch(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if      (priv.equals(AccessPrivilege.ADMIN))  { 
      this._canADMIN(s, g, subj, priv);
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      this._canOPTIN(s, g, subj, priv);
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      this._canOPTOUT(s, g, subj, priv);
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      this._canREAD(s, g, subj, priv);
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      this._canUPDATE(s, g, subj, priv);
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      this._canVIEW(s, g, subj, priv);
    }
    else {
      throw new SchemaException("unknown access privilege: " + priv);
    }
  } // private void _canFieldDispatch(s, g, subj, priv)

  private void _canADMIN(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException
  {
    if (!PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // private void _canADMIN(s, g, subj, priv)

  private void _canOPTIN(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException
  {
    if (!PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // private void _canOPTIN(s, g, subj, priv)

  private void _canOPTOUT(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException
  {
    if (!PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // private void _canOPTOUT(s, g, subj, priv)

  private void _canREAD(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException
  {
    if (!PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // private void _canREAD(s, g, subj, priv)

  private void _canUPDATE(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException
  {
    if (!PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // private void _canUPDATE(s, g, subj, priv)

  private void _canVIEW(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException
  {
    if (!PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // private void _canVIEW(s, g, subj, priv)

  private void _canWriteField(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    Field f = (Field) FieldFinder.find( (String) priv2list.get(priv));
    this._canFieldDispatch(s, g, subj, f.getWritePriv());
  } // private void _canWriteField(s, g, subj, priv)
}

