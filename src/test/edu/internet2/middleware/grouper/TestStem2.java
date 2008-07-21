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
import  junit.framework.*;
import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestStem2.java,v 1.6 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestStem2 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem2.class);

  public TestStem2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToDeleteStemWithChildGroups() {
    LOG.info("testFailToDeleteStemWithChildGroups");
    try {
      R r = R.populateRegistry(1, 1, 0);
      Stem ns = r.getStem("a");
      ns.delete();
      Assert.fail("deleted stem with child groups");
      r.rs.stop();
    }
    catch (StemDeleteException eSD) {
      Assert.assertTrue("OK: failed to delete stem with child groups", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteStemWithChildGroups()

}

