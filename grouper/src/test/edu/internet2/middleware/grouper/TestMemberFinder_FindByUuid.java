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
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestMemberFinder_FindByUuid.java,v 1.5 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.0
 */
public class TestMemberFinder_FindByUuid extends GrouperTest {
  private static final Log LOG = GrouperUtil.getLog(TestMemberFinder_FindByUuid.class);
  public TestMemberFinder_FindByUuid(String name) {
    super(name);
  }
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }
  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // TESTS //

  public void testFailToFindByNullUuid() {
    LOG.info("testFailToFindByNullUuid");
    try {
      MemberFinder.findByUuid(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        null, true
      );
      fail("found member by null uuid");
    }
    catch (MemberNotFoundException eMNF) {
      assertTrue("OK: did not find member by null uuid", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToFindByNullUuid()

  public void testFindGrouperSystemByUuid() {
    LOG.info("testFindGrouperSystemByUuid");
    try {
      Subject         root  = SubjectFinder.findRootSubject();
      GrouperSession  s     = GrouperSession.start(root);
      Member          m     = MemberFinder.findBySubject(s, root, true);
      MemberFinder.findByUuid( s, m.getUuid(), true );
      assertTrue("OK: found member by uuid", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperSystemByUuid()

} // public class TestMemberFinder_FindByUuid

