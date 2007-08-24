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
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Collection;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;


/** 
 * Privilege helper class.
 * <p>TODO 20070823 Relocate these methods once I figure out the best home for them.</p>
 * @author  blair christensen.
 * @version $Id: PrivilegeHelper.java,v 1.2 2007-08-24 18:33:50 blair Exp $
 * @since   @HEAD@
 */
class PrivilegeHelper {


  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canAdmin(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    return s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canCreate(GrouperSession s, Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return ns.getSession().getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.CREATE);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canOptin(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false;
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canOptout(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTOUT)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false; 
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canRead(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
   }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canStem(Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return ns.getSession().getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.STEM);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canUpdate(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static boolean canView(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.VIEW)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTOUT)
    )
    {
      return true;
    }
    return false; 
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @since   @HEAD@
   */
  protected static Set canViewGroups(GrouperSession s, Set candidates) {
    Set       groups  = new LinkedHashSet();
    Group     g;
    Iterator  it      = candidates.iterator();
    while (it.hasNext()) {
      g = (Group) new Group().setDTO( (GroupDTO) it.next() );
      g.setSession(s);
      if ( canView( s, g, s.getSubject() ) ) {
        groups.add(g);
      }
    }
    return groups;
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @SINCE   @HEAD@
   */
  protected static Set canViewMemberships(GrouperSession s, Collection c) {
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = c.iterator();
    while ( it.hasNext() ) {
      ms = new Membership();
      ms.setDTO( (MembershipDTO) it.next() );
      ms.setSession(s);
      try {
        if ( FieldType.ACCESS.equals( ms.getList().getType() ) ) {
          dispatch( s, ms.getGroup(), s.getSubject(), ms.getList().getReadPriv() );
        }
        mships.add(ms);
      }
      catch (Exception e) {
        ErrorLog.error( PrivilegeHelper.class, "canViewMemberships: " + e.getMessage() );
      }
    }
    return mships;
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @SINCE   @HEAD@
   */
  protected static void dispatch(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if ( Privilege.isNaming(priv) ) {
      throw new SchemaException("access privileges only apply to groups");
    }

    if      (priv.equals(AccessPrivilege.ADMIN))  {
      rv = PrivilegeHelper.canAdmin(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_ADMIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      rv = PrivilegeHelper.canOptin(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_OPTIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      rv = PrivilegeHelper.canOptout(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_OPTOUT;
      }
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      rv = PrivilegeHelper.canRead(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_READ;
      }
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      rv = PrivilegeHelper.canView( s, g, subj );
      if (!rv) {
        msg = E.CANNOT_VIEW;
      }
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      rv = PrivilegeHelper.canUpdate(s, g, subj);
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
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @SINCE   @HEAD@
   */
  protected static void dispatch(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if      ( Privilege.isAccess(priv) ) {
      throw new SchemaException("naming privileges only apply to stems");
    }

    if      (priv.equals(NamingPrivilege.CREATE)) { 
      rv = PrivilegeHelper.canCreate(s, ns,  subj);
      if (!rv) {
        msg = E.CANNOT_CREATE;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM))   {
      rv = PrivilegeHelper.canStem(ns, subj);
      if (!rv) {
        msg = E.CANNOT_STEM;
      }
    }
    else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } 

  /**
   * TODO 20070824 add tests
   * @return  Given an array of privileges return an array of access privileges.
   * @since   @HEAD@
   */
  protected static Privilege[] getAccessPrivileges(Privilege[] privileges) {
    Set<Privilege> accessPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isAccess(priv) ) {
        accessPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return accessPrivs.toArray(template);
  }

  /**
   * TODO 20070824 add tests
   * @return  Given an array of privileges return an array of naming privileges.
   * @since   @HEAD@
   */
  protected static Privilege[] getNamingPrivileges(Privilege[] privileges) {
    Set<Privilege> namingPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isNaming(priv) ) {
        namingPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return namingPrivs.toArray(template);
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @SINCE   @HEAD@
   */
  protected static boolean isRoot(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    if ( SubjectHelper.eq( s.getSubject(), SubjectFinder.findRootSubject() ) ) {
      rv = true;
    }
    else {
      rv = isWheel(s);
    }
    return rv;
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @SINCE   @HEAD@
   */
  protected static boolean isWheel(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    if ( Boolean.valueOf( s.getConfig( GrouperConfig.PROP_USE_WHEEL_GROUP ) ).booleanValue() ) {
      String name = s.getConfig( GrouperConfig.PROP_WHEEL_GROUP );
      try {
        // goodbye, performance
        Group wheel = GroupFinder.findByName( s.internal_getRootSession(), name );
        rv          = wheel.hasMember( s.getSubject() );
      }
      catch (GroupNotFoundException eGNF) {
        // wheel group not found. oh well!
        ErrorLog.error( PrivilegeHelper.class, E.NO_WHEEL_GROUP + name );
      }
    } 
    return rv;
  } 

}

