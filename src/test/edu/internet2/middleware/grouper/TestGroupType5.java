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

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType5.java,v 1.7 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestGroupType5 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroupType5.class);

  public TestGroupType5(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToAddFieldToBaseAsNonRoot() {
    LOG.info("testFailToAddFieldToBaseAsNonRoot");
    try {
      R               r     = R.populateRegistry(1, 1, 1);
      GroupType       base  = GroupTypeFinder.find("base", true);
      Subject         subj  = r.getSubject("a");
      GrouperSession  s     = GrouperSession.start(subj);
      try {
        base.addList(s, "test", AccessPrivilege.VIEW, AccessPrivilege.UPDATE);
        Assert.fail("added field to base as non-root");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue("OK: not privileged to add field", true);
      }
      finally {
        s.stop();
        r.rs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToAddFieldToBaseAsNonRoot()

}

