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
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup35.java,v 1.1 2006-06-16 18:42:20 blair Exp $
 */
public class TestGroup35 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestGroup35.class);

  public TestGroup35(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToGetViaGroupWhenComposite() {
    LOG.info("testFailToGetViaGroupWhenComposite");
    try {
      R           r     = R.populateRegistry(1, 3, 1);
      Group       gA    = r.getGroup("a", "a");
      Group       gB    = r.getGroup("a", "b");
      Group       gC    = r.getGroup("a", "c");
      Subject     subjA = r.getSubject("a");
      gB.addMember(subjA);
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Membership  ms    = MembershipFinder.findCompositeMembership(
        r.rs, gA, subjA
      );  
      // Fail
      try {
        ms.getViaGroup();
        Assert.fail("FAIL: got via group");
      }
      catch (GroupNotFoundException eGNF) {
        Assert.assertTrue("OK: failed to get !group via", true);
      }
      // Pass 
      try {
        Owner o = ms.getVia();
        Assert.assertTrue("via is composite", o instanceof Composite);
      }
      catch (OwnerNotFoundException eONF) {
        Assert.fail("FAIL: did not get via");
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToGetViaGroupWhenComposite()

}

