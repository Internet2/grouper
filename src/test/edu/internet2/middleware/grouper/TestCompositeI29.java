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
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeI29.java,v 1.4 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestCompositeI29 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeI29.class);

  public TestCompositeI29(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithTwoChildrenAndCompositeParent() {
    LOG.info("testAddUnionWithTwoChildrenAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 1);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject subjA = r.getSubject("a");

      a.addMember(subjA);
      b.addMember(subjA);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      e.addCompositeMember(CompositeType.INTERSECTION, c, d);

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjA", b.hasMember(subjA));

      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjA", c.hasMember(subjA));

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
  } // public void testAddUnionWithTwoChildrenAndCompositeParent()

}

