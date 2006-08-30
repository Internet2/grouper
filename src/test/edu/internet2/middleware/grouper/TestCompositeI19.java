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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeI19.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 */
public class TestCompositeI19 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeI19.class);

  public TestCompositeI19(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithNoChildrenAndParent() {
    LOG.info("testAddUnionWithNoChildrenAndParent");
    try {
      R       r     = R.populateRegistry(1, 4, 0);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      // Parent Group
      Group   d     = r.getGroup("a", "d");
      d.addMember(c.toSubject());
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertFalse( "d !isComposite"  , d.isComposite()   );

      T.amount( "a members", 0, a.getMembers().size() );
      T.amount( "b members", 0, b.getMembers().size() );
      T.amount( "c members", 0, c.getMembers().size() );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has a:c",  d.hasMember(c.toSubject())  );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithNoChildrenAndParent()

}

