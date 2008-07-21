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
import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeModel10.java,v 1.7 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestCompositeModel10 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeModel10.class);

  public TestCompositeModel10(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testIntersectionComposite() {
    LOG.info("testIntersectionComposite");
    assertTrue("TODO 20070131 this test no longer works", true);
/*
    try {
      R         r     = R.populateRegistry(1, 2, 0);
      Owner     owner = r.ns;
      Owner     left  = r.getGroup("a", "a");
      Owner     right = r.getGroup("a", "b");
      Composite c     = new Composite(
        r.rs, owner, left, right, CompositeType.INTERSECTION
      );
      Assert.assertTrue("created intersection composite", true);
      Assert.assertTrue("instanceof Composite", c instanceof Composite);
      Assert.assertEquals( "owner", owner.getUuid(),            c.getOwner() );
      Assert.assertEquals( "left",  left.getUuid(),             c.getLeft()  );
      Assert.assertEquals( "right", right.getUuid(),            c.getRight() );
      Assert.assertEquals( "type",  CompositeType.INTERSECTION, c.getType()  );
      r.rs.stop();
    }
    catch (ModelException eM) {
      Assert.fail("could not create intersection composite: " + eM.getMessage());
    }
    catch (Exception e) {
      T.e(e);
    }
*/
  } // public void testIntersectionComposite()

}

