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
 * @version $Id: TestCompositeI30.java,v 1.5 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestCompositeI30 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestCompositeI30.class);

  public TestCompositeI30(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDelUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testDelUnionWithTwoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");

      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);
      c.deleteCompositeMember();

      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      Assert.assertFalse(  "b !isComposite" , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));

      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 0, c.getMembers().size());

      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      T.amount("d members", 0, d.getMembers().size());

      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      T.amount("e members", 0, e.getMembers().size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithTwoChildrenAndCompositeParent()

}

