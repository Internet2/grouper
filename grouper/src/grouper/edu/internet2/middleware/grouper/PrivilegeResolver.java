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
import  java.lang.reflect.*;
import  java.util.*;
import  net.sf.ehcache.*;

/** 
 * Privilege resolution class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: PrivilegeResolver.java,v 1.53 2006-08-17 18:19:09 blair Exp $
 */
public class PrivilegeResolver {

  // PRIVATE CLASS CONSTANTS //
  // TODO Move to *E*
  private static final String ERR_CI    = "unable to instantiate interface ";  
  private static final String ERR_RPC   = "unable to reset privilege caches: ";
  // TODO Move to *M*
  private static final String MSG_RPC   = "reset privilege cache: ";


  // PRIVATE CLASS VARIABLES //
  private static  PrivilegeResolver pr        = null;
  private static  boolean           use_wheel = Boolean.valueOf(
    GrouperConfig.getProperty(GrouperConfig.GWU)
  );


  // PRIVATE INSTANCE VARIABLES //
  private PrivilegeCache  ac        = null;
  private AccessAdapter   access    = null; 
  private PrivilegeCache  nc        = null;
  private NamingAdapter   naming    = null;


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
      PrivilegeResolver pr = PrivilegeResolver.getInstance();
      if (pr.ac != null) {
        pr.ac.removeAll();
        DebugLog.info(PrivilegeResolver.class, MSG_RPC + PrivilegeCache.ACCESS);
      }
      if (pr.nc != null) {
        pr.nc.removeAll();
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
    return PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN);
  } // protected static boolean canADMIN(s, g, subj)

  // @since   1.1.0
  protected static boolean canCREATE(GrouperSession s, Stem ns, Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(s, ns, subj, NamingPrivilege.CREATE);
  } // protected static boolean canCREATE(s, ns, subj)

  // @since   1.1.0
  protected static boolean canOPTIN(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTIN)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false;
  } // protected static boolean canOPTIN(s, g, subj)

  // @since   1.1.0
  protected static boolean canOPTOUT(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
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
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.READ)  
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
   }// protected static boolean canREAD(s, g, subj)

  // @since   1.1.0
  protected static boolean canSTEM(GrouperSession s, Stem ns, Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(s, ns, subj, NamingPrivilege.STEM);
  } // protected static boolean canSTEM(s, ns, subj)

  // @since   1.1.0
  protected static boolean canUPDATE(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
  } // protected static boolean canUPDATE(s, g, subj)

  // @since   1.1.0
  protected static boolean canVIEW(GrouperSession s, Group g, Subject subj) {
    if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.VIEW)  
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.READ)  
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTIN) 
      ||
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
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

  // FIXME DEPRECATE!
  protected static PrivilegeResolver getInstance() {
    if (pr == null) {
      pr = new PrivilegeResolver();
    
      pr.access = (AccessAdapter) _createInterface(
        GrouperConfig.getProperty(GrouperConfig.PAI)
      );
      pr.naming = (NamingAdapter) _createInterface(
        GrouperConfig.getProperty(GrouperConfig.PNI)
      );
      // Get access and naming privilege classes
      // TODO Make configurable
      pr.ac = PrivilegeCache.getCache(PrivilegeCache.ACCESS);
      pr.nc = PrivilegeCache.getCache(PrivilegeCache.NAMING);
    }
    return pr;
  } // protected static PrivilegeResolver getInstance()

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


  // PROTECTED INSTANCE METHODS //

  // TODO Deprecate?
  protected Set canVIEW(GrouperSession s, Set candidates) {
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
  }

  protected Set getPrivs(
    GrouperSession s, Group g, Subject subj
  )
  {
    GrouperSession.validate(s);
    return access.getPrivs(s, g, subj);
  } // protected Set getPrivs(s, g, subj)

  protected Set getPrivs(
    GrouperSession s, Stem ns, Subject subj
  )
  {
    GrouperSession.validate(s);
    return naming.getPrivs(s, ns, subj);
  } // protected Set getPrivs(s, ns, subj)

  protected Set getGroupsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    GrouperSession.validate(s);
    return this.access.getGroupsWhereSubjectHasPriv(s, subj, priv);
  } // protected Set getGroupsWhereSubjectHasPriv(s, subj, priv)

  protected Set getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    GrouperSession.validate(s);
    return this.naming.getStemsWhereSubjectHasPriv(s, subj, priv);
  } // protected Set getStemsWhereSubjectHasPriv(s, subj, priv)

  protected Set getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    GrouperSession.validate(s);
    return this.access.getSubjectsWithPriv(s, g, priv);
  } // protected Set getSubjectsWithPriv(s, g, priv)

  protected Set getSubjectsWithPriv(GrouperSession s, Stem ns, Privilege priv) 
    throws SchemaException
  {
    GrouperSession.validate(s);
    return this.naming.getSubjectsWithPriv(s, ns, priv);
  } // protected Set getSubjectsWithPriv(s, ns, priv)

  protected void grantPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.access.grantPriv(s, g, subj, priv);
    // FIXME
    try {
      this.ac.removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected void grantPriv(s, g, subj, priv)

  protected void grantPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.naming.grantPriv(s, ns, subj, priv);
    // FIXME
    try {
      this.nc.removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected void grantPriv(s, ns, subj, priv)

  protected boolean hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    boolean rv = false;
    Element el = this.ac.get(g, subj, priv);
    if (el != null) {
      if (el.getValue().equals(GrouperConfig.BT)) {
        rv = true;
      }
    }
    else if (isRoot(subj)) {
      rv = true;  
    }
    else {
      try {
        rv = access.hasPriv(s, g, subj, priv);
        if (rv == false) {
          rv = this._isAll(s, g, priv);
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
  } // protected boolean hasPriv(s, g, subj, priv)

  protected boolean hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    boolean rv = false;
    Element el = this.nc.get(ns, subj, priv);
    if (el != null) {
      if (el.getValue().equals(GrouperConfig.BT)) {
        rv = true;
      }
    }
    else if (isRoot(subj)) {
      rv = true;  
    }
    else {
      try {
        rv = naming.hasPriv(s, ns, subj, priv);
        if (rv == false) {
          rv = this._isAll(s, ns, priv);
        } 
      }
      catch (SchemaException eS) {
        rv = false;
        return rv;
      }
    }
    if (el == null) {
      nc.put(ns, subj, priv, rv);
    }
    return rv;
  } // protected boolean hasPriv(s, ns, subj, priv)

  protected void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.access.revokePriv(s, g, priv);
    // FIXME
    try {
      this.ac.removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected void revokePriv(s, g, priv)

  protected void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.naming.revokePriv(s, ns, priv);
    // FIXME
    try {
      this.nc.removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected void revokePriv(s, ns, priv)

  protected void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.access.revokePriv(s, g, subj, priv);
    // FIXME
    try {
      this.ac.removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected void revokePriv(s, g, subj, priv)

  protected void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.naming.revokePriv(s, ns, subj, priv);
    // FIXME
    try {
      this.nc.removeAll();
    }
    catch (Exception e) {
      ErrorLog.error(PrivilegeResolver.class, ERR_RPC + e.getMessage());
    }
  } // protected void revokePriv(s, ns, subj, priv)


  // PRIVATE CLASS METHODS //
  private static Object _createInterface(String name) 
    throws  GrouperRuntimeException
  {
    try {
      Class   classType     = Class.forName(name);
      Class[] paramsClass   = new Class[] { };
      Constructor con = classType.getDeclaredConstructor(paramsClass);
      Object[] params = new Object[] { };
      return con.newInstance(params);
    }
    catch (Exception e) {
      String msg = ERR_CI + name + ": " + e.getMessage();
      ErrorLog.fatal(PrivilegeResolver.class, msg);
      throw new GrouperRuntimeException(msg, e);
    }
  } // private static Object _createInterface(name)

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


  // PRIVATE INSTANCE METHODS //
  private boolean _isAll(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    return access.hasPriv(s, g, SubjectFinder.findAllSubject(), priv);
  } // private boolean _isAll(s, g, priv);

  private boolean _isAll(GrouperSession s, Stem ns, Privilege priv) 
    throws  SchemaException
  {
    return naming.hasPriv(s, ns, SubjectFinder.findAllSubject(), priv);
  } // private boolean _isAll(s, ns, priv);

} // public class PrivilegeResolver

