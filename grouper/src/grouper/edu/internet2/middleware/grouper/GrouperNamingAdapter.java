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
import  org.apache.commons.logging.*;


/** 
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingAdapter.java,v 1.24 2005-12-06 19:42:19 blair Exp $
 */
public class GrouperNamingAdapter implements NamingAdapter {

  // Private Class Constants
  private static final String ERR_GP  = "grantPriv: unable to grant priv: ";
  private static final String ERR_RP  = "revokePriv: unable to revoke priv: ";
  private static final Log    LOG     = LogFactory.getLog(GrouperNamingAdapter.class);
  private static final String MSG_GP  = "grantPriv: ";


  // Private Class Variables
  private static Map priv2list = new HashMap();

  static {
    priv2list.put(  NamingPrivilege.CREATE, "creators"  );
    priv2list.put(  NamingPrivilege.STEM  , "stemmers"  );
  } // static


  // Public Instance Methods

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
    GrouperSession.validate(s);
    return MembershipFinder.findSubjects(s, ns.getUuid(), this._getField(priv));
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
    GrouperSession.validate(s);
    Set stems = new LinkedHashSet();
    try {
      Member    m   = MemberFinder.findBySubject(s, subj);
      Iterator iter = MembershipFinder.findMemberships(
        m, this._getField(priv)
      ).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        ms.setSession(s);
        stems.add( ms.getStem() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(eMNF.getMessage());
    }
    catch (StemNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
    return stems;
  } // public Set getStemsWhereSubjectHasPriv(s, subj, priv)

  /**
   * Get all privileges held by this subject on this stem.
   * <p>
   * TODO What type of objects should be returned?  Review Gary's
   * proposals for ideas.
   * </p>
   * <p>
   * TODO And should be explicitly included?
   * </p>
   * <pre class="eg">
   * Set privs = np.getPrivs(s, ns, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   ns    Get privileges on this stem.
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link NamingPrivilege} objects.
   */
  public Set getPrivs(GrouperSession s, Stem ns, Subject subj) {
    GrouperSession.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      Iterator  iterP = Privilege.getNamingPrivs().iterator();
      while (iterP.hasNext()) {
        Privilege priv = (Privilege) iterP.next();
        Iterator  iterM = MembershipFinder.findMemberships(
          ns.getUuid(), m, this._getField(priv)
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
            new NamingPrivilege(
              ms.getStem(), subj,               owner,
              priv        , s.getNamingClass(), revoke
            )
          );
        }
      }
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
    catch (StemNotFoundException eSNF) {
      throw new RuntimeException(
        "error getting privs: " + eSNF.getMessage()
      );
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
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
  {
    String msg = MSG_GP + "'" + priv.toString().toUpperCase() + "' " 
      + SubjectHelper.getPretty(subj);
    GrouperLog.debug(LOG, s, msg);
    this._canWriteList(s, ns, priv, msg);
    try {
      // The objects that will need saving
      Set     objects = new LinkedHashSet();
      // Who we're adding
      Member  m       = PrivilegeResolver.getInstance().canViewSubject(
        s, subj, msg
      );

      ns.setModified();
      objects.add(ns);

      Field f = this._getField(priv);

      // Create the immediate membership
      objects.add(Membership.addMembership(s, ns, m, f));

      // Find effective memberships
      Set effs = MemberOf.doMemberOf(s, ns, m, f);
      objects.addAll(effs);

      // And then save stem and memberships
      HibernateHelper.save(objects);
      // TODO make INFO + (conditionally?) log each privilege granted
      GrouperLog.debug(
        LOG, s, 
        msg + "added members: " + SubjectHelper.getPretty(subj) 
        + " and " + effs.size() + " effs"
      );
    }
    catch (HibernateException eH) {
      GrouperLog.debug(LOG, s, ERR_GP + eH.getMessage());
      throw new GrantPrivilegeException(ERR_GP + eH.getMessage());
    }
    catch (MemberAddException eMAF) {
      GrouperLog.debug(LOG, s, ERR_GP + eMAF.getMessage());
      throw new GrantPrivilegeException(ERR_GP + eMAF.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      GrouperLog.debug(LOG, s, ERR_GP + eMNF.getMessage());
      throw new GrantPrivilegeException(ERR_GP + eMNF.getMessage());
    }
    catch (StemNotFoundException eSNF) {
      GrouperLog.debug(LOG, s, ERR_GP + eSNF.getMessage());
      throw new GrantPrivilegeException(ERR_GP + eSNF.getMessage());
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
    throws SchemaException 
  {
    // TODO Use a _hasMember()_ or _isMember()_ variant?
    GrouperSession.validate(s);
    try {
      if (
        MembershipFinder.findMemberships(
          ns.getUuid(), MemberFinder.findBySubject(s, subj), this._getField(priv)
        ).size() > 0
      ) 
      {
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
   */
  public void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException 
  {
    // TODO Refactor into smaller chunks
    GrouperSession.validate(s);
    String msg_rp = "unable to revoke " + priv + " on " + ns.getName(); 
    try {
      // TODO Replace with _canWriteList
      this._canWriteField(s, ns, priv);

      // The objects that will need updating and deleting
      Set     saves   = new LinkedHashSet();
      Set     deletes = new LinkedHashSet();

      Field   f       = this._getField(priv);

      // Update stem modify time
      ns.setModified();
      saves.add(ns);

      // Find every subject that needs to have the priv revoked
      Iterator iter = MembershipFinder.findImmediateSubjects(
        s, ns.getUuid(), f
      ).iterator();
      while (iter.hasNext()) {
        Subject subj  = (Subject) iter.next();
        Member  m     = MemberFinder.findBySubject(s, subj);

        // This is the immediate privilege that needs to be deleted
        deletes.add(
          MembershipFinder.findImmediateMembership(
            s, ns.getUuid(), m, this._getField(priv)
          )
        );

        // As many of the privileges are likely to be transient, we
        // need to retrieve the persistent version of each before
        // passing it along to be deleted by HibernateHelper.  
        Session   hs    = HibernateHelper.getSession();
        Iterator  iterM = MemberOf.doMemberOf(s, ns, m, f).iterator();
        while (iterM.hasNext()) {
          Membership ms = (Membership) iterM.next();
          deletes.add( 
            MembershipFinder.findEffectiveMembership(
              ms.getOwner_id(), ms.getMember().getId(), 
              ms.getList(), ms.getVia_id(), ms.getDepth()
            )
          );
        }
        hs.close();
      }

      // And then update the registry
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (HibernateException eH) {
      throw new RevokePrivilegeException(ERR_RP + eH.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      throw new RevokePrivilegeException(ERR_RP + eMNF.getMessage());
    }
    catch (MembershipNotFoundException eMSNF) {
      throw new RevokePrivilegeException(ERR_RP + eMSNF.getMessage());
    }
    catch (SchemaException eS) {
      throw new RevokePrivilegeException(ERR_RP + eS.getMessage());
    }
    catch (StemNotFoundException eSNF) {
      throw new RevokePrivilegeException(ERR_RP + eSNF.getMessage());
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
   */
  public void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException 
  {
    // TODO Refactor into smaller components
    GrouperSession.validate(s);
    String msg_rp = "unable to revoke " + priv + " on " + ns.getName()
      + " from " + subj.getId();
    try {
      // TODO Replace with _canWriteList
      this._canWriteField(s, ns, priv);

      // Convert subject to a member
      Member  m       = MemberFinder.findBySubject(s, subj);

      // The objects that will need updating and deleting
      Set     saves   = new LinkedHashSet();
      Set     deletes = new LinkedHashSet();
      Field   f       = this._getField(priv);

      // Update stem modify time
      ns.setModified();
      saves.add(ns);

      // Find the immediate privilege that is to be deleted
      deletes.add(
        MembershipFinder.findImmediateMembership(s, ns.getUuid(), m, f)
      );

      // As many of the privileges are likely to be transient, we
      // need to retrieve the persistent version of each before
      // passing it along to be deleted by HibernateHelper.  
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = MemberOf.doMemberOf(s, ns, m, f).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        deletes.add( 
          MembershipFinder.findEffectiveMembership(
            ms.getOwner_id(), ms.getMember().getId(), 
            ms.getList(), ms.getVia_id(), ms.getDepth()
          )
        );
      }
      hs.close();

      // And then update the registry
      //HibernateHelper.delete(objects);
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (HibernateException eH) {
      throw new RevokePrivilegeException(ERR_RP + eH.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      throw new RevokePrivilegeException(ERR_RP + eMNF.getMessage());
    }
    catch (MembershipNotFoundException eMSNF) {
      throw new RevokePrivilegeException(ERR_RP + eMSNF.getMessage());
    }
    catch (SchemaException eS) {
      throw new RevokePrivilegeException(ERR_RP + eS.getMessage());
    }
    catch (StemNotFoundException eSNF) {
      throw new RevokePrivilegeException(ERR_RP + eSNF.getMessage());
    }
  } // public void revokePriv(s, ns, subj, priv)

  
  // Private Instance Methods
  // TODO Remove
  private void _canWriteField(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    PrivilegeResolver.getInstance().canPrivDispatch(
      s, ns, s.getSubject(), this._getField(priv).getWritePriv()
    );
  } // private void _canWriteField(s, ns, priv)

  private Field _getField(Privilege priv) 
    throws  SchemaException
  {
    return FieldFinder.find( (String) priv2list.get(priv) );
  } // private Field _getField(priv)

  // What a tangled mess this has become
  private void _canWriteList(
    GrouperSession s, Stem ns, Privilege p, String msg
  ) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO Validate that this is a "naming" field
    // First see if we can write to the desired list
    try {
      PrivilegeResolver.getInstance().canPrivDispatch(
        s, ns, s.getSubject(), this._getField(p).getWritePriv()
      );
      GrouperLog.debug(LOG, s, msg + "true");
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, msg + eIP.getMessage());
      throw new InsufficientPrivilegeException(msg + eIP.getMessage());
    }
  } // private void _canWriteList(s, ns, p, msg)

}

