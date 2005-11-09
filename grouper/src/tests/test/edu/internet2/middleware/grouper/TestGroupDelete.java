/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

/**
 * Test {@link Group.delete()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupDelete.java,v 1.1.2.2 2005-11-09 23:20:03 blair Exp $
 */
public class TestGroupDelete extends TestCase {

  public TestGroupDelete(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testGroupDelete() {
    Stem  root  = StemHelper.getRootStem(
      Helper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      i2.delete();
      Assert.assertTrue("group deleted", true);
    }
    catch (GroupDeleteException e0) {
      Assert.fail("failed to delete group: " + e0.getMessage());
    }
    catch (InsufficientPrivilegeException e1) {
      Assert.fail("not privileged to delete group: " + e1.getMessage());
    }
  } // public void testGroupDelete()

}

