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
import  java.io.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link StemFinder.findRootStem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStFiFindRootStem.java,v 1.1.2.1 2005-11-01 18:01:38 blair Exp $
 */
public class TestStFiFindRootStem extends TestCase {

  public TestStFiFindRootStem(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFindRootStem() {
    try {
      GrouperSession  s = GrouperSession.startSession(
        SubjectFinder.findById("GrouperSystem")
      );
      try {
        Stem root = StemFinder.findRootStem(s);
        Assert.assertTrue("found root stem", true);
        Assert.assertTrue("root isa Stem", root instanceof Stem);
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
  } // public void testFindRootStem()

}

