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
 * Grouper Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link Group} class.
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperAccessAdapter.java,v 1.28 2005-12-12 16:53:57 blair Exp $
 */
public class GrouperAccessAdapter implements AccessAdapter {

  // Private Class Constants
  private static final String ERR_GP    = "unable to grant priv: ";
  private static final String ERR_MGNF  = "membership group not found: ";
  private static final String ERR_MMNF  = "membership member not found: ";
  private static final String ERR_RP    = "unable to revoke priv: ";
  private static final Log    LOG       = LogFactory.getLog(GrouperAccessAdapter.class);
  private static final String MSG_GP    = "grant priv ";
  private static final String MSG_RP    = "revoke priv ";


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
        s, m, (Field) FieldFinder.find( (String) priv2list.get(priv) )
      ).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        ms.setSession(s);
        groups.add( ms.getGroup() );
      }
    }
    catch (GroupNotFoundException eGNF) {
      LOG.error(ERR_MGNF + eGNF.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      LOG.error(ERR_MMNF + eMNF.getMessage());
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
      Member    m   = MemberFinder.findBySubject(s, subj);
      Member    all = MemberFinder.findAllMember();     
      Iterator  iterP = Privilege.getAccessPrivs().iterator();
      while (iterP.hasNext()) {
        Privilege p = (Privilege) iterP.next();
        Field     f = this._getField(p);   
        Iterator  iterM = MembershipFinder.findMemberships(g.getUuid(), m, f).iterator();
        privs.addAll( this._getPrivs(s, g, subj, m, p, iterM) );
        Iterator  iterA = MembershipFinder.findMemberships(g.getUuid(), all, f).iterator();
        privs.addAll( this._getPrivs(s, g, subj, all, p, iterA) );
      }
    }
    catch (MemberNotFoundException eMNF) {
      GrouperLog.error(LOG, s, ERR_MMNF + eMNF.getMessage());
    }
    catch (SchemaException eS) {
      // Well, this is strange.  Ignore.
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
    GrouperSession.validate(s);
    Field   f   = this._getField(priv);
    String  msg = MSG_GP + "'" + f + "' " + SubjectHelper.getPretty(subj);
    String  err = ERR_GP;
    GrouperLog.debug(LOG, s, msg);
    try {
      PrivilegeResolver.getInstance().canWriteField(
        s, g, s.getSubject(), f, FieldType.ACCESS
      );
      GrouperLog.debug(LOG, s, msg + " can grant priv");
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, msg + ": " + eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }
    try {
      Membership.addImmediateMembership(s, g, subj, f);
    }
    catch (MemberAddException eMA) {
      GrouperLog.debug(LOG, s, msg + ": " + eMA.getMessage());
      throw new GrantPrivilegeException(eMA.getMessage());
    }
    GrouperLog.debug(LOG, s, msg + ": granted");
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
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      rv = m.isMember(g, this._getField(priv));
    }
    catch (MemberNotFoundException eMNF) {
      LOG.error(ERR_MMNF + eMNF.getMessage());
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
    GrouperSession.validate(s);
    String  msg = "revokePriv '" + priv + "'";
    Field   f   = this._getField(priv);
    GrouperLog.debug(LOG, s, msg);

    try {
      PrivilegeResolver.getInstance().canWriteField(
        s, g, s.getSubject(), f, FieldType.ACCESS
      );
      GrouperLog.debug(LOG, s, msg + " can revoke priv");
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, msg + ": " + eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }

    try {
      // The objects that will need updating and deleting
      Set     saves   = new LinkedHashSet();
      Set     deletes = new LinkedHashSet();

      // Update stem modify time
      g.setModified();
      saves.add(g);

      try {
        // Find privileges that need to be revoked
        GrouperLog.debug(LOG, s, msg + " find privs to revoke");
        deletes = Membership.deleteAllField(s, g, f);
        GrouperLog.debug(
          LOG, s, msg + " found privs to revoke: " + deletes.size()
        );
      }
      catch (Exception e) {
        String err = msg + " " + e.getMessage();
        GrouperLog.debug(LOG, s, err);
        throw new RevokePrivilegeException(err);
      }

      // And then update the registry
      GrouperLog.debug(LOG, s, msg + " committing changes to registry");
      HibernateHelper.saveAndDelete(saves, deletes);
      GrouperLog.debug(LOG, s, msg + " revoked privs: " + deletes.size());
    }
    catch (HibernateException eH) {
      String err = msg + ": " + eH.getMessage();
      GrouperLog.debug(LOG, s, err);
      throw new RevokePrivilegeException(err);
    }
    GrouperLog.debug(LOG, s, msg + " revoked");
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
    GrouperSession.validate(s);
    Field   f   = this._getField(priv);
    String  msg = MSG_GP + "'" + f + "' " + SubjectHelper.getPretty(subj);
    String  err = ERR_RP;
    GrouperLog.debug(LOG, s, msg);
    try {
      PrivilegeResolver.getInstance().canWriteField(
        s, g, s.getSubject(), f, FieldType.ACCESS
      );
      GrouperLog.debug(LOG, s, msg + " can revoke priv");
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, msg + ": " + eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }
    try {
      Membership.delImmediateMembership(s, g, subj, f);
    }
    catch (MemberDeleteException eMD) {
      GrouperLog.debug(LOG, s, msg + ": " + eMD.getMessage());
      throw new RevokePrivilegeException(eMD.getMessage());
    }
    GrouperLog.debug(LOG, s, msg + ": revoked");
  } // public void revokePriv(s, g, subj, priv)


  // Private Instance Methods
  private Field _getField(Privilege priv) 
    throws  SchemaException
  {
    return FieldFinder.find( (String) priv2list.get(priv) );
  } // private Field _getField(priv)

  // Return appropriate _AccessPrivilege_ objects 
  private Set _getPrivs(
    GrouperSession s, Group g, Subject subj, Member m, Privilege p, Iterator iter
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
          new AccessPrivilege(
            ms.getGroup() , subj              , owner,
            p             , s.getAccessClass(), revoke
          )
        );
      }
      catch (GroupNotFoundException eGNF) {
        // this is also bloody odd
      }
    }
    return privs;
  } // private Set _get(s, g, subj, m, p, iter)

}

