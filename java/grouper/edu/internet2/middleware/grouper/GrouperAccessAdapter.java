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
 * Grouper Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Group} class.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperAccessAdapter.java,v 1.8 2005-11-17 15:40:59 blair Exp $
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
    return MembershipFinder.findSubjects(
      s, g.getUuid(), 
      (Field) FieldFinder.getField( (String) priv2list.get(priv) )
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
   * catch (PrivilegeNotFoundException e0) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Group} objects.
   * @throws  PrivilegeNotFoundException
   */
  public Set getGroupsWhereSubjectHashPriv(GrouperSession s, Subject subj, String priv) 
    throws PrivilegeNotFoundException
  {
    throw new RuntimeException("not implemented");
  } // public Set getGroupsWhereSubjectHashPriv(s, subj, priv)

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
    Set privs = new LinkedHashSet();
    try {
      Member    m     = MemberFinder.findBySubject(s, subj);
      Iterator  iter  = FieldFinder.findType(FieldType.ACCESS).iterator();
      while (iter.hasNext()) {
        Field f = (Field) iter.next();
        if (
          MembershipFinder.findMemberships(g.getUuid(), m, f).size() > 0
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
    try {
      g.addMember(
        subj, FieldFinder.getField( (String) priv2list.get(priv) ) 
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
   * catch (PrivilegeNotFoundException e) {
   *   // Invalid privilege
   * }
   * </pre>
   * @param   s     Check privilege in this session context.
   * @param   g     Check privilege on this group.
   * @param   subj  Check privilege for this subject.
   * @param   priv  Check this privilege.   
   * @throws  PrivilegeNotFoundException
   */
  public boolean hasPriv(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  PrivilegeNotFoundException
  {
    try {
      Field   f   = FieldFinder.getField( (String) priv2list.get(priv));
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
      throw new PrivilegeNotFoundException("invalid privilege: " + priv);
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
    throw new RuntimeException("not implemented");
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
    try {
      g.deleteMember(
        subj, FieldFinder.getField( (String) priv2list.get(priv) ) 
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

}

