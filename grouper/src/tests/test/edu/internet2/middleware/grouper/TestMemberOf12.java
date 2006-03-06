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
 * @version $Id: TestMemberOf12.java,v 1.1 2006-03-06 20:21:50 blair Exp $
 */
public class TestMemberOf12 extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestMemberOf12.class); 


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
  


  public TestMemberOf12(String name) {
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

  public void testHalfLoop() {
    LOG.info("testHalfLoop");

    MembershipHelper.testNumMship(i2,   "members",  0, 0, 0);
    MembershipHelper.testNumMship(uofc, "members",  0, 0, 0);

    // 0 -> I2^M
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testNumMship(uofc, "members",  0, 0, 0);

    // I2 -> UOFC^M
    GroupHelper.addMember(uofc, i2.toSubject(), "members");

    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testNumMship(i2,   "members",  1, 1, 0);

    MembershipHelper.testImm(s, uofc, i2.toSubject(), "members");
    MembershipHelper.testEff(s, uofc, subj0, "members", i2, 1);
    MembershipHelper.testNumMship(uofc, "members",  2, 1, 1);

    // UOFC -> I2C^M
    GroupHelper.addMember(i2, uofc.toSubject(), "members");

    MembershipHelper.testImm(s, i2 , subj0, "members");
    MembershipHelper.testImm(s, i2 , uofc.toSubject(), "members");
    MembershipHelper.testEff(s, i2, i2.toSubject(), "members", uofc, 1);
    MembershipHelper.testEff(s, i2, subj0, "members", i2, 2);
    MembershipHelper.testNumMship(i2,   "members",  4, 2, 2);

    MembershipHelper.testImm(s, uofc, i2.toSubject(), "members");
    MembershipHelper.testEff(s, uofc, subj0, "members", i2, 1);
    MembershipHelper.testEff(s, uofc, uofc.toSubject(), "members", i2, 1);
    MembershipHelper.testEff(s, uofc, i2.toSubject(), "members", uofc, 2);
    MembershipHelper.testEff(s, uofc, subj0, "members", i2, 3);
    MembershipHelper.testNumMship(uofc, "members",  5, 1, 4);

  } // public void testLoop()

}

