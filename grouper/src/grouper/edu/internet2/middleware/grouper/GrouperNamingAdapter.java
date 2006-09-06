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
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingAdapter.java,v 1.44 2006-09-06 15:30:40 blair Exp $
 */
public class GrouperNamingAdapter implements NamingAdapter {

  // PRIVATE CLASS VARIABLES //
  private static Map priv2list = new HashMap();


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
    GrouperSessionValidator.validate(s);
    return MembershipFinder.findSubjectsNoPriv(s, ns, this._getField(priv));
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
    GrouperSessionValidator.validate(s);
    Set stems = new LinkedHashSet();
    try {
      // The subject
      Member      m     = MemberFinder.findBySubject(s, subj);
      Membership  msS;
      Iterator    iter  = MembershipFinder.findMemberships(
        s, m, this._getField(priv)
      ).iterator();
      while (iter.hasNext()) {
        msS = (Membership) iter.next();
        msS.setSession(s);
        stems.add( msS.getStem() );
      }
      // And the ALL subject
      Member      all     = MemberFinder.findAllMember();
      Membership  msAll;
      Iterator    iterAll = MembershipFinder.findMemberships(
        s, all, this._getField(priv)
      ).iterator();
      while (iterAll.hasNext()) {
        msAll = (Membership) iterAll.next();
        msAll.setSession(s);
        stems.add( msAll.getStem() );
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
    GrouperSessionValidator.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      Member    all   = MemberFinder.findAllMember();     
      Privilege p;
      Field     f;
      Iterator  iterM;
      Iterator  iterA;
      Iterator  iterP = Privilege.getNamingPrivs().iterator();
      while (iterP.hasNext()) {
        p     = (Privilege) iterP.next();
        f     = this._getField(p);   
        iterM = MembershipFinder.findMembershipsNoPrivsNoSession(ns, m, f).iterator();
        privs.addAll( this._getPrivs(s, subj, m, p, iterM) );
        iterA = MembershipFinder.findMembershipsNoPrivsNoSession(ns, all, f).iterator();
        privs.addAll( this._getPrivs(s, subj, all, p, iterA) );
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
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException, 
            InsufficientPrivilegeException,
            SchemaException
  {
    try {
      GrouperSessionValidator.validate(s);
      Field f = this._getField(priv);
      StemValidator.canWriteField(ns, s.getSubject(), f, FieldType.NAMING);
      Membership.addImmediateMembership(s, ns, subj, f);
    }
    catch (MemberAddException eMA) {
      throw new GrantPrivilegeException(eMA.getMessage(), eMA);
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
    GrouperSessionValidator.validate(s);
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      rv = m.isMember(ns, this._getField(priv));
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
    GrouperSessionValidator.validate(s);
    Field f = this._getField(priv);
    StemValidator.canWriteField(ns, s.getSubject(), f, FieldType.NAMING);

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
      throw new RevokePrivilegeException(e.getMessage(), e);
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
      StemValidator.canWriteField(ns, s.getSubject(), f, FieldType.NAMING);
      MemberOf  mof     = Membership.delImmediateMembership(s, ns, subj, f);
      Set       saves   = mof.getSaves();
      Set       deletes = mof.getDeletes();

      // TODO Should this be in _Stem_?  
      ns.setModified();
      saves.add(ns);

      // And then commit changes to registry
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (HibernateException eH) {
      throw new RevokePrivilegeException(eH);
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException(eMD);
    }
  } // public void revokePriv(s, ns, subj, priv)

  
  // PRIVATE INSTANCE METHODS //
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
    GrouperSession s, Subject subj, Member m, Privilege p, Iterator iter
  )
    throws  SchemaException
  {
    Set         privs = new LinkedHashSet();
    Membership  ms;
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
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
        ErrorLog.error(GrouperNamingAdapter.class, eSNF.getMessage());
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
      catch (StemNotFoundException eNSNF) {
        ErrorLog.error(GrouperNamingAdapter.class, eNSNF.getMessage());
      }
    }
    return privs;
  } // private Set _get(s, subj, m, p, iter)

}

