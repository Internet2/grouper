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

/**
 * @author  blair christensen.
 * @version $Id: TestMember1.java,v 1.6 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestMember1 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestMember1.class);

  public TestMember1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailCanCreateWhenNoPriv() {
    LOG.info("testFailCanCreateWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 0, 1);
      Stem    a   = r.getStem("a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot create", m.canCreate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanCreateWhenNoPriv()

}

