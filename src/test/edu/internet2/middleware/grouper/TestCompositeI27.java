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
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeI27.java,v 1.2 2006-06-16 17:30:01 blair Exp $
 */
public class TestCompositeI27 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeI27.class);

  public TestCompositeI27(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithTwoChildrenAndParent() {
    LOG.info("testAddUnionWithTwoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");

      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      d.addMember(cSubj);

      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjA", b.hasMember(subjA));

      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));
      T.amount("c comp members", 1, c.getCompositeMembers().size());
      T.amount("c comp mships", 1, c.getCompositeMemberships().size());

      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has cSubj", d.hasMember(cSubj));
      Assert.assertTrue("d has subjA", d.hasMember(subjA));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoChildrenAndParent()

}

