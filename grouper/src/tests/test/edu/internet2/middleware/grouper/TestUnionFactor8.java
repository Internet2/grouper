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
 * @version $Id: TestUnionFactor8.java,v 1.1 2006-03-23 20:21:08 blair Exp $
 */
public class TestUnionFactor8 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestUnionFactor8.class);

  public TestUnionFactor8(String name) {
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

  public void testFailToAddEmptyFactorAsUnionFactor() {
    LOG.info("testFailToAddEmptyFactorAsUnionFactor");
    try {
      R r = R.createOneStemAndFourGroups();
      try {
        r.i2.addFactor( new Factor() );
        Assert.fail("added empty factor");
      }
      catch (FactorAddException eFA) {
        Assert.assertTrue("OK: did not add empty factor", true);
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testFailToAddEmptyFactorAsUnionFactor()

}

