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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test memberOf calculations.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMemberOf10.java,v 1.1 2006-03-06 20:21:50 blair Exp $
 */
public class TestMemberOf10 extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestMemberOf10.class); 


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;
  private static Group          ub;
  private static Group          uw;
  


  public TestMemberOf10(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    ub    = StemHelper.addChildGroup(edu, "ub", "ub");
    uw    = StemHelper.addChildGroup(edu, "uw", "uw");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testHasMemberViaTwoPaths() {
    LOG.info("testHasMemberViaTwoPaths");

    MembershipHelper.testNumMship(i2,   "members",  0, 0, 0);
    MembershipHelper.testNumMship(uofc, "members",  0, 0, 0);
    MembershipHelper.testNumMship(ub,   "members",  0, 0, 0);
    MembershipHelper.testNumMship(uw,   "members",  0, 0, 0);

    // 0 -> I2^M
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testNumMship(uofc, "members",  0, 0, 0);
    MembershipHelper.testNumMship(ub,   "members",  0, 0, 0);
    MembershipHelper.testNumMship(uw,   "members",  0, 0, 0);

    // I2 -> UOFC^M
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipHelper.testEff(s, uofc,  subj0, "members", i2, 1);
    MembershipHelper.testNumMship(ub,   "members",  0, 0, 0);
    MembershipHelper.testNumMship(uw,   "members",  0, 0, 0);

    // I2 -> UB^M
    GroupHelper.addMember(ub, i2.toSubject(), "members");
    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipHelper.testEff(s, uofc,  subj0, "members", i2, 1);
    MembershipHelper.testNumMship(ub, "members",  2, 1, 1);
    MembershipHelper.testImm(s, ub , i2.toSubject(), "members");
    MembershipHelper.testEff(s, ub,  subj0, "members", i2, 1);
    MembershipHelper.testNumMship(uw,   "members",  0, 0, 0);

    // UOFC -> UW^M
    GroupHelper.addMember(uw, uofc.toSubject(), "members");
    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipHelper.testEff(s, uofc,  subj0, "members", i2, 1);
    MembershipHelper.testNumMship(ub, "members",  2, 1, 1);
    MembershipHelper.testImm(s, ub , i2.toSubject(), "members");
    MembershipHelper.testEff(s, ub,  subj0, "members", i2, 1);
    MembershipHelper.testNumMship(uw,   "members",  3, 1, 2);
    MembershipHelper.testImm(s, uw, uofc.toSubject(), "members");
    MembershipHelper.testEff(s, uw, i2.toSubject(), "members", uofc, 1);
    MembershipHelper.testEff(s, uw, subj0, "members", i2, 2);

    // UB -> UW^M
    GroupHelper.addMember(uw, ub.toSubject(), "members");

    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipHelper.testImm(s, i2 , subj0, "members");

    MembershipHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipHelper.testEff(s, uofc,  subj0, "members", i2, 1);

    MembershipHelper.testNumMship(ub, "members",  2, 1, 1);
    MembershipHelper.testImm(s, ub , i2.toSubject(), "members");
    MembershipHelper.testEff(s, ub,  subj0, "members", i2, 1);

    MembershipHelper.testNumMship(uw, "members",  6, 2, 4);
    MembershipHelper.testImm(s, uw, uofc.toSubject(), "members");
    MembershipHelper.testImm(s, uw, ub.toSubject(), "members");
    MembershipHelper.testEff(s, uw, i2.toSubject(), "members", uofc, 1);
    MembershipHelper.testEff(s, uw, subj0, "members", i2, 2);
    MembershipHelper.testEff(s, uw, i2.toSubject(), "members", ub, 1);
    MembershipHelper.testEff(s, uw, subj0, "members", i2, 2);

  } // public void testHasMemberViaTwoPaths()

}

