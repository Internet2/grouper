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
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import  junit.framework.*;

/**
 * Test {@link GroupFinder.findByName()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrFiFindByName.java,v 1.6 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestGrFiFindByName extends TestCase {

  public TestGrFiFindByName(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFindByName() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      Group found = GroupFinder.findByName(s, i2.getName());
      Assert.assertTrue("found a group", true);
      Assert.assertNotNull("found group !null", found);
      Assert.assertTrue("found instanceof Group", found instanceof Group);
      Assert.assertTrue("i2 equals found", i2.equals(found));
    }
    catch (GroupNotFoundException e) {
      Assert.fail("failed to find group");
    }
  } // public void testFindByName()

}

