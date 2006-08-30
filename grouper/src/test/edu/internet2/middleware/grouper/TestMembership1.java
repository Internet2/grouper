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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestMembership1.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 */
public class TestMembership1 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMembership1.class);

  public TestMembership1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testParentsAndChildren() {
    LOG.info("testParentsAndChildren");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");

      gA.addMember(subjA);
      gB.addMember( gA.toSubject() );
      gC.addMember( gB.toSubject() );

      Membership msSA_GA  = MembershipFinder.findImmediateMembership(
        r.rs, gA, subjA, Group.getDefaultList()
      );
      Assert.assertNotNull(msSA_GA);
      Membership msGA_GB  = MembershipFinder.findImmediateMembership(
        r.rs, gB, gA.toSubject(), Group.getDefaultList()
      );
      Assert.assertNotNull(msGA_GB);
      Membership msGB_GC  = MembershipFinder.findImmediateMembership(
        r.rs, gC, gB.toSubject(), Group.getDefaultList()
      );
      Assert.assertNotNull(msGB_GC);

      // subjA  -> gA
      // gA     -> gB
      // subjA  -> gA -> g (parent: gA -> gB)
      // gB     -> gC
      // gA     -> gB -> gC (parent: gB -> gC)
      // subjA  -> gA -> B -> gC (parent: gA -> gB)

      // Parents and children
      List mships = new ArrayList( msGB_GC.getChildMemberships() );
      T.amount("gB -> gC children", 1, mships.size());
      Membership childGC = (Membership) mships.get(0);
      Assert.assertTrue(
        "gA -> gC parent is gB -> gC", 
        childGC.getParentMembership().equals(msGB_GC)
      );

      mships = new ArrayList( msGA_GB.getChildMemberships() );
      T.amount("gA -> gB children", 2, mships.size());
      Membership childGB_0 = (Membership) mships.get(0);
      Membership childGB_1 = (Membership) mships.get(1);
      Assert.assertTrue(
        "subjA -> gB parent is gA -> gB",
        childGB_0.getParentMembership().equals(msGA_GB)
      );
      Assert.assertTrue(
        "subjA -> gC parent is gA -> gB",
        childGB_1.getParentMembership().equals(msGA_GB)
      );

      mships = new ArrayList( msSA_GA.getChildMemberships() );
      T.amount("subjA -> gA children", 0, mships.size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testParentsAndChildren()

}

