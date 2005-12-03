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


/** 
 * Privilege resolution class.
 * Grouper configuration information.
 * <p />
 * @author  blair christensen.
 * @version $Id: PrivilegeResolver.java,v 1.15 2005-12-03 17:46:22 blair Exp $
 *     
*/
class PrivilegeResolver {

  // Private Class Variables
  private static PrivilegeResolver pr = null;


  // Private Instance Variables
  private AccessAdapter access;
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
    if (PrivilegeResolver.getInstance().hasPriv(s, g, subj, priv)) {
      can = true;
    }
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
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
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + ns.getName() + "'"
      );
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
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
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
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // protected void canOPTOUT(s, g, subj)

  protected void canPrivDispatch(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
System.err.println("DISPATCH/"+g.getName()+"/"+subj.getName()+"/"+priv.getName());
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
      this.canVIEW(s, g, subj );
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
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
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
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + ns.getName() + "'"
      );
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
    if (can == false) {
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
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
      throw new InsufficientPrivilegeException(
        s.getSubject().getId() + " does not have " + priv + " on '" 
        + g.getName() + "'"
      );
    }
  } // protected void canVIEW(s, g, subj)
  // TODO Can I remove s or subj?

  protected Set canVIEW(GrouperSession s, Set candidates) {
    Set             groups  = new LinkedHashSet();
    Iterator        iter    = candidates.iterator();
    while (iter.hasNext()) {
      Group g = (Group) iter.next();
      g.setSession(s);
      try {
        this.canVIEW(s, g, s.getSubject());
        groups.add(g);
      }
      catch (InsufficientPrivilegeException eIP) {
        // Ignore
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
            InsufficientPrivilegeException
  {
    GrouperSession.validate(s);
    this.access.grantPriv(s, g, subj, priv);
  } // protected void grantPriv(s, g, subj, priv)

  protected void grantPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException
  {
    GrouperSession.validate(s);
    this.naming.grantPriv(s, ns, subj, priv);
  } // protected void grantPriv(s, ns, subj, priv)

  protected boolean hasPriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    if (this._isRoot(subj)) {
      return true;
    }
    try {
      if (access.hasPriv(s, g, SubjectFinder.findAllSubject(), priv)) {
        return true;
      }
      return access.hasPriv(s, g, subj, priv);
    }
    catch (SchemaException ePNF) {
      // TODO Is this right?
      return false;
    }
  } // protected boolean hasPriv(s, g, subj, priv)

  protected boolean hasPriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
  {
    GrouperSession.validate(s);
    if (this._isRoot(subj)) {
      return true;
    }
    try {
      if (naming.hasPriv(s, ns, SubjectFinder.findAllSubject(), priv)) {
        return true;
      }
      return naming.hasPriv(s, ns, subj, priv);
    }
    catch (SchemaException ePNF) {
      // TODO This is *not* right
      return false;
    }
  } // protected boolean hasPriv(s, ns, subj, priv)

  protected void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  RevokePrivilegeException,
            InsufficientPrivilegeException
  {
    GrouperSession.validate(s);
    this.access.revokePriv(s, g, priv);
  } // protected void revokePriv(s, g, priv)

  protected void revokePriv(GrouperSession s, Stem ns, Privilege priv)
    throws  RevokePrivilegeException,
            InsufficientPrivilegeException
  {
    GrouperSession.validate(s);
    this.naming.revokePriv(s, ns, priv);
  } // protected void revokePriv(s, ns, priv)

  protected void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  RevokePrivilegeException,
            InsufficientPrivilegeException
  {
    GrouperSession.validate(s);
    this.access.revokePriv(s, g, subj, priv);
  } // protected void revokePriv(s, g, subj, priv)

  protected void revokePriv(
    GrouperSession s, Stem ns, Subject subj, Privilege priv
  )
    throws  RevokePrivilegeException,
            InsufficientPrivilegeException
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

  private boolean _isRoot(Subject subj) {
    if (
      (subj.getId().equals("GrouperSystem"))
      && (subj.getSource().getId().equals("grouper internal adapter"))
      && (subj.getType().getName().equals("application"))
    )
    {
      return true;
    }  
    return false;
  } // private boolean _isRoot(subj)

}

