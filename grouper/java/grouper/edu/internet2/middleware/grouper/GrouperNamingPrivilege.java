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


/** 
 * Default implementation of the Grouper {@link NamingPrivilege}
 * interface.
 * <p>
 * This implementation uses the Groups Registry and custom list types
 * to manage naming privileges.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNamingPrivilege.java,v 1.2 2005-11-14 16:47:07 blair Exp $
 */
public class GrouperNamingPrivilege implements NamingPrivilege {

  // Public Instance Methods

  /**
   * Get all subjects with this privilege on this stem.
   * <pre class="eg">
   * try {
   *   Set stemmers = np.getSubjectsWithPriv(s, ns, Privilege.STEM);
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
   *     s, subj, Privilege.STEM
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
  public Set getPriv(GrouperSession s, Subject subj, String priv) 
    throws PrivilegeNotFoundException 
  {
    throw new RuntimeException("not implemented");
  } // public Set getPriv(s, subj, priv)

  /**
   * Get all privileges held by this subject on this stem.
   * <pre class="eg">
   * Set privs = np.getPrivs(s, ns, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   ns    Get privileges on this stem.
   * @param   subj  Get privileges for this subject.
   * @return  Set of privileges.
   */
  public Set getPrivs(GrouperSession s, Stem ns, Subject subj) {
    throw new RuntimeException("not implemented");
  } // public Set getPrivs(s, ns, subj)

  /**
   * Grant the privilege to the subject on this stem.
   * <pre class="eg">
   * try {
   *   np.grantPriv(s, ns, subj, Privilege.STEM);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Unable to grant the privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to grant the privilege
   * }
   * catch (PrivilegeNotFoundException e2) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Grant privilege in this session context.
   * @param   ns    Grant privilege on this stem.
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.   
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  PrivilegeNotFoundException
   */
  public void grantPriv(GrouperSession s, Stem ns, Subject subj, String priv)
    throws GrantPrivilegeException, 
           InsufficientPrivilegeException, 
           PrivilegeNotFoundException 
  {
    throw new RuntimeException("not implemented");
  } // public void grantPriv(s, ns, subj, priv)

  /**
   * Check whether the subject has this privilege on this stem.
   * <pre class="eg">
   * try {
   *   np.hasPriv(s, ns, subj, Privilege.STEM);
   * }
   * catch (PrivilegeNotFoundException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   ns    Check privilege on this stem.
   * @param   subj     Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @throws  PrivilegeNotFoundException
   */
  public boolean hasPriv(GrouperSession s, Stem ns, Subject subj, String priv)
    throws PrivilegeNotFoundException 
  {
    throw new RuntimeException("not implemented");
  } // public boolean hasPriv(s, ns, subj, priv) 

  /**
   * Revoke this privilege from everyone on this stem.
   * <pre class="eg">
   * try {
   *   np.revokePriv(s, ns, Privilege.STEM);
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
   *   np.revokePriv(s, ns, subj, Privilege.STEM);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to grant the privilege
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
   * @param   subj  Revoke privilege from this member.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  PrivilegeNotFoundException
   * @throws  RevokePrivilegeException
   */
  public void revokePriv(GrouperSession s, Stem ns, Subject subj, String priv)
    throws InsufficientPrivilegeException, 
           PrivilegeNotFoundException, 
           RevokePrivilegeException 
  {
    throw new RuntimeException("not implemented");
  } // public void revokePriv(s, ns, subj, priv)

}

