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
 * @version $Id: TestCompositeModel6.java,v 1.6 2007-01-04 17:17:46 blair Exp $
 */
public class TestCompositeModel6 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeModel6.class);

  public TestCompositeModel6(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailInvalidType() {
    LOG.info("testFailInvalidType");
    try {
      R r = R.populateRegistry(1, 2, 0);
      new Composite(
        r.rs, r.ns, r.getGroup("a", "a"), r.getGroup("a", "b"), 
        CompositeType.getInstance("invalid type")
      );
      r.rs.stop();
      Assert.fail("created composite with null type");
    }
    catch (ModelException eM) {
      Assert.assertTrue("OK: did not create composite with invalid type", true);
      T.string("error message", E.COMP_T, eM.getMessage());
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailInvalidType()

}

