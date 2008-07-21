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

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestMember8.java,v 1.4 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestMember8 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMember8.class);

  public TestMember8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testPassCanAdminWhenRoot() {
    LOG.info("testPassCanAdminWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can admin", m.canAdmin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanAdminWhenRoot()

}

