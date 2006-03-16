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

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Group.delete()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupDelete2.java,v 1.1 2006-03-16 20:59:57 blair Exp $
 */
public class TestGroupDelete2 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupDelete2.class);


  public TestGroupDelete2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testGroupDeleteWhenHasMemberViaTwoPaths() {
    LOG.info("testGroupDeleteWhenHasMemberViaTwoPaths");

    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Subject         subj0 = SubjectHelper.SUBJ0;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uofc");
    Group           ub    = StemHelper.addChildGroup(edu, "ub", "ub");
    Group           uw    = StemHelper.addChildGroup(edu, "uw", "uw");

    // 0 -> I2^M
    GroupHelper.addMember(i2, subj0, "members");

    // I2 -> UOFC^M
    GroupHelper.addMember(uofc, i2.toSubject(), "members");

    // I2 -> UB^M
    GroupHelper.addMember(ub, i2.toSubject(), "members");

    // UOFC -> UW^M
    GroupHelper.addMember(uw, uofc.toSubject(), "members");

    // UB -> UW^M
    GroupHelper.addMember(uw, ub.toSubject(), "members");

    try {
      i2.delete();
      Assert.assertTrue("group deleted", true);

      MembershipHelper.testNumMship(i2,   "members",  0, 0, 0);

      MembershipHelper.testNumMship(uofc, "members",  0, 0, 0);

      MembershipHelper.testNumMship(ub, "members",  0, 0, 0);

      MembershipHelper.testNumMship(uw, "members",  2, 2, 0);
      MembershipHelper.testImm(s, uw, uofc.toSubject(), "members");
      MembershipHelper.testImm(s, uw, ub.toSubject(), "members");
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGroupDeleteWhenHasMemberViaTwoPaths()

}

