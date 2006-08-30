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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSubject3.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 */
public class TestSubject3 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestSubject3.class);

  public TestSubject3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetSource() {
    LOG.info("testGetSource");
    try {
      String  id  = "no such source";
      Source  sa  = SubjectFinder.getSource(id);
      Assert.fail("found invalid source: " + id);
    }
    catch (SourceUnavailableException eSU) {
      Assert.assertTrue("OK: failed to find invalid source", true);
    }
  } // public void testGetSource()

}

