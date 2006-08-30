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
 * @version $Id: TestCompositeModel9.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 */
public class TestCompositeModel9 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeModel9.class);

  public TestCompositeModel9(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testComplementComposite() {
    LOG.info("testComplementComposite");
    try {
      R         r     = R.populateRegistry(1, 2, 0);
      Owner     owner = r.ns;
      Owner     left  = r.getGroup("a", "a");
      Owner     right = r.getGroup("a", "b");
      Composite c     = new Composite(
        r.rs, owner, left, right, CompositeType.COMPLEMENT
      );
      Assert.assertTrue("created complement composite", true);
      Assert.assertTrue("instanceof Composite", c instanceof Composite);
      Assert.assertEquals("owner" , owner                   , c.getOwner());
      Assert.assertEquals("left"  , left                    , c.getLeft() );
      Assert.assertEquals("right" , right                   , c.getRight());
      Assert.assertEquals("type"  , CompositeType.COMPLEMENT, c.getType() );
      r.rs.stop();
    }
    catch (ModelException eM) {
      Assert.fail("could not create complement composite: " + eM.getMessage());
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testComplementComposite()

}

