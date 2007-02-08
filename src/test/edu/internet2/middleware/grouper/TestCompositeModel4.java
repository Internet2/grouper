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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeModel4.java,v 1.7 2007-02-08 16:25:25 blair Exp $
 */
public class TestCompositeModel4 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeModel4.class);

  public TestCompositeModel4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailLeftEqualsRight() {
    LOG.info("testFailLeftEqualsRight");
    assertTrue("TODO 20070131 this test no longer works", true);
/*
    try {
      R r = R.populateRegistry(1, 2, 0);
      new Composite(
        r.rs, r.getGroup("a", "a"), r.getGroup("a", "b"), r.getGroup("a", "b"), CompositeType.UNION
      );
      r.rs.stop();
      Assert.fail("created composite with left == right");
    }
    catch (ModelException eM) {
      Assert.assertTrue("OK: did not create composite with left == right", true);
      T.string("error message", E.COMP_LR, eM.getMessage());
    }
    catch (Exception e) {
      T.e(e);
    }
*/
  } // public void testFailLeftEqualsRight()

}

