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
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestMemberFinder_FindBySubject.java,v 1.8 2008-11-12 09:05:53 mchyzer Exp $
 * @since   1.2.0
 */
public class TestMemberFinder_FindBySubject extends GrouperTest {
  private static final Log LOG = GrouperUtil.getLog(TestMemberFinder_FindBySubject.class);
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

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(TestMemberFinder_FindBySubject.class);
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
    } catch (NullPointerException npe) {
      //ok
    } catch (Exception e) {
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

} // public class TestMemberFinder_FindBySubject

