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
 * Test {@link Stem.addChildStem()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemAddChildGroup.java,v 1.1.2.1 2005-11-04 17:29:28 blair Exp $
 */
public class TestStemAddChildGroup extends TestCase {

  public TestStemAddChildGroup(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testAddChildGroupAtRoot() {
    try {
      GrouperSession s = GrouperSession.startSession(
        SubjectFinder.findById("GrouperSystem")
      );
      try {
        Stem root = StemFinder.findRootStem(s);
        try {
          Stem edu = root.addChildStem("edu", "educational");
          try {
            String extn         = "i2";
            String displayExtn  = "internet2";
            Group child = edu.addChildGroup(extn, displayExtn);
            Assert.assertNotNull("child !null", child);
            Assert.assertTrue("added child group", true);
            Assert.assertTrue(
              "child group instanceof Group", 
              child instanceof Group
            );
            Assert.assertTrue("child has uuid", !child.getUuid().equals(""));
          }
          catch (GroupAddException e) {
            Assert.fail("failed to add group: " + e.getMessage());
          }
        }
        catch (StemAddException e) {
          Assert.fail("failed to add stem: " + e.getMessage());
        }
      }
      catch (StemNotFoundException e) {
        Assert.fail("root stem not found: " + e.getMessage());
      }  
    }
    catch (SessionException e0) {
      Assert.fail(
        "failed to start session with good subject: " + e0.getMessage()
      );
    }
    catch (SubjectNotFoundException e1) {
      Assert.fail(
        "failed to start session with good subject: " + e1.getMessage()
      );
    }
  } // public void testAddChildGroupAtRoot()

}

