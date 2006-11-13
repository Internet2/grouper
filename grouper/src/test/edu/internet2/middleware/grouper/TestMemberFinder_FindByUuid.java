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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestMemberFinder_FindByUuid.java,v 1.1 2006-11-13 16:47:50 blair Exp $
 * @since   1.2.0
 */
public class TestMemberFinder_FindByUuid extends GrouperTest {
  private static final Log LOG = LogFactory.getLog(TestMemberFinder_FindByUuid.class);
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
        null
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
      Member          m     = MemberFinder.findBySubject(s, root);
      MemberFinder.findByUuid( s, m.getUuid() );
      assertTrue("OK: found member by uuid", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperSystemByUuid()

} // public class TestMemberFinder_FindByUuid

