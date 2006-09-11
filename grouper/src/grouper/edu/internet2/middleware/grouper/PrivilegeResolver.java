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

/** 
 * Privilege resolution class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeResolver.java,v 1.69 2006-09-11 14:00:33 blair Exp $
 */
 class PrivilegeResolver {

  // PRIVATE CLASS VARIABLES //
  private static  AccessAdapter     access    = getAccess();
  private static  NamingAdapter     naming    = getNaming();


  // CONSTRUCTORS //
  protected PrivilegeResolver() {
    super();
  } // protected PrivilegeResolver()



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
      rv = canSTEM((Stem) o, subj);
      if (!rv) {
        msg = E.CANNOT_STEM;
      }
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      rv = canVIEW((Group) o, subj);
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
  protected static boolean canSTEM(Stem ns, Subject subj) {
    return PrivilegeResolver.hasPriv(ns.getSession(), ns, subj, NamingPrivilege.STEM);
  } // protected static boolean canSTEM(ns, subj)

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
  protected static boolean canVIEW(Group g, Subject subj) {
    GrouperSession s = g.getSession();
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
  } // protected static boolean canVIEW(g, subj)

  // @since   1.1.0
  protected static Set canViewGroups(GrouperSession s, Set candidates) {
    Set             groups  = new LinkedHashSet();
    Group           g;
    Iterator        iter    = candidates.iterator();
    while (iter.hasNext()) {
      g = (Group) iter.next();
      g.setSession(s);
      // Can we view the group
      if (canVIEW(g, s.getSubject())) {
        groups.add(g);
      }
    }
    return groups;
  } // protected static Set canViewGroups(s, candidates)

  // @since   1.1.0
  protected static Set canViewMemberships(GrouperSession s, Collection c) {
    GrouperSessionValidator.validate(s);
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    iter    = c.iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      ms.setSession(s);
      try {
        if (FieldType.ACCESS.equals(ms.getList().getType())) {
          canPrivDispatch(
            s, ms.getGroup(), s.getSubject(), ms.getList().getReadPriv()
          );
        }
        mships.add(ms);
      }
      catch (Exception e) {
        // @exception GroupNotFoundException
        // @exception InsufficientPrivilegeException
        // @exception SchemaException
        // @exception StemNotFoundException
        // ignore
      }
    }
    return mships;
  } // protected static Set canViewMemberships(s, c)
  
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
        if (!canVIEW(what, who)) {
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
    s.getAccessCache().grantPriv(g, subj, priv);
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
    s.getNamingCache().grantPriv(ns, subj, priv);
  } // protected static void grantPriv(s, ns, subj, priv)

  // @since   1.1.0
  protected static boolean hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    GrouperSessionValidator.validate(s);
    boolean rv = false;
    PrivilegeCacheElement el = s.getAccessCache().get(g, subj, priv);
    if (el.getIsCached()) {
      rv = el.getHasPriv(); // use cached result
    }
    else {
      try { // TODO Eliminate the try/catch
        if (
             RootPrivilegeResolver.isRoot(s, subj)
          || getAccess().hasPriv(s, g, subj, priv)
          || _isAll(s, g, priv)
        )
        {
          rv = true;
        }
      }
      catch (SchemaException eS) {
        rv = false; 
      }
    }
    s.getAccessCache().put(g, subj, priv, rv);
    return rv;
  } // protected static boolean hasPriv(s, g, subj, priv)

  // @since   1.1.0
  protected static boolean hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    boolean rv = false;
    PrivilegeCacheElement el = s.getNamingCache().get(ns, subj, priv);
    if (el.getIsCached()) {
      rv = el.getHasPriv(); // use cached result
    }
    else {
      try { // TODO Eliminate the try/catch
        if (
             RootPrivilegeResolver.isRoot(s, subj)
          || getNaming().hasPriv(s, ns, subj, priv)
          || _isAll(s, ns, priv)
        )
        {
          rv = true;
        }
      }
      catch (SchemaException eS) {
        rv = false; 
      }
    }
    s.getNamingCache().put(ns, subj, priv, rv);
    return rv;
  } // protected static boolean hasPriv(s, ns, subj, priv)

  // @since   1.1.0
  protected static void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    getAccess().revokePriv(s, g, priv);
    s.getAccessCache().revokePriv(g, priv);
  } // protected static void revokePriv(s, g, priv)

  // @since   1.1.0
  protected static void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    getNaming().revokePriv(s, ns, priv);
    s.getNamingCache().revokePriv(ns, priv);
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
    s.getAccessCache().revokePriv(g, subj, priv);
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
    s.getNamingCache().revokePriv(ns, subj, priv);
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

} // class PrivilegeResolver

