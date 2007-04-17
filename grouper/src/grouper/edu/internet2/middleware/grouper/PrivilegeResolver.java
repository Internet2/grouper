/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import  edu.internet2.middleware.grouper.internal.cache.PrivilegeCacheElement;
import  edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  edu.internet2.middleware.grouper.internal.util.Realize;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/** 
 * Privilege resolution class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeResolver.java,v 1.83 2007-04-17 17:13:26 blair Exp $
 */
 class PrivilegeResolver {

  // PRIVATE CLASS VARIABLES //
  private static  AccessAdapter     access    = internal_getAccess();
  private static  NamingAdapter     naming    = internal_getNaming();


  // CONSTRUCTORS //
  protected PrivilegeResolver() {
    super();
  } // protected PrivilegeResolver()



  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static boolean internal_canADMIN(GrouperSession s, Group g, Subject subj) {
    return PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.ADMIN);
  } // protected static boolean internal_canADMIN(s, g, subj)

  // @since   1.2.0
  protected static boolean internal_canCREATE(GrouperSession s, Stem ns, Subject subj) {
    return PrivilegeResolver.internal_hasPriv(s, ns, subj, NamingPrivilege.CREATE);
  } // protected static boolean internal_canCREATE(s, ns, subj)

  // @since   1.2.0
  protected static boolean internal_canOPTIN(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.OPTIN)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false;
  } // protected static boolean internal_canOPTIN(s, g, subj)

  // @since   1.2.0
  protected static boolean internal_canOPTOUT(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean internal_canOPTOUT(s, g, subj)

  // @since   1.2.0
  protected static void internal_canPrivDispatch(
    GrouperSession s, Owner o, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 
    if      (priv.equals(AccessPrivilege.ADMIN))  {
      rv = internal_canADMIN(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_ADMIN;
      }
    }
    else if (priv.equals(NamingPrivilege.CREATE)) { 
      rv = internal_canCREATE(s, (Stem) o, subj);
      if (!rv) {
        msg = E.CANNOT_CREATE;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      rv = internal_canOPTIN(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_OPTIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      rv = internal_canOPTOUT(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_OPTOUT;
      }
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      rv = internal_canREAD(s, (Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_READ;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM))   {
      rv = internal_canSTEM((Stem) o, subj);
      if (!rv) {
        msg = E.CANNOT_STEM;
      }
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      rv = internal_canVIEW((Group) o, subj);
      if (!rv) {
        msg = E.CANNOT_VIEW;
      }
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      rv = internal_canUPDATE(s, (Group) o, subj);
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
  } // protected static void internal_canPrivDispatch(s, o, subj, priv)

  // @since   1.2.0
  protected static boolean internal_canREAD(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.READ)  
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
   }// protected static boolean internal_canREAD(s, g, subj)

  // @since   1.2.0
  protected static boolean internal_canSTEM(Stem ns, Subject subj) {
    return PrivilegeResolver.internal_hasPriv(ns.getSession(), ns, subj, NamingPrivilege.STEM);
  } // protected static boolean internal_canSTEM(ns, subj)

  // @since   1.2.0
  protected static boolean internal_canUPDATE(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.UPDATE)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean internal_canUPDATE(s, g, subj)

  // @since   1.2.0
  protected static boolean internal_canVIEW(Group g, Subject subj) {
    GrouperSession s = g.getSession();
    if (
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.VIEW)  
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.READ)  
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.UPDATE)
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.OPTIN) 
      ||
      PrivilegeResolver.internal_hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean internal_canVIEW(g, subj)

  // @since   1.2.0
  protected static Set internal_canViewGroups(GrouperSession s, Set candidates) {
    Set             groups  = new LinkedHashSet();
    Group           g;
    Iterator        it      = candidates.iterator();
    while (it.hasNext()) {
      g = (Group) Rosetta.getAPI( it.next() );
      g.setSession(s);
      // Can we view the group
      if (internal_canVIEW(g, s.getSubject())) {
        groups.add(g);
      }
    }
    return groups;
  } // protected static Set internal_canViewGroups(s, candidates)

  // @since   1.2.0
  protected static Set internal_canViewMemberships(GrouperSession s, Collection c) {
    GrouperSession.validate(s);
    Set           mships  = new LinkedHashSet();
    Membership    ms;
    String        msg     = "canViewMemberships: ";
    Iterator      it      = c.iterator();
    while (it.hasNext()) {
      ms = new Membership();
      ms.setDTO( (MembershipDTO) it.next() );
      ms.setSession(s);
      try {
        if (FieldType.ACCESS.equals(ms.getList().getType())) {
          internal_canPrivDispatch(
            s, ms.getGroup(), s.getSubject(), ms.getList().getReadPriv()
          );
        }
        mships.add(ms);
      }
      catch (GroupNotFoundException eGNF)         {
        ErrorLog.error(PrivilegeResolver.class, msg + eGNF.getMessage());
      }
      catch (InsufficientPrivilegeException eIP)  {
        ErrorLog.error(PrivilegeResolver.class, msg + eIP.getMessage());
      }
      catch (SchemaException eS)                  {
        ErrorLog.error(PrivilegeResolver.class, msg + eS.getMessage());
      }
    }
    return mships;
  } // protected static Set internal_canViewMemberships(s, c)
 
  // @since   1.2.0
  protected static AccessAdapter internal_getAccess() {
    if (access == null) {
      access = (AccessAdapter) Realize.instantiate( GrouperConfig.getProperty(GrouperConfig.PAI) );
    }
    return access;
  } // protected static AccessAdapter internal_getAccess()

  // @since   1.2.0 
  protected static Set internal_getGroupsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    return internal_getAccess().getGroupsWhereSubjectHasPriv(s, subj, priv);
  } // protected static Set internal-getGroupsWhereSubjectHasPriv(s, subj, priv)

  // @since   1.2.0
  protected static NamingAdapter internal_getNaming() {
    if (naming == null) {
      naming = (NamingAdapter) Realize.instantiate( GrouperConfig.getProperty(GrouperConfig.PNI) );
    }
    return naming;
  } // protected static AccessAdapter internal_getNaming()

  // @since   1.2.0
  protected static Set internal_getPrivs(
    GrouperSession s, Group g, Subject subj
  )
  {
    return internal_getAccess().getPrivs(s, g, subj);
  } // protected static Set internal_getPrivs(s, g, subj)

  // @since   1.2.0
  protected static Set internal_getPrivs(
    GrouperSession s, Stem ns, Subject subj
  )
  {
    return internal_getNaming().getPrivs(s, ns, subj);
  } // protected static Set internal_getPrivs(s, ns, subj)

  // @since   1.2.0
  protected static Set internal_getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    return internal_getNaming().getStemsWhereSubjectHasPriv(s, subj, priv);
  } // protected static Set internal_getStemsWhereSubjectHasPriv(s, subj, priv)

  // @since   1.2.0
  protected static Set internal_getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    return internal_getAccess().getSubjectsWithPriv(s, g, priv);
  } // protected static Set internal_getSubjectsWithPriv(s, g, priv)

  // @since   1.2.0
  protected static Set internal_getSubjectsWithPriv(GrouperSession s, Stem ns, Privilege priv) 
    throws SchemaException
  {
    return internal_getNaming().getSubjectsWithPriv(s, ns, priv);
  } // protected static Set internal_getSubjectsWithPriv(s, ns, priv)

  // @since   1.2.0
  protected static void internal_grantPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    internal_getAccess().grantPriv(s, g, subj, priv);
    ( (GrouperSessionDTO) s.getDTO() ).getAccessCache().grantPriv(g, subj, priv);
  } // protected static void internal_grantPriv(s, g, subj, priv)

  // @since   1.2.0
  protected static void internal_grantPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    internal_getNaming().grantPriv(s, ns, subj, priv);
    ( (GrouperSessionDTO) s.getDTO() ).getNamingCache().grantPriv(ns, subj, priv);
  } // protected static void internal_grantPriv(s, ns, subj, priv)

  // @since   1.2.0
  protected static boolean internal_hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    boolean rv = false;
    PrivilegeCacheElement el = ( (GrouperSessionDTO) s.getDTO() ).getAccessCache().get(g, subj, priv);
    if (el.getIsCached()) {
      rv = el.getHasPriv(); // use cached result
    }
    else {
      try { // TODO 20070321. Eliminate the try/catch.  Too bad it is in the privilege interface.
        if (
             RootPrivilegeResolver.internal_isRoot(s, subj)
          || internal_getAccess().hasPriv(s, g, subj, priv)
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
    ( (GrouperSessionDTO) s.getDTO() ).getAccessCache().put(g, subj, priv, rv);
    return rv;
  } // protected static boolean internal_hasPriv(s, g, subj, priv)

  // @since   1.2.0
  protected static boolean internal_hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    boolean rv = false;
    PrivilegeCacheElement el = ( (GrouperSessionDTO) s.getDTO() ).getNamingCache().get(ns, subj, priv);
    if (el.getIsCached()) {
      rv = el.getHasPriv(); // use cached result
    }
    else {
      try { // TODO 20070321 Eliminate the try/catch.  Too bad it is in the privilege interface.
        if (
             RootPrivilegeResolver.internal_isRoot(s, subj)
          || internal_getNaming().hasPriv(s, ns, subj, priv)
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
    ( (GrouperSessionDTO) s.getDTO() ).getNamingCache().put(ns, subj, priv, rv);
    return rv;
  } // protected static boolean internal_hasPriv(s, ns, subj, priv)

  // @since   1.2.0
  protected static void internal_revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    internal_getAccess().revokePriv(s, g, priv);
    ( (GrouperSessionDTO) s.getDTO() ).getAccessCache().revokePriv(g, priv);
  } // protected static void internal_revokePriv(s, g, priv)

  // @since   1.2.0
  protected static void internal_revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    internal_getNaming().revokePriv(s, ns, priv);
    ( (GrouperSessionDTO) s.getDTO() ).getNamingCache().revokePriv(ns, priv);
  } // protected static void internal_revokePriv(s, ns, priv)

  // @since   1.2.0
  protected static void internal_revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    internal_getAccess().revokePriv(s, g, subj, priv);
    ( (GrouperSessionDTO) s.getDTO() ).getAccessCache().revokePriv(g, subj, priv);
  } // protected static void internal_revokePriv(s, g, subj, priv)

  // @since   1.2.0
  protected static void internal_revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    internal_getNaming().revokePriv(s, ns, subj, priv);
    ( (GrouperSessionDTO) s.getDTO() ).getNamingCache().revokePriv(ns, subj, priv);
  } // protected static void internal_revokePriv(s, ns, subj, priv)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static boolean _isAll(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    return internal_getAccess().hasPriv(s, g, SubjectFinder.findAllSubject(), priv);
  } // private static boolean _isAll(s, g, priv);

  // @since   1.1.0
  private static boolean _isAll(GrouperSession s, Stem ns, Privilege priv) 
    throws  SchemaException
  {
    return internal_getNaming().hasPriv(s, ns, SubjectFinder.findAllSubject(), priv);
  } // private static boolean _isAll(s, ns, priv);

} // class PrivilegeResolver

