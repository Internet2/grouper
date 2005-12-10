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
import  edu.internet2.middleware.subject.provider.*;
import  java.lang.reflect.*;
import  java.util.*;
import  org.apache.commons.logging.*;


/** 
 * Privilege resolution class.
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: PrivilegeResolver.java,v 1.27 2005-12-10 22:31:36 blair Exp $
 *     
*/
class PrivilegeResolver {

  // Private Class Constants
  private static final String BT      = "true";
  private static final String CFG_GWG = "groups.wheel.group";
  private static final String CFG_GWU = "groups.wheel.use";
  private static final Log    LOG     = LogFactory.getLog(PrivilegeResolver.class);


  // Private Class Variables
  private static PrivilegeResolver pr = null;


  // Private Instance Variables
  private AccessAdapter access;
  private Group         wheel     = null;
  private NamingAdapter naming;


  // Constructors
  private PrivilegeResolver() {
    // nothing
  } // private PrivilegeResolver()


  // Protected Class Methods
  protected static PrivilegeResolver getInstance() {
    if (pr == null) {
      pr = new PrivilegeResolver();
      pr.access = (AccessAdapter) _createInterface(
        GrouperConfig.getInstance().getProperty("interface.access")
      );
      pr.naming = (NamingAdapter) _createInterface(
        GrouperConfig.getInstance().getProperty("interface.naming")
      );
    }
    return pr;
  } // protected static PrivilegeResolver getInstance()


  // Protected Instance Methods

  protected void canADMIN(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.ADMIN;
    String    msg   = "canADMIN: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      msg += "ADMIN";
      can = true;
    }
    if (can == false) {
      GrouperLog.debug(LOG, s, msg + "no");
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canADMIN(s, g, subj)

  protected void canCREATE(GrouperSession s, Stem ns, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = NamingPrivilege.CREATE;
    String    msg   = "canCREATE: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, ns, subj, priv)) {
      msg += "CREATE";
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + ns.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canCREATE(s, ns, subj)

  protected void canOPTIN(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.OPTIN;
    String    msg   = "canOPTIN: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      msg += "OPTIN";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      msg += "ADMIN";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      GrouperLog.debug(LOG, s, msg + "UPDATE");
      can = true;
    }
    if (can == false) {
      GrouperLog.debug(LOG, s, msg + "no");
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canOPTIN(s, g, subj)

  protected void canOPTOUT(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.OPTOUT;
    String    msg   = "canOPTOUT: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      msg += "OPTOUT";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      msg += "ADMIN";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      msg += "UPDATE";
      can = true;
    }
    if (can == false) {
      GrouperLog.debug(LOG, s, msg + "no");
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canOPTOUT(s, g, subj)

  protected void canPrivDispatch(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    String msg = "canPrivDispatch '" + priv.getName().toUpperCase() + "': ";
    if      (priv.equals(AccessPrivilege.ADMIN))  { 
      GrouperLog.debug(LOG, s, msg + "canADMIN");
      this.canADMIN(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      GrouperLog.debug(LOG, s, msg + "canOPTIN");
      this.canOPTIN(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      GrouperLog.debug(LOG, s, msg + "canOPTOUT");
      this.canOPTOUT(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      GrouperLog.debug(LOG, s, msg + "canREAD");
      this.canREAD(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      GrouperLog.debug(LOG, s, msg + "canUPDATE");
      this.canUPDATE(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      GrouperLog.debug(LOG, s, msg + "canVIEW");
      this.canVIEW(s, g, subj);
    }
    else if (priv.equals(AccessPrivilege.SYSTEM))  {
      GrouperLog.debug(LOG, s, msg + "system maintained");
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
    String    msg   = "canREAD: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      msg += "READ";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      msg += "ADMIN";
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canREAD(s, g, subj)

  protected void canSTEM(GrouperSession s, Stem ns, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = NamingPrivilege.STEM;
    String    msg   = "canSTEM: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, ns, subj, priv)) {
      msg += "STEM";
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + ns.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canSTEM(s, ns, subj)

  protected void canUPDATE(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.UPDATE;
    String    msg   = "canUPDATE: ";
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      msg += "UPDATE";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      msg += "ADMIN";
      can = true;
    }
    if (can == false) {
      GrouperLog.debug(LOG, s, msg + "no");
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canUPDATE(s, g, subj)

  protected void canVIEW(GrouperSession s, Group g, Subject subj)
    throws  InsufficientPrivilegeException
  {
    // TODO This is ugly
    boolean   can   = false;
    Privilege priv  = AccessPrivilege.VIEW;
    String    msg   = "canVIEW ";
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      msg += "VIEW";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.READ)
    )
    {
      msg += "READ";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.ADMIN)
    )
    {
      msg += "ADMIN";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.UPDATE)
    )
    {
      msg += "UPDATE";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTIN)
    )
    {
      msg += "OPTIN";
      can = true;
    }
    else if (
      PrivilegeResolver.getInstance().hasPriv(s, g, subj, AccessPrivilege.OPTOUT)
    )
    {
      msg += "OPTOUT";
      can = true;
    }
    if (can == false) {
      GrouperLog.debug(LOG, s, msg + "no");
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
    GrouperLog.debug(LOG, s, msg);
  } // protected void canVIEW(s, g, subj)
  // TODO Can I remove s or subj?

  // TODO Deprecate?
  protected Set canVIEW(GrouperSession s, Set candidates) {
    Set             groups  = new LinkedHashSet();
    Iterator        iter    = candidates.iterator();
    while (iter.hasNext()) {
      Group g = (Group) iter.next();
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

  protected Member canViewSubject(GrouperSession s, Subject subj, String msg) 
    throws  MemberNotFoundException
  {
    Member  m = MemberFinder.findBySubject(s, subj);
    // If the subject being added is a group, verify that we can VIEW it
    try {
      if (m.getSubjectType().equals(SubjectTypeEnum.valueOf("group"))) {
        Subject who   = s.getSubject();
        Group   what  = m.toGroup();
        PrivilegeResolver.getInstance().canVIEW(s, what, who);
        GrouperLog.debug(LOG, s, msg + "true");
      }
    }
    catch (GroupNotFoundException eGNF) {
      GrouperLog.debug(LOG, s, msg + eGNF.getMessage());
      throw new MemberNotFoundException(msg + eGNF.getMessage());
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, msg + eIP.getMessage());
      throw new MemberNotFoundException(msg + eIP.getMessage());
    }
    return m;
  } // protected Member canViewSubject(s, subj, msg)

  protected void canWriteField(
    GrouperSession s, Group g, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    String msg = "canWriteField '" + f + "' '" + type + "'";
    GrouperLog.debug(LOG, s, msg);

    // Validate field type
    if (f.getType().equals(type)) {
      GrouperLog.debug(LOG, s, msg + " right type");
    }
    else {
      String err = msg + " wrong type";
      GrouperLog.debug(LOG, s, err);
      throw new SchemaException(err);
    }

    try {
      PrivilegeResolver.getInstance().canPrivDispatch(
        s, g, subj, f.getWritePriv()
      );
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }
  } // protected static void canWriteField(s, g, subj, f, type)

  protected void canWriteField(
    GrouperSession s, Stem ns, Subject subj, Field f, FieldType type
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    String msg = "canWriteField '" + f + "' '" + type + "'";
    GrouperLog.debug(LOG, s, msg);

    // Validate field type
    // TODO extract
    if (f.getType().equals(type)) {
      GrouperLog.debug(LOG, s, msg + " right type");
    }
    else {
      String err = msg + " wrong type";
      GrouperLog.debug(LOG, s, err);
      throw new SchemaException(err);
    }

    try {
      PrivilegeResolver.getInstance().canPrivDispatch(
        s, ns, subj, f.getWritePriv()
      );
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, s, eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }
  } // protected static void canWriteField(s, ns, subj, f, type)

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
  {
    GrouperSession.validate(s);
    try {
      return this.access.getGroupsWhereSubjectHasPriv(s, subj, priv);
    }
    catch (SchemaException eS) {
      throw new RuntimeException(eS.getMessage());
    }
  } // protected Set getGroupsWhereSubjectHasPriv(s, subj, priv)

  protected Set getStemsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
  {
    GrouperSession.validate(s);
    try {
      return this.naming.getStemsWhereSubjectHasPriv(s, subj, priv);
    }
    catch (SchemaException eS) {
      throw new RuntimeException(eS.getMessage());
    }
  } // protected Set getStemsWhereSubjectHasPriv(s, subj, priv)

  protected Set getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv) {
    GrouperSession.validate(s);
    try {
      return this.access.getSubjectsWithPriv(s, g, priv);
    }
    catch (SchemaException eS) {
      throw new RuntimeException(eS.getMessage());
    }
  } // protected Set getSubjectsWithPriv(s, g, priv)

  protected Set getSubjectsWithPriv(GrouperSession s, Stem ns, Privilege priv) {
    GrouperSession.validate(s);
    try {
      return this.naming.getSubjectsWithPriv(s, ns, priv);
    }
    catch (SchemaException eS) {
      throw new RuntimeException(eS.getMessage());
    }
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
  } // protected void grantPriv(s, ns, subj, priv)

  protected boolean isRoot(Subject subj) {
    boolean rv = false;
    // First check to see if this is GrouperSystem
    if (
      (subj.getId().equals("GrouperSystem"))
      && (subj.getSource().getId().equals(InternalSourceAdapter.ID))
      && (subj.getType().getName().equals("application"))
    )
    {
      rv = true;
    }  
    // TODO REFACTOR/EXTRACT
    else {
      GrouperConfig cfg = GrouperConfig.getInstance();
      if (cfg.getProperty(CFG_GWU).equals(BT)) {
        // TODO This has to be a performance killer
        GrouperSession  root = GrouperSessionFinder.getTransientRootSession();
        String          name = cfg.getProperty(CFG_GWG);
        try {
          Group wheel = GroupFinder.findByName(root, name);
          rv = wheel.hasMember(subj);
        }
        catch (GroupNotFoundException eGNF) {
          // Group not found.  Oh well.
          // TODO The problem is that the test suite deletes it.  
          LOG.error("wheel group enabled but not found: " + name);
        }
        root.stop();
      }
    }
    return rv;
  } // protected boolean isRoot(subj)

  protected boolean hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    String msg = "hasPriv '" + priv.getName().toUpperCase() 
      + "' '" + subj.getId() + "' ";
    if (this.isRoot(subj)) {
      GrouperLog.debug(LOG, s, msg + "true (ROOT)");
      return true;
    }
    try {
      boolean rv = access.hasPriv(s, g, subj, priv);
      GrouperLog.debug(LOG, s, msg + rv);
      if (rv == false) {
        rv = this._isAll(s, g, priv);
        GrouperLog.debug(LOG, s, msg + rv + " (ALL)");
      } 
      return rv;
    }
    catch (SchemaException ePNF) {
      GrouperLog.debug(LOG, s, msg + "false (" + ePNF.getMessage() + ")");
      return false;
    }
  } // protected boolean hasPriv(s, g, subj, priv)

  protected boolean hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    String msg = "hasPriv '" + priv.getName().toUpperCase() 
      + "' '" + subj.getId() + "': ";
    if (this.isRoot(subj)) {
      GrouperLog.debug(LOG, s, msg + "true (ROOT)");
      return true;
    }
    try {
      boolean rv = naming.hasPriv(s, ns, subj, priv);
      GrouperLog.debug(LOG, s, msg + rv);
      if (rv == false) {
        rv = this._isAll(s, ns, priv);
        GrouperLog.debug(LOG, s, msg + rv + " (ALL)");
      } 
      return rv;
    }
    catch (SchemaException ePNF) {
      GrouperLog.debug(LOG, s, msg + "false (" + ePNF.getMessage() + ")");
      return false;
    }
  } // protected boolean hasPriv(s, ns, subj, priv)

  protected void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    String msg = "revokePriv '" + priv + "'";
    GrouperLog.debug(LOG, s, msg);
    this.access.revokePriv(s, g, priv);
    GrouperLog.debug(LOG, s, msg + " revoked");
  } // protected void revokePriv(s, g, priv)

  protected void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    GrouperSession.validate(s);
    this.naming.revokePriv(s, ns, priv);
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
  } // protected void revokePriv(s, ns, subj, priv)


  // Private Class Methods
  private static Object _createInterface(String name) {
    try {
      Class   classType     = Class.forName(name);
      Class[] paramsClass   = new Class[] { };
      try {
        Constructor con = 
          classType.getDeclaredConstructor(paramsClass);
        Object[] params = new Object[] { };
        try {
          return con.newInstance(params);
        } 
        catch (Exception e) {
          throw new RuntimeException(
            "Unable to instantiate class: " + name 
          );
        }
      } 
      catch (NoSuchMethodException eNSM) {
        throw new RuntimeException(
          "Unable to find constructor for class: " + name);
      }
    } 
    catch (ClassNotFoundException eCNF) {
      throw new RuntimeException("Unable to find class: " + name);
    }
  } // private static Object _createInterface(name)

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

}

