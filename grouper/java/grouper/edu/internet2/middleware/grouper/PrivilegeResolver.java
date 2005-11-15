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
 * @version $Id: PrivilegeResolver.java,v 1.3 2005-11-15 18:23:56 blair Exp $
 *     
*/
class PrivilegeResolver {

  // Private Class Variables
  private static PrivilegeResolver pr = null;


  // Private Instance Variables
  private AccessPrivilege access;
  private NamingPrivilege naming;


  // Constructors
  private PrivilegeResolver() {
    // nothing
  } // private PrivilegeResolver()


  // Protected Class Methods
  protected static PrivilegeResolver getInstance() {
    if (pr == null) {
      pr = new PrivilegeResolver();
      pr.access = (AccessPrivilege) _createInterface(
        GrouperConfig.getInstance().getProperty("interface.access")
      );
      pr.naming = (NamingPrivilege) _createInterface(
        GrouperConfig.getInstance().getProperty("interface.naming")
      );
    }
    return pr;
  } // protected static PrivilegeResolver getInstance()


  // Protected Instance Methods
  protected boolean hasPriv(
    GrouperSession s, Group g, Subject subj, String priv
  )
  {
    if (this._isRoot(subj)) {
      return true;
    }
    try {
      return access.hasPriv(s, g, subj, priv);
    }
    catch (PrivilegeNotFoundException ePNF) {
      // TODO Is this right?
      return false;
    }
  } // protected boolean hasPriv(s, g, subj, priv)

  protected boolean hasPriv(
    GrouperSession s, Stem ns, Subject subj, String priv
  )
  {
    if (this._isRoot(subj)) {
      return true;
    }
    try {
      return naming.hasPriv(s, ns, subj, priv);
    }
    catch (PrivilegeNotFoundException ePNF) {
      // TODO This is *not* right
      return false;
    }
  } // protected boolean hasPriv(s, ns, subj, priv)

  protected Set getPrivs(
    GrouperSession s, Group g, Subject subj
  )
  {
    return access.getPrivs(s, g, subj);
  } // protected boolean hasPriv(s, ns, subj, priv)

  protected Set getPrivs(
    GrouperSession s, Stem ns, Subject subj
  )
  {
    return naming.getPrivs(s, ns, subj);
  } // protected boolean hasPriv(s, ns, subj, priv)


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

