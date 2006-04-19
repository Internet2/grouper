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

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestStem0.java,v 1.1.2.2 2006-04-19 22:55:14 blair Exp $
 */
public class TestStem0 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem0.class);

  public TestStem0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDeleteEmptyStem() {
    LOG.info("testDeleteEmptyStem");
    try {
      R r = R.populateRegistry(0, 0, 0);
      r.ns.delete();
      Assert.assertTrue("deleted stem", true);
      r.rs.stop();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testDeleteEmptyStem()

}

