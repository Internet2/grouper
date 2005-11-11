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

import  java.util.*;


/** 
 * Grouper Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Group} class.
 * </p>
 * @author  blair christensen.
 * @version $Id: AccessPrivilege.java,v 1.2 2005-11-11 18:32:06 blair Exp $
 */
public interface AccessPrivilege {

  // Public Instance Methods

  /**
   * Get all members with this privilege on this group.
   * <pre class="eg">
   * try {
   *   Set admins = ap.getPriv(s, g, Privilege.ADMIN);
   * }
   * catch (PrivilegeNotFoundException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   g     Get privileges on this group.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Member} objects.
   * @throws  PrivilegeNotFoundException
   */
  Set getPriv(GrouperSession s, Group g, String priv) 
    throws PrivilegeNotFoundException;

  /**
   * Get all groups where this member has this privilege.
   * <pre class="eg">
   * try {
   *   Set isAdmin = ap.getPriv(s, m, Privilege.ADMIN);
   * }
   * catch (PrivilegeNotFoundException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   m     Get privileges for this member.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Group} objects.
   * @throws  PrivilegeNotFoundException
   */
  Set getPriv(GrouperSession s, Member m, String priv) 
    throws PrivilegeNotFoundException;

  /**
   * Get all privileges held by this member on this group.
   * <pre class="eg">
   * Set privs = ap.getPrivs(s, g, m);
   * </pre>
   * @param   s   Get privileges within this session context.
   * @param   g   Get privileges on this group.
   * @param   m   Get privileges for this member.
   * @return  Set of privileges.
   */
  Set getPrivs(GrouperSession s, Group g, Member m);

  /**
   * Grant the privilege to the member on this group.
   * <pre class="eg">
   * try {
   *   ap.grantPriv(s, g, m, Privilege.ADMIN);
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
   * @param   g     Grant privilege on this group.
   * @param   m     Grant privilege to this member.
   * @param   priv  Grant this privilege.   
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  PrivilegeNotFoundException
   */
  void grantPriv(GrouperSession s, Group g, Member m, String priv)
    throws GrantPrivilegeException, 
           InsufficientPrivilegeException, 
           PrivilegeNotFoundException;

  /**
   * Check whether the member has this privilege on this group.
   * <pre class="eg">
   * try {
   *   ap.hasPriv(s, g, m, Privilege.ADMIN);
   * }
   * catch (PrivilegeNotFoundException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   g     Check privilege on this group.
   * @param   m     Check privilege for this member.
   * @param   priv  Check this privilege.   
   * @throws  PrivilegeNotFoundException
   */
  boolean hasPriv(GrouperSession s, Group g, Member m, String priv)
    throws PrivilegeNotFoundException;

  /**
   * Revoke this privilege from everyone on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, Privilege.ADMIN);
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
   * @param   g     Revoke privilege on this group.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  PrivilegeNotFoundException
   * @throws  RevokePrivilegeException
   */
  void revokePriv(GrouperSession s, Group g, String priv)
    throws InsufficientPrivilegeException, 
           PrivilegeNotFoundException, 
           RevokePrivilegeException;

  /**
   * Revoke the privilege from the member on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, m, Privilege.ADMIN);
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
   * @param   g     Revoke privilege on this group.
   * @param   m     Revoke privilege from this member.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  PrivilegeNotFoundException
   * @throws  RevokePrivilegeException
   */
  void revokePriv(GrouperSession s, Group g, Member m, String priv)
    throws InsufficientPrivilegeException, 
           PrivilegeNotFoundException, 
           RevokePrivilegeException;

}

