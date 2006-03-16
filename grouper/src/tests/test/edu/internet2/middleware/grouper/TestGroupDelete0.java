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
 * Test {@link Group.delete()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupDelete0.java,v 1.1 2006-03-16 20:59:57 blair Exp $
 */
public class TestGroupDelete0 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupDelete0.class);


  public TestGroupDelete0(String name) {
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

  public void testGroupDelete() {
    LOG.info("testGroupDelete");
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      i2.delete();
      Assert.assertTrue("group deleted", true);
    }
    catch (Exception e) {
      Assert.fail("failed to delete group: " + e.getMessage());
    }
  } // public void testGroupDelete()

}

