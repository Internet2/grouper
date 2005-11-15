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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;

/**
 * Privilege helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: PrivHelper.java,v 1.2 2005-11-15 20:14:42 blair Exp $
 */
public class PrivHelper {

  // Protected Class Methods

  protected static void getPrivs(
    GrouperSession  s , Group g       , Subject subj  , int cnt, 
    boolean admin     , boolean optin , boolean optout,
    boolean read      , boolean update, boolean view
  ) 
  {
    try {
      Member  m   = MemberFinder.findBySubject(s, subj);
      String  msg = subj.getName() + " has " + cnt + " privs on " + g.getName();
      Assert.assertTrue(
        msg, g.getPrivs(subj).size() == cnt
      );
      Assert.assertTrue(
        msg, m.getPrivs(g).size()    == cnt
      );
      hasPriv(g, subj, m, AccessPrivilege.ADMIN,  admin );
      hasPriv(g, subj, m, AccessPrivilege.OPTIN,  optin );
      hasPriv(g, subj, m, AccessPrivilege.OPTOUT, optout);
      hasPriv(g, subj, m, AccessPrivilege.READ,   read  );
      hasPriv(g, subj, m, AccessPrivilege.UPDATE, update);
      hasPriv(g, subj, m, AccessPrivilege.VIEW,   view  );
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void getPrivs(s, g, subj, cnt, admin, optin, optout, read, update view)


  protected static void getPrivs(
    GrouperSession s, Stem ns       , Subject subj, 
    int cnt         , boolean create, boolean stem
  ) 
  {
    try {
      Member  m   = MemberFinder.findBySubject(s, subj);
      String  msg = subj.getName() + " has " + cnt + " privs on " + ns.getName();
      Assert.assertTrue(
        msg, ns.getPrivs(subj).size() == cnt
      );
      Assert.assertTrue(
        msg, m.getPrivs(ns).size()    == cnt
      );
      hasPriv(ns, subj, m, NamingPrivilege.CREATE, create);
      hasPriv(ns, subj, m, NamingPrivilege.STEM,   stem);
    }
    catch (MemberNotFoundException eMNF) {
      Assert.fail(eMNF.getMessage());
    }
  } // protected static void getPrivs(s,ns, subj, cnt, create, stem)

  protected static void hasPriv(
    Group g, Subject subj, Member m, Privilege priv, boolean has
  ) 
  {
    String msg  = subj.getName();
    if (has == true) {
      msg += " has ";
    }
    else {
      msg += " does not have ";
    }
    if      (priv.equals(AccessPrivilege.ADMIN)) {
      Assert.assertTrue(msg + " ADMIN",   g.hasAdmin(subj)  == has  );
      Assert.assertTrue(msg + " ADMIN",   m.hasAdmin(g)     == has  );
    }
    else if (priv.equals(AccessPrivilege.OPTIN)) {
      Assert.assertTrue(msg + " OPTIN",   g.hasOptin(subj)  == has  );
      Assert.assertTrue(msg + " OPTIN",   m.hasOptin(g)     == has  );
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      Assert.assertTrue(msg + " OPTOUT",  g.hasOptout(subj) == has  );
      Assert.assertTrue(msg + " OPTOUT",  m.hasOptout(g)    == has  );
    }
    else if (priv.equals(AccessPrivilege.READ)) {
      Assert.assertTrue(msg + " READ",    g.hasRead(subj)   == has  );
      Assert.assertTrue(msg + " READ",    m.hasRead(g)      == has  );
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      Assert.assertTrue(msg + " UPDATE",  g.hasUpdate(subj) == has  );
      Assert.assertTrue(msg + " UPDATE",  m.hasUpdate(g)    == has  );
    }
    else if (priv.equals(AccessPrivilege.VIEW)) {
      Assert.assertTrue(msg + " VIEW",    g.hasView(subj)   == has  );
      Assert.assertTrue(msg + " VIEW",    m.hasView(g)      == has  );
    }
    else {
      Assert.fail("unable test priv '" + priv + "'");
    }
  } // protected static void hasPriv(g, subj, m, priv, has)

  protected static void hasPriv(
    Stem ns, Subject subj, Member m, Privilege priv, boolean has
  ) 
  {
    String msg  = subj.getName();
    if (has == true) {
      msg += " has ";
    }
    else {
      msg += " does not have ";
    }
    if      (priv.equals(NamingPrivilege.CREATE)) {
      Assert.assertTrue(msg + " CREATE",  ns.hasCreate(subj)  == has  );
      Assert.assertTrue(msg + " CREATE",  m.hasCreate(ns)     == has  );
    }
    else if (priv.equals(NamingPrivilege.STEM)) {
      Assert.assertTrue(msg + " STEM",    ns.hasStem(subj)    == has  );
      Assert.assertTrue(msg + " STEM",    m.hasStem(ns)       == has  );
    }
    else {
      Assert.fail("unable test priv '" + priv + "'");
    } 
  } // protected static void hasPriv(ns, subj, m, priv, has)

}

