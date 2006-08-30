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
 * @version $Id: TestStem3.java,v 1.3 2006-08-30 18:35:37 blair Exp $
 */
public class TestStem3 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem3.class);

  public TestStem3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToDeleteStemWithoutSTEM() {
    LOG.info("testFailToDeleteStemWithoutSTEM");
    try {
      R               r     = R.populateRegistry(1, 0, 1);
      Subject         subj  = r.getSubject("a");
      GrouperSession  s     = GrouperSession.start(subj);
      r.ns.setSession(s);
      r.ns.delete();
      s.stop();
      r.rs.stop();
      Assert.fail("deleted stem without STEM");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("OK: failed to delete stem without STEM", true);
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToDeleteStemWithoutSTEM()

}

