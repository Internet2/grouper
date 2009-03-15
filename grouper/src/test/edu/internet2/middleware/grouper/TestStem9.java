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
import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestStem9.java,v 1.6 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestStem9 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStem9.class);

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
      Stem            ns  = StemFinder.findByName(s, name, true);
      ns.delete();
      Assert.assertTrue("no lazy initialization error", true);
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteEmptyStemInNewSession()

}

