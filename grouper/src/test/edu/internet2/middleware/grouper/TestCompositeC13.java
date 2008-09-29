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
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeC13.java,v 1.6 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestCompositeC13 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestCompositeC13.class);

  public TestCompositeC13(String name) {
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
      b.addMember(subjA);                                     // subjA
      c.addMember(subjB);                                     // subjB
      a.addCompositeMember(CompositeType.UNION, b, c);        // subjA, subjB
      d.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA
      T.amount("d members", 1, d.getMembers().size());
      Assert.assertTrue("d hasMember subjB", d.hasMember(subjB));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithCompositeChildAndNoParents()

}

