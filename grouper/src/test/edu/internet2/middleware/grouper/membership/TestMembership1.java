/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package edu.internet2.middleware.grouper.membership;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestMembership1.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestMembership1 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembership1.class);

  public TestMembership1(String name) {
    super(name);
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
        r.rs, gA, subjA, Group.getDefaultList(), true
      );
      Assert.assertNotNull(msSA_GA);
      Membership msGA_GB  = MembershipFinder.findImmediateMembership(
        r.rs, gB, gA.toSubject(), Group.getDefaultList(), true
      );
      Assert.assertNotNull(msGA_GB);
      Membership msGB_GC  = MembershipFinder.findImmediateMembership(
        r.rs, gC, gB.toSubject(), Group.getDefaultList(), true
      );
      Assert.assertNotNull(msGB_GC);

      // subjA  -> gA
      // gA     -> gB
      // subjA  -> gB (parent: gA -> gB)
      // gB     -> gC
      // gA     -> gC (parent: gB -> gC)
      // subjA  -> gC (parent: gA -> gC)

      // Parents and children
      List mships = new ArrayList( msGB_GC.getChildMemberships() );
      T.amount("gB -> gC children", 1, mships.size());
      Membership childGC = (Membership) mships.get(0);
      Assert.assertTrue(
        "gA -> gC parent is gB -> gC", 
        childGC.getParentMembership().equals(msGB_GC)
      );

      mships = new ArrayList( msGA_GB.getChildMemberships() );
      T.amount("gA -> gB children", 1, mships.size());
      Membership childGB_0 = (Membership) mships.get(0);
      Assert.assertTrue(
        "subjA -> gB parent is gA -> gB",
        childGB_0.getParentMembership().equals(msGA_GB)
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

