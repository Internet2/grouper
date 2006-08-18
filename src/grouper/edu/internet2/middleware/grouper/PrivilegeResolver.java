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
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.ehcache.*;

/** 
 * Privilege resolution class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeResolver.java,v 1.60 2006-08-18 15:34:09 blair Exp $
 */
public class PrivilegeResolver {

  // PRIVATE CLASS CONSTANTS //
  // TODO Move to *E*
  private static final String ERR_RPC   = "unable to reset privilege caches: ";
  // TODO Move to *M*
  private static final String MSG_RPC   = "reset privilege cache: ";


  // PRIVATE CLASS VARIABLES //
  private static  PrivilegeCache    ac        = getAccessCache();
  private static  AccessAdapter     access    = getAccess();
  private static  NamingAdapter     naming    = getNaming();
  private static  PrivilegeCache    nc        = getNamingCache();
  private static  boolean           use_wheel = Boolean.valueOf(
    GrouperConfig.getProperty(GrouperConfig.GWU)
  );


  // CONSTRUCTORS //
  private PrivilegeResolver() {
    // nothing
  } // private PrivilegeResolver()


  // PUBLIC CLASS METHODS //

  /**
   * Remove all entries from the access and naming privilege caches.
   * <pre class="eg">
   * PrivilegeResolver.resetPrivilegeCaches();
   * </pre>
   * @throws  GrouperRuntimeException 
   */
  public static void resetPrivilegeCaches() 
    throws  GrouperRuntimeException
  {
    try {
      if (ac != null) {
        ac.removeAll();
        DebugLog.info(PrivilegeResolver.class, MSG_RPC + PrivilegeCache.ACCESS);
      }
      if (nc != null) {
        nc.removeAll();
        DebugLog.info(PrivilegeResolver.class, MSG_RPC + PrivilegeCache.NAMING);
      }
    }
    catch (Exception e) {
      String msg = ERR_RPC + e.getMessage();
      ErrorLog.fatal(PrivilegeResolver.class, msg);
      throw new GrouperRuntimeException(msg, e);
    }
  } // public static void resetPrivilegeCaches()


  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static boolean canADMIN(GrouperSession s, Group g, Subject subj) {
    return PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.ADMIN);
  } // protected static boolean canADMIN(s, g, subj)

  // @since   1.1.0
  protected static boolean canCREATE(GrouperSession s, Stem ns, Subject subj) {
    return PrivilegeResolver.hasPriv(s, ns, subj, NamingPrivilege.CREATE);
  } // protected static boolean canCREATE(s, ns, subj)

  // @since   1.1.0
  protected static boolean canOPTIN(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.OPTIN)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false;
  } // protected static boolean canOPTIN(s, g, subj)

  // @since   1.1.0
  protected static boolean canOPTOUT(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean canOPTOUT(s, g, subj)

  // @since   1.1.0
  protected static void canPrivDispatch(
    GrouperSession s, Owner o, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 
    if      (priv.equals(AccessPrivilege.ADMIN))  {
      rv = canADMIN(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_ADMIN;
      }
    }
    else if (priv.equals(NamingPrivilege.CREATE)) { 
      rv = canCREATE(s, (Stem) o, subj);
      if (!rv) {
        msg = E.CANNOT_CREATE;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      rv = canOPTIN(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_OPTIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      rv = canOPTOUT(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_OPTOUT;
      }
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      rv = canREAD(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_READ;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM))   {
      rv = canSTEM(s, (Stem) o, subj);
      if (!rv) {
        msg = E.CANNOT_STEM;
      }
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      rv = canVIEW(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_VIEW;
      }
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      rv = canUPDATE(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_UPDATE;
      }
    }
    else if (priv.equals(AccessPrivilege.SYSTEM))  {
      msg = E.SYSTEM_MAINTAINED + priv;
    }
    else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } // protected static void canPrivDispatch(s, o, subj, priv)

  // @since   1.1.0
  protected static boolean canREAD(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.READ)  
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
   }// protected static boolean canREAD(s, g, subj)

  // @since   1.1.0
  protected static boolean canSTEM(GrouperSession s, Stem ns, Subject subj) {
    return PrivilegeResolver.hasPriv(s, ns, subj, NamingPrivilege.STEM);
  } // protected static boolean canSTEM(s, ns, subj)

  // @since   1.1.0
  protected static boolean canUPDATE(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.UPDATE)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean canUPDATE(s, g, subj)

  // @since   1.1.0
  protected static Set canVIEW(GrouperSession s, Set candidates) {
    Set             groups  = new LinkedHashSet();
    Group           g;
    Iterator        iter    = candidates.iterator();
    while (iter.hasNext()) {
      g = (Group) iter.next();
      g.setSession(s);
      // Can we view the group
      if (canVIEW(s, g, s.getSubject())) {
        groups.add(g);
      }
    }
    return groups;
  } // protected static Set canVIEW(s, candidates)

  // @since   1.1.0
  protected static boolean canVIEW(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.VIEW)  
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.READ)  
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.UPDATE)
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.OPTIN) 
      ||
      PrivilegeResolver.hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean canVIEW(s, g, subj)

  // If the subject being added is a group, verify that we can VIEW it
  // @since   1.1.0
  protected static Member canViewSubject(GrouperSession s, Subject subj)
    throws  ModelException
  {
    try {
      Member  m = MemberFinder.findBySubject(s, subj);
      if (m.getSubjectType().equals(SubjectTypeEnum.valueOf("group"))) {
        Subject who   = s.getSubject();
        Group   what  = m.toGroup();
        if (!canVIEW(s, what, who)) {
          throw new InsufficientPrivilegeException(E.CANNOT_VIEW);
        }
      }
      return m;
    }
    catch (Exception e) {
      throw new ModelException(e.getMessage(), e);
    }
  } // protected static Member canViewSubject(s, subj)

  // @since   1.1.0
  protected static AccessAdapter getAccess() {
    if (access == null) {
      access = (AccessAdapter) U.realizeInterface(
        GrouperConfig.getProperty(GrouperConfig.PAI)
      );
    }
    return access;
  } // protected static AccessAdapter getAccess()

  // @since   1.1.0
  protected static PrivilegeCache getAccessCache() {
    if (ac == null) {
      ac = PrivilegeCache.getCache(PrivilegeCache.ACCESS);
    }
    return ac;
  } // protected static PrivilegeCache getAccessCache()

  // @since   1.1.0 
  protected static Set getGroupsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    return getAccess().getGroupsWhereSubjectHasPriv(s, subj, priv);
  } // protected static Set getGroupsWhereSubjectHasPriv(s, subj, priv)

  // @since   1.1.0
  protected static NamingAdapter getNaming() {
    if (naming == null) {
      naming = (NamingAdapter) U.realizeInterface(
        GrouperConfig.getProperty(GrouperConfig.PNI)
      );
    }
    return naming;
  } // protected static AccessAdapter getNaming()

  // @since   1.1.0
  protected static PrivilegeCache getNamingCache() {
    if (nc == null) {
      nc = PrivilegeCache.getCache(PrivilegeCache.NAMING);
    }
    return nc;
  } // protected static PrivilegeCache getNamingCache()

  // @since   1.1.0
  protected static Set getPrivs(
    GrouperSession s, Group g, Subject subj
  )
  {
    return getAccess().getPrivs(s, g, subj);
  } // protected static Set getPrivs(s, g, subj)

  // @since   1.1.0
  protected static Set getPrivs(
    GrouperSession s, Stem ns, Subject subj
  )
  {
    return getNaming().getPrivs(s, ns, subj);
  } // protected static Set getPrivs(s, ns, subj)

  // @since   1.1.0
  protected static Set getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    return getNaming().getStemsWhereSubjectHasPriv(s, subj, priv);
  } // protected static Set getStemsWhereSubjectHasPriv(s, subj, priv)

  // @since   1.1.0
  protected static Set getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    return getAccess().getSubjectsWithPriv(s, g, priv);
  } // protected static Set getSubjectsWithPriv(s, g, priv)

  // @since   1.1.0
  protected static Set getSubjectsWithPriv(GrouperSession s, Stem ns, Privilege priv) 
    throws SchemaException
  {
    return getNaming().getSubjectsWithPriv(s, ns, priv);
  } // protected static Set getSubjectsWithPriv(s, ns, priv)

  // @since   1.1.0
  protected static void grantPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    getAccess().grantPriv(s, g, subj, priv);
    // FIXME
    try {
      getAccessCache().removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected static void grantPriv(s, g, subj, priv)

  // @since   1.1.0
  protected static void grantPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    getNaming().grantPriv(s, ns, subj, priv);
    // FIXME
    try {
      getNamingCache().removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected static void grantPriv(s, ns, subj, priv)

  // FIXME    Refactor once I rework caching
  // @since   1.1.0
  protected static boolean hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    boolean rv = false;
    Element el = getAccessCache().get(g, subj, priv);
    if (el != null) {
      // Result cached.  Is it true?
      if ( el.getValue().equals(GrouperConfig.BT) ) {
        rv = true;
      }
    }
    else if (isRoot(subj)) {
      rv = true;  
    }
    else {
      try {
        rv = getAccess().hasPriv(s, g, subj, priv);
        if (rv == false) {
          rv = _isAll(s, g, priv);
        } 
      }
      catch (SchemaException eS) {
        rv = false;
        return rv;
      }
    }
    if (el == null) {
      ac.put(g, subj, priv, rv);
    }
    return rv;
  } // protected static boolean hasPriv(s, g, subj, priv)


  // FIXME    Refactor once I rework caching
  // @since   1.1.0
  protected static boolean hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    boolean rv = false;
    // Is result cached?
    Element el = getNamingCache().get(ns, subj, priv);
    if      (el != null) {
      // Result cached.  Is it true?
      if ( el.getValue().equals(GrouperConfig.BT) ) {
        rv = true;
      }
    }
    else if (isRoot(subj)) {
      // Is the subject root?
      rv = true;  
    }
    else {
      // Otherwise, check and see if GrouperAll ihas the privilege
      try {
        rv = getNaming().hasPriv(s, ns, subj, priv);
        if (rv == false) {
          rv = _isAll(s, ns, priv);
        } 
      }
      catch (SchemaException eS) {
        rv = false;
        return rv;
      }
    }
    if (el == null) {
      getNamingCache().put(ns, subj, priv, rv);
    }
    return rv;
  } // protected static boolean hasPriv(s, ns, subj, priv)

  // @since   1.1.0
  protected static boolean isRoot(Subject subj) {
    boolean rv = false;
    // First check to see if this is GrouperSystem
    // FIXME Refactor
    if      ( SubjectHelper.eq(subj, SubjectFinder.findRootSubject()) ) {
      rv = true;
    }  
    else if (use_wheel) {
      rv = _isWheel(subj);
    }
    return rv;
  } // protected static boolean isRoot(subj)

  // @since   1.1.0
  protected static void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    getAccess().revokePriv(s, g, priv);
    // FIXME
    try {
      getAccessCache().removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected static void revokePriv(s, g, priv)

  // @since   1.1.0
  protected static void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    getNaming().revokePriv(s, ns, priv);
    // FIXME
    try {
      getNamingCache().removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected static void revokePriv(s, ns, priv)

  // @since   1.1.0
  protected static void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    getAccess().revokePriv(s, g, subj, priv);
    // FIXME
    try {
      getAccessCache().removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected static void revokePriv(s, g, subj, priv)

  // @since   1.1.0
  protected static void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    getNaming().revokePriv(s, ns, subj, priv);
    // FIXME
    try {
      getNamingCache().removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected static void revokePriv(s, ns, subj, priv)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static boolean _isAll(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    return getAccess().hasPriv(s, g, SubjectFinder.findAllSubject(), priv);
  } // private static boolean _isAll(s, g, priv);

  // @since   1.1.0
  private static boolean _isAll(GrouperSession s, Stem ns, Privilege priv) 
    throws  SchemaException
  {
    return getNaming().hasPriv(s, ns, SubjectFinder.findAllSubject(), priv);
  } // private static boolean _isAll(s, ns, priv);

  // @since   1.1.0
  private static boolean _isWheel(Subject subj) {
    boolean       rv  = false;
    if (use_wheel) {
      // TODO This has to be a performance killer
      String name = GrouperConfig.getProperty(GrouperConfig.GWG);
      try {
        Group wheel = GroupFinder.findByName(GrouperSession.startTransient(), name);
        rv          = wheel.hasMember(subj);
      }
      catch (GroupNotFoundException eGNF) {
        // Group not found.  Oh well.
        // TODO The problem is that the test suite deletes it.  
        //      But, now that the test suite has evolved, I should be able to
        //      more properly test this.
        ErrorLog.error(PrivilegeResolver.class, E.NO_WHEEL_GROUP + name);
        use_wheel = false;
      }
    } 
    return rv;
  } // private static boolean _isWheel(subj)

} // public class PrivilegeResolver

