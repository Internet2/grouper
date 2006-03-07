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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestAddMember4.java,v 1.1 2006-03-07 19:27:05 blair Exp $
 */
public class TestAddMember4 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestAddMember4.class);

  public TestAddMember4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testNoListRecursion() {
    LOG.info("testNoListRecursion");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemHelper.findRootStem(s);
      Stem            ns    = StemHelper.addChildStem(root  , "ns_a", "stem a");
      Group           g     = StemHelper.addChildGroup(ns   , "g_a" , "group a");
      try {
        g.addMember(g.toSubject());
        Assert.fail("fail: MemberAddException not thrown");
      }
      catch (MemberAddException eMA) {
        Assert.assertTrue("pass: MemberAddException thrown", true);
      }
      finally {
        s.stop();
      }
    }
    catch (Exception e) {
      Assert.fail("fail: " + e.getMessage());
    }
  } // public void testNoListRecursion()

}

