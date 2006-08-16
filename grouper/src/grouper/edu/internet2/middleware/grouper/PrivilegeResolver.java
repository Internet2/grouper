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
 * @version $Id: PrivilegeResolver.java,v 1.46 2006-08-16 21:26:57 blair Exp $
 */
public class PrivilegeResolver {

  // PRIVATE CLASS CONSTANTS //
  // TODO Move to *E*
  private static final String ERR_CI    = "unable to instantiate interface ";  
  private static final String ERR_RPC   = "unable to reset privilege caches: ";
  // TODO Move to *M*
  private static final String MSG_RPC   = "reset privilege cache: ";


  // PRIVATE CLASS VARIABLES //
  private static PrivilegeResolver pr = null;


  // PRIVATE INSTANCE VARIABLES //
  private PrivilegeCache  ac        = null;
  private AccessAdapter   access    = null; 
  private PrivilegeCache  nc        = null;
  private NamingAdapter   naming    = null;
  private boolean         use_wheel = true;


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
  protected static PrivilegeResolver getInstance() {
    if (pr == null) {
      GrouperConfig cfg = GrouperConfig.getInstance();
      pr = new PrivilegeResolver();
    
      pr.access = (AccessAdapter) _createInterface(
        cfg.getProperty(GrouperConfig.PAI)
      );
      pr.naming = (NamingAdapter) _createInterface(
        cfg.getProperty(GrouperConfig.PNI)
      );
      // Get access and naming privilege classes
      // TODO Make configurable
      pr.ac = PrivilegeCache.getCache(PrivilegeCache.ACCESS);
      pr.nc = PrivilegeCache.getCache(PrivilegeCache.NAMING);
    }
    return pr;
  } // protected static PrivilegeResolver getInstance()


  // PROTECTED INSTANCE METHODS //

  protected void canADMIN(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.ADMIN;
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot ADMIN");
    }
  } // protected void canADMIN(s, g, subj)

  protected void canCREATE(GrouperSession s, Stem ns, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = NamingPrivilege.CREATE;
    if (PrivilegeResolver.getInstance().hasPriv(s, ns, subj, priv)) {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot CREATE");
    }
  } // protected void canCREATE(s, ns, subj)

  protected void canOPTIN(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.OPTIN;
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot OPTIN");
    }
  } // protected void canOPTIN(s, g, subj)

  protected void canOPTOUT(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.OPTOUT;
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot OPTOUT");
    }
  } // protected void canOPTOUT(s, g, subj)

  protected void canPrivDispatch(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if      (priv.equals(AccessPrivilege.ADMIN))  { 
      this.canADMIN(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      this.canOPTIN(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      this.canOPTOUT(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      this.canREAD(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      this.canUPDATE(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      this.canVIEW(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.SYSTEM))  {
      throw new InsufficientPrivilegeException("system maintained");
    }
    else {
      throw new SchemaException("unknown access privilege: " + priv);
    }
  } // protected void canPrivDispatch(s, g, subj, priv)

  protected void canPrivDispatch(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    if      (priv.equals(NamingPrivilege.CREATE)) { 
      this.canCREATE(s, ns, subj);
    }
    else if (priv.equals(NamingPrivilege.STEM))   {
      this.canSTEM(s, ns, subj);
    }
    else {
      throw new SchemaException("unknown naming privilege: " + priv);
    }
  } // protected void canPrivDispatch(s, ns, subj, priv)

  protected void canREAD(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.READ;
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot READ");
    }
  } // protected void canREAD(s, g, subj)

  protected void canSTEM(GrouperSession s, Stem ns, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = NamingPrivilege.STEM;
    if (PrivilegeResolver.getInstance().hasPriv(s, ns, subj, priv)) {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot STEM");
    }
  } // protected void canSTEM(s, ns, subj)

  protected void canUPDATE(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.UPDATE;
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot UPDATE");
    }
  } // protected void canUPDATE(s, g, subj)

  protected void canVIEW(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    // TODO This is ugly
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.VIEW;
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.READ)
    )
    {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTIN)
    )
    {
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
    )
    {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException("cannot VIEW");
    }
  } // protected void canVIEW(s, g, subj)
  // TODO Can I remove s or subj?

  // TODO Deprecate?
  protected Set canVIEW(GrouperSession s, Set candidates) {
    Set             groups  = new LinkedHashSet();
    Group           g;
    Iterator        iter    = candidates.iterator();
    while (iter.hasNext()) {
      g = (Group) iter.next();
      g.setSession(s);
      try {
        // Can we view the group
        this.canVIEW(s, g, s.getSubject());
        groups.add(g);
      }
      catch (InsufficientPrivilegeException eIP) {
        // Ignore
      }  
    }
    return groups;
  }

  // If the subject being added is a group, verify that we can VIEW it
  protected Member canViewSubject(GrouperSession s, Subject subj)
    throws  ModelException
  {
    try {
      Member  m = MemberFinder.findBySubject(s, subj);
      if (m.getSubjectType().equals(SubjectTypeEnum.valueOf("group"))) {
        Subject who   = s.getSubject();
        Group   what  = m.toGroup();
        PrivilegeResolver.getInstance().canVIEW(s, what, who);
      }
      return m;
    }
    catch (Exception e) {
      throw new ModelException(e.getMessage(), e);
    }
  } // protected Member canViewSubject(s, subj)

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

  protected boolean isRoot(Subject subj) {
    boolean rv = false;
    // First check to see if this is GrouperSystem
    // FIXME Refactor
    if      (
      (subj.getId().equals(GrouperConfig.ROOT))
      && (subj.getSource().getId().equals(InternalSourceAdapter.ID))
      && (subj.getType().getName().equals(GrouperConfig.IST))
    )
    {
      rv = true;
    }  
    else if (this.use_wheel) {
      rv = this._isWheel(subj);
    }
    return rv;
  }

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
    else if (this.isRoot(subj)) {
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
    else if (this.isRoot(subj)) {
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

  private boolean _isWheel(Subject subj) {
    boolean       rv  = false;
    GrouperConfig cfg = GrouperConfig.getInstance();
    if (cfg.getProperty(GrouperConfig.GWU).equals(GrouperConfig.BT)) {
      // TODO This has to be a performance killer
      String name = cfg.getProperty(GrouperConfig.GWG);
      try {
        Group wheel = GroupFinder.findByName(GrouperSession.startTransient(), name);
        rv          = wheel.hasMember(subj);
      }
      catch (GroupNotFoundException eGNF) {
        // Group not found.  Oh well.
        // TODO The problem is that the test suite deletes it.  
        //      But, now that the test suite has evolved, I should be able to
        //      more properly test this.
        ErrorLog.error(
          PrivilegeResolver.class, 
          "disabling wheel group.  enabled but found found: " + name
        );
        this.use_wheel = false;
      }
    } 
    else {
      this.use_wheel = false;
    }
    return rv;
  } // private boolean _isWheel(subj)

}

