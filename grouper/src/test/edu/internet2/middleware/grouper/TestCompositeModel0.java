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
 * @version $Id: TestCompositeModel0.java,v 1.5 2006-08-30 19:31:02 blair Exp $
 */
public class TestCompositeModel0 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeModel0.class);

  public TestCompositeModel0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailInvalidSession() {
    LOG.info("testFailInvalidSession");
    try {
      new Composite(null, null, null, null, null);
      Assert.fail("created composite with invalid session");
    }
    catch (ModelException eM) {
      Assert.assertTrue("OK: did not create composite with null session", true);  
      T.string("error message", E.SV_O, eM.getMessage());
    }
  } // public void testFailInvalidSession()

}

