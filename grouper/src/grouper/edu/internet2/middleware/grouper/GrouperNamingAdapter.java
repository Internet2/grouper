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
import  org.apache.commons.logging.*;


/** 
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingAdapter.java,v 1.34.2.2 2006-04-19 22:55:14 blair Exp $
 */
public class GrouperNamingAdapter implements NamingAdapter {

  // Private Class Constants
  private static final String ERR_GP    = "grantPriv: unable to grant priv: ";
  private static final String ERR_MMNF  = "membership member not found: ";
  private static final String ERR_MSNF  = "membership stem not found: ";
  private static final String ERR_RP    = "revokePriv: unable to revoke priv: ";
  private static final Log    LOG       = LogFactory.getLog(GrouperNamingAdapter.class);
  private static final String MSG_GP    = "grantPriv: ";


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
    return MembershipFinder.findSubjects(s, ns, this._getField(priv));
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
      // The subject
      Member    m   = MemberFinder.findBySubject(s, subj);
      Iterator iter = MembershipFinder.findMemberships(
        s, m, this._getField(priv)
      ).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        ms.setSession(s);
        stems.add( ms.getStem() );
      }
      // And the ALL subject
      Member    all     = MemberFinder.findAllMember();
      Iterator  iterAll = MembershipFinder.findMemberships(
        s, all, this._getField(priv)
      ).iterator();
      while (iterAll.hasNext()) {
        Membership ms = (Membership) iterAll.next();
        ms.setSession(s);
        stems.add( ms.getStem() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      GrouperLog.error(LOG, s, ERR_MMNF + eMNF.getMessage());
    }
    catch (StemNotFoundException eSNF) {
      GrouperLog.error(LOG, s, ERR_MSNF + eSNF.getMessage());
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
      Member    m   = MemberFinder.findBySubject(s, subj);
      Member    all = MemberFinder.findAllMember();     
      Iterator  iterP = Privilege.getNamingPrivs().iterator();
      while (iterP.hasNext()) {
        Privilege p = (Privilege) iterP.next();
        Field     f = this._getField(p);   
        Iterator  iterM = MembershipFinder.findMemberships(ns, m, f).iterator();
        privs.addAll( this._getPrivs(s, ns, subj, m, p, iterM) );
        Iterator  iterA = MembershipFinder.findMemberships(ns, all, f).iterator();
        privs.addAll( this._getPrivs(s, ns, subj, all, p, iterA) );
      }
    }
    catch (MemberNotFoundException eMNF) {
      GrouperLog.error(LOG, s, ERR_MMNF + eMNF.getMessage());
    }
    catch (SchemaException eS) {
      // Well, this is strange.  Ignore.
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
    try {
      GrouperSessionValidator.validate(s);
      Field f = this._getField(priv);
      StemValidator.canWriteField(s, ns, s.getSubject(), f, FieldType.NAMING);
      Membership.addImmediateMembership(s, ns, subj, f);
    }
    catch (MemberAddException eMA) {
      throw new GrantPrivilegeException(eMA.getMessage());
    }
    catch (ModelException eM) {
      throw new GrantPrivilegeException(eM.getMessage());
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
    GrouperSession.validate(s);
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      rv = m.isMember(ns, this._getField(priv));
    }
    catch (MemberNotFoundException eMNF) {
      LOG.error(ERR_MMNF + eMNF.getMessage());
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
    try {
      GrouperSessionValidator.validate(s);
      Field f = this._getField(priv);
      StemValidator.canWriteField(s, ns, s.getSubject(), f, FieldType.NAMING);

      // The objects that will need updating and deleting
      Set     saves   = new LinkedHashSet();
      Set     deletes = new LinkedHashSet();

      ns.setModified();
      saves.add(ns);

      try {
        // Find privileges that need to be revoked and then update registry
        deletes = Membership.deleteAllField(s, ns, f);
        HibernateHelper.saveAndDelete(saves, deletes);
      }
      catch (Exception e) {
        throw new RevokePrivilegeException(e.getMessage());
      }
    }
    catch (ModelException eM) {
      throw new RevokePrivilegeException(eM.getMessage());
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
    try {
      GrouperSessionValidator.validate(s);
      Field f = this._getField(priv);
      StemValidator.canWriteField(s, ns, s.getSubject(), f, FieldType.NAMING);
      Membership.delImmediateMembership(s, ns, subj, f);
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException(eMD.getMessage());
    }
    catch (ModelException eM) {
      throw new RevokePrivilegeException(eM.getMessage());
    }
  } // public void revokePriv(s, ns, subj, priv)

  
  // Private Instance Methods //
  private Field _getField(Privilege priv) 
    throws  SchemaException
  {
    if (priv2list.containsKey(priv)) {
      return FieldFinder.find( (String) priv2list.get(priv) );
    }
    throw new SchemaException("invalid naming privilege");
  } // private Field _getField(priv)

  // Return appropriate _NamingPrivilege_ objects 
  private Set _getPrivs(
    GrouperSession s, Stem ns, Subject subj, Member m, Privilege p, Iterator iter
  )
    throws  SchemaException
  {
    Set privs = new LinkedHashSet();
    while (iter.hasNext()) {
      Membership  ms      = (Membership) iter.next();
      ms.setSession(s);
      Subject     owner   = subj;
      boolean     revoke  = true;
      try {
        if (!SubjectHelper.eq(m.getSubject(), subj)) {
          owner   = m.getSubject();
          revoke  = false;
        }
      }
      catch (SubjectNotFoundException eSNF) {
        // bloody odd
      }
      try {
        owner   = ms.getViaGroup().toSubject();
        revoke  = false;
      }
      catch (GroupNotFoundException eGNF) {
        // ignore
      }
      try {
        privs.add(
          new NamingPrivilege(
            ms.getStem()  , subj              , owner,
            p             , s.getNamingClass(), revoke
          )
        );
      }
      catch (StemNotFoundException eGNF) {
        // this is also bloody odd
      }
    }
    return privs;
  } // private Set _get(s, g, subj, m, p, iter)

}

