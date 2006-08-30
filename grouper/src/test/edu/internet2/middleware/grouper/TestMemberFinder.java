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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test {@link MemberFinder}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMemberFinder.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 */
public class TestMemberFinder extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestMemberFinder.class);


  public TestMemberFinder(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testFindBySubjectBadSubject() {
    LOG.info("testFindBySubjectBadSubject");
    Helper.getMemberBySubjectBad(
      SessionHelper.getRootSession(), null
    );
    Assert.assertTrue("failed to find bad member", true);
  } // public void testFindBySubjectBadSubject()

  public void testFindBySubject() {
    LOG.info("testFindBySubject");
    try {
      GrouperSession  s   = SessionHelper.getRootSession();
      String          id  = "GrouperSystem";
      Member          m   = Helper.getMemberBySubject(
        s, SubjectTestHelper.getSubjectById(id)
      );
      Assert.assertTrue("found member", true);
      if (s.getMember().equals(m)) {
        Assert.assertTrue("s.getMember().equals(m)", true);
      } 
      else {
        Assert.fail("s.getMember().equals(m)");
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testFindBySubject()

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

}

