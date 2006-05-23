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
 * @version $Id: TestSubject5.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class TestSubject5 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestSubject5.class);

  public TestSubject5(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToFindByIdAndTypeAndSource() {
    LOG.info("testFailToFindByIdAndTypeAndSource");
    try {
      R       r     = R.populateRegistry(0, 0, 0);
      String  id    = "a";
      String  type  = "person";
      String  sa    = "jdbc";
      Subject subj  = SubjectFinder.findById(id, type, sa);
      Assert.fail("found non-existent subject: " + subj);
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.assertTrue("OK: failed to find non-existent subject", true);
    }
  } // public void testFailToFindByIdAndTypeAndSource()

}

