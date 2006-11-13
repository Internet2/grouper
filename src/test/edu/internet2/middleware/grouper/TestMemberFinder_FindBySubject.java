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
 * @version $Id: TestMemberFinder_FindBySubject.java,v 1.1 2006-11-13 16:47:50 blair Exp $
 * @since   1.2.0
 */
public class TestMemberFinder_FindBySubject extends GrouperTest {
  private static final Log LOG = LogFactory.getLog(TestMemberFinder_FindBySubject.class);
  public TestMemberFinder_FindBySubject(String name) {
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

  public void testFailToFindByNullSubject() {
    LOG.info("testFailToFindByNullSubject");
    try {
      MemberFinder.findBySubject(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        null
      );
      fail("found member by null subject");
    }
    catch (MemberNotFoundException eMNF) {
      assertTrue("OK: did not find member by null subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToFindByNullSubject()

  public void testFindGrouperSystemBySubject() {
    LOG.info("testFindGrouperSystemBySubject");
    try {
      MemberFinder.findBySubject(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        SubjectFinder.findRootSubject()
      );
      assertTrue("OK: found member by subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperSystemBySubject()

/*
  public void testFindByUuid() {
    LOG.info("testFindByUuid");
    try {
      GrouperSession  s   = SessionHelper.getRootSession();
      Member          mBS = MemberFinder.findBySubject(s, s.getSubject());
      Assert.assertNotNull("mBS !null", mBS);
      Assert.assertTrue("mBS instanceof Member", mBS instanceof Member);
      Member          mBU = MemberFinder.findByUuid(s, mBS.getUuid());
      Assert.assertNotNull("mBU !null", mBU);
      Assert.assertTrue("mBU instanceof Member", mBU instanceof Member);
      Assert.assertTrue("mBS == mBU", mBS.equals(mBU));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    } 
  } // public void testFindByUuid()
*/

} // public class TestMemberFinder_FindBySubject

