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
 * Test {@link GrouperNamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperNamingSTEM.java,v 1.3 2005-11-15 04:23:04 blair Exp $
 */
public class TestGrouperNamingSTEM extends TestCase {

  public TestGrouperNamingSTEM(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testDefaultPrivs() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.getRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    getPrivs(edu, s.getSubject(),       0, true,  true);
    getPrivs(edu, SubjectHelper.SUBJ0,  0, false, false);
    getPrivs(edu, SubjectHelper.SUBJ1,  0, false, false);
  } // public void testDefaultPrivs()

  protected void hasPriv(Stem ns, Subject subj, String priv, boolean has) {
    String msg = subj.getName();
    if (has == true) {
      msg += " has ";
      if      (priv.equals(Privilege.CREATE)) {
        Assert.assertTrue(msg + " CREATE",  ns.hasCreate(subj)  );
      }
      else if (priv.equals(Privilege.STEM)) {
        Assert.assertTrue(msg + " STEM",    ns.hasStem(subj)    );
      }
      else {
        Assert.fail("unable test priv '" + priv + "'");
      } 
    }
    else {
      msg += " does not have ";
      if      (priv.equals(Privilege.CREATE)) {
        Assert.assertFalse(msg + " CREATE",  ns.hasCreate(subj)  );
      }
      else if (priv.equals(Privilege.STEM)) {
        Assert.assertFalse(msg + " STEM",    ns.hasStem(subj)    );
      }
      else {
        Assert.fail("unable test priv '" + priv + "'");
      } 
    }
  } // protected void hasPriv(ns, subj, priv, has)

  protected void getPrivs(
    Stem ns, Subject subj, int cnt, boolean create, boolean stem
  ) 
  {
    String msg = subj.getName() + " has ";
    Assert.assertTrue(
      msg + cnt + " privs on " + ns.getName(),
      ns.getPrivs(subj).size() == cnt
    );
    hasPriv(ns, subj, Privilege.CREATE, create);
    hasPriv(ns, subj, Privilege.STEM,   stem);
  } // protected void getPrivs(ns, subj, cnt, create, stem)

}

