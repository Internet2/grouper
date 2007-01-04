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
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestStem9.java,v 1.3 2007-01-04 17:17:46 blair Exp $
 */
public class TestStem9 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem9.class);

  public TestStem9(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDeleteEmptyStemInNewSession() {
    LOG.info("testDeleteEmptyStemInNewSession");
    try {
      R       r     = R.populateRegistry(0, 0, 0);
      String  name  = r.ns.getName();
      Subject subj  = r.rs.getSubject();
      r.rs.stop();

      // Now reload and delete
      GrouperSession  s   = GrouperSession.start(subj);
      Stem            ns  = StemFinder.findByName(s, name);
      ns.delete();
      Assert.assertTrue("no lazy initialization error", true);
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteEmptyStemInNewSession()

}

