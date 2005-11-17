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
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingAdapter.java,v 1.9 2005-11-17 04:10:18 blair Exp $
 */
public class GrouperNamingAdapter implements NamingAdapter {

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
   * catch (PrivilegeNotFoundException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   ns    Get privileges on this stem.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Subject} objects.
   * @throws  PrivilegeNotFoundException
   */
  public Set getSubjectsWithPriv(GrouperSession s, Stem ns, String priv) 
    throws PrivilegeNotFoundException 
  {
    throw new RuntimeException("not implemented");
  } // public Set getSubjectsWithPriv(s, ns, priv)

  /**
   * Get all stems where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isStemmer = np.getStemsWhereSubjectHasPriv(
   *     s, subj, NamingPrivilege.STEM
   *   );
   * }
   * catch (PrivilegeNotFoundException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Stem} objects.
   * @throws  PrivilegeNotFoundException
   */
  public Set getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, String priv
  ) 
    throws PrivilegeNotFoundException 
  {
    throw new RuntimeException("not implemented");
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
   * @return  Set of {@link Privilege} objects.
   */
  public Set getPrivs(GrouperSession s, Stem ns, Subject subj) {
    Set privs = new LinkedHashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      Iterator  iter  = FieldFinder.findType(FieldType.NAMING).iterator();
      while (iter.hasNext()) {
        Field f = (Field) iter.next();
        if (
          MembershipFinder.findMemberships(ns.getUuid(), m, f).size() > 0
        )
        {
          privs.add(f);
        }
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(
        "could not convert subject to member: " + eMNF.getMessage()
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
   */
  public void grantPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws GrantPrivilegeException, 
           InsufficientPrivilegeException
  {
    try {
      // Convert subject to a member
      Member  m       = MemberFinder.findBySubject(s, subj);

      // The objects that will need saving
      Set     objects = new HashSet();

      // Update group modify time
      ns.setModified();
      objects.add(ns);

      // Create the immediate membership
      objects.add( 
        Membership.addMembership(
          s, ns, m, FieldFinder.getField( (String) priv2list.get(priv) )
        )
      );

      // Find effective memberships
      objects.addAll( MemberOf.doMemberOf(s, ns, m) );

      // And then save group and memberships
      HibernateHelper.save(objects);
    }
    catch (GroupNotFoundException eGNF) {
      throw new GrantPrivilegeException(
        "could not grant privilege: " + eGNF.getMessage()
      );
    }
    catch (HibernateException eH) {
      throw new GrantPrivilegeException(
        "could not grant privilege: " + eH.getMessage()
      );
    }
    catch (MemberAddException eMA) {
      throw new GrantPrivilegeException(
        "could not grant privilege: " + eMA.getMessage()
      );
    }
    catch (MemberNotFoundException eMNF) {
      throw new GrantPrivilegeException(
        "could not grant privilege: " + eMNF.getMessage()
      );
    }
    catch (SchemaException eS) {
      throw new GrantPrivilegeException(
        "could not grant privilege: " + eS.getMessage()
      );
    }
  } // public void grantPriv(s, ns, subj, priv)

  /**
   * Check whether the subject has this privilege on this stem.
   * <pre class="eg">
   * try {
   *   np.hasPriv(s, ns, subj, NamingPrivilege.STEM);
   * }
   * catch (PrivilegeNotFoundException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   ns    Check privilege on this stem.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @throws  PrivilegeNotFoundException
   */
  public boolean hasPriv(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws PrivilegeNotFoundException 
  {
    try {
      Field   f   = FieldFinder.getField( (String) priv2list.get(priv));
      Member  m   = MemberFinder.findBySubject(s, subj);
      if (MembershipFinder.findMemberships(ns.getUuid(), m, f).size() > 0) {
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
      throw new PrivilegeNotFoundException("invalid privilege: " + priv);
    }
  } // public boolean hasPriv(s, ns, subj, priv) 

  /**
   * Revoke this privilege from everyone on this stem.
   * <pre class="eg">
   * try {
   *   np.revokePriv(s, ns, NamingPrivilege.STEM);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (PrivilegeNotFoundException e1) {
   *   // Invalid privilege
   * }
   * catch (RevokePrivilegeException e2) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   ns    Revoke privilege on this stem.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  PrivilegeNotFoundException
   * @throws  RevokePrivilegeException
   */
  public void revokePriv(GrouperSession s, Stem ns, String priv)
    throws InsufficientPrivilegeException, 
           PrivilegeNotFoundException, 
           RevokePrivilegeException 
  {
    throw new RuntimeException("not implemented");
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
    try {
      // Convert subject to a member
      Member  m       = MemberFinder.findBySubject(s, subj);

      // The objects that will need updating and deleting
      Set     saves   = new HashSet();
      Set     deletes = new HashSet();

      // Update stem modify time
      ns.setModified();
      saves.add(ns);

      // Find the immediate privilege that is to be deleted
      deletes.add(
        MembershipFinder.getImmediateMembership(
          s, ns.getUuid(), m, FieldFinder.getField( (String) priv2list.get(priv) )
        )
      );

      // As many of the privileges are likely to be transient, we
      // need to retrieve the persistent version of each before
      // passing it along to be deleted by HibernateHelper.  
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = MemberOf.doMemberOf(s, ns, m).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        deletes.add( 
          MembershipFinder.getEffectiveMembership(
            ms.getOwner_id(), ms.getMember_id(), 
            ms.getList(), ms.getVia_id(), ms.getDepth()
          )
        );
      }
      hs.close();

      // And then update the registry
      //HibernateHelper.delete(objects);
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
  } // public void revokePriv(s, ns, subj, priv)

}

