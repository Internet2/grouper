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
 * @version $Id: TestCompositeU13.java,v 1.1.2.1 2006-04-24 15:25:34 blair Exp $
 */
public class TestCompositeU13 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeU13.class);

  public TestCompositeU13(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithCompositeChildAndNoParents() {
    LOG.info("testAddUnionWithCompositeChildAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 4, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Field   f     = Group.getDefaultList();
      b.addMember(subjA);
      c.addMember(subjB);
      a.addCompositeMember(CompositeType.UNION, b, c);
      d.addCompositeMember(CompositeType.UNION, a, b);
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d hasMember subjA", d.hasMember(subjA));
      Assert.assertTrue("d hasMember subjB", d.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithCompositeChildAndNoParents()

}

