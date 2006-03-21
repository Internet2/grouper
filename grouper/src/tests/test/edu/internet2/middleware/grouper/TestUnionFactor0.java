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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestUnionFactor0.java,v 1.1 2006-03-21 18:36:45 blair Exp $
 */
public class TestUnionFactor0 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestUnionFactor0.class);

  public TestUnionFactor0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testFailToAddUnionFactorWithCircularLeftNode() {
    LOG.info("testFailToAddUnionFactorWithCircularLeftNode");
    try {
      R r = R.createOneStemAndTwoGroups();
      GrouperSession  s = SessionHelper.getRootSession();
      try {
        r.i2.addFactor( new UnionFactor(r.i2, r.uc) );
        Assert.fail("FAIL: added recursive left node");
      }
      catch (FactorAddException eFA) {
        Assert.assertTrue("OK: expected failure", true);
      } 
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("FAIL: " + e.getMessage());
    }
  } // public void testFailToAddUnionFactorWithCircularLeftNode()

}

