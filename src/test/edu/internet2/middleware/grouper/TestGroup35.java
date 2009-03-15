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
import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup35.java,v 1.7 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestGroup35 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestGroup35.class);

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
      Membership  ms    = MembershipFinder.findCompositeMembership(r.rs, gA, subjA, true);  
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
        Composite o = ms.getViaComposite();
        Assert.assertTrue("via is composite", o instanceof Composite);
      }
      catch (CompositeNotFoundException eONF) {
        Assert.fail("FAIL: did not get via");
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToGetViaGroupWhenComposite()

}

