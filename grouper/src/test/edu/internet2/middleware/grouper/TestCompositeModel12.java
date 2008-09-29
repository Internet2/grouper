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
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeModel12.java,v 1.10 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestCompositeModel12 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestCompositeModel12.class);

  public TestCompositeModel12(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailOwnerNotGroupOrStem() {
    LOG.info("testFailOwnerNotGroupOrStem");
    assertTrue("TODO 20070131 this test no longer works", true);
/*
    try {
      R r = R.populateRegistry(1, 2, 0);
      Composite c0 = new Composite(
        r.rs, r.ns, r.getGroup("a", "a"), r.getGroup("a", "b"), CompositeType.UNION
      );
      try {
        new Composite(
        r.rs, c0, r.getGroup("a", "a"), r.getGroup("a", "b"), CompositeType.UNION
        );
        Assert.fail("created composite with composite as owner");
      }
      catch (ModelException eM) {
        Assert.assertTrue("OK: cannot create composites with !(group|stem) as owner", true);
        T.string("error message", E.COMP_OC, eM.getMessage());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
*/
  } // public void testFailOwnerNotGroupOrStem()

}

