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
 * Grouper Naming Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Stem} class.
 * </p>
 * @author  blair christensen.
 * @version $Id: NamingPrivilege.java,v 1.3 2005-11-14 16:25:05 blair Exp $
 */
public interface NamingPrivilege {

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
   * @return  Set of {@link Subjecct} objects.
   * @throws  PrivilegeNotFoundException
   */
  Set getSubjectsWithPriv(GrouperSession s, Stem ns, String priv) 
    throws PrivilegeNotFoundException;

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
  Set getPriv(GrouperSession s, Subject subj, String priv) 
    throws PrivilegeNotFoundException;

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
  Set getPrivs(GrouperSession s, Stem ns, Subject subj);

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
  void grantPriv(GrouperSession s, Stem ns, Subject subj, String priv)
    throws GrantPrivilegeException, 
           InsufficientPrivilegeException, 
           PrivilegeNotFoundException;

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
  boolean hasPriv(GrouperSession s, Stem ns, Subject subj, String priv)
    throws PrivilegeNotFoundException;

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
  void revokePriv(GrouperSession s, Stem ns, String priv)
    throws InsufficientPrivilegeException, 
           PrivilegeNotFoundException, 
           RevokePrivilegeException;

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
  void revokePriv(GrouperSession s, Stem ns, Subject subj, String priv)
    throws InsufficientPrivilegeException, 
           PrivilegeNotFoundException, 
           RevokePrivilegeException;

}

