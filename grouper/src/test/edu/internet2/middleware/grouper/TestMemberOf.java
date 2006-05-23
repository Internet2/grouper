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
 * @version $Id: TestMemberOf.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class TestMemberOf extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestMemberOf.class); 


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
  


  public TestMemberOf(String name) {
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
    subj0 = SubjectTestHelper.SUBJ0;
    subj1 = SubjectTestHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testNoEffMshipsDefaultList() {
    LOG.info("testNoEffMshipsDefaultList");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2, "members", 1, 1, 0);
  } // public void testNoEffMshipsDefaultList()    

  public void testNoEffMshipsCustomList() {
    LOG.info("testNoEffMshipsCustomList");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2, "readers", 1, 1, 0);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    MembershipTestHelper.testImm(s, i2, subj0, "readers");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2, "readers", 2, 2, 0);
  } // public void testNoEffMshipsCustomList()    

  public void testHasMemberDefaultList() {
    LOG.info("testHasMemberDefaultList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, i2   , subj0         , "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testEff(s, uofc , subj0         , "members", i2, 1);
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 2, 1, 1);
  } // public void testHasMemberDefaultList()

  public void testHasMemberCustomList() {
    LOG.info("testHasMemberCustomList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    MembershipTestHelper.testImm(s, i2, subj0, "readers");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 2, 2, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, i2   , subj0         , "readers");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 2, 2, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
  } // public void testHasMemberCustomList()

  public void testIsMemberDefaultList() {
    LOG.info("testIsMemberDefaultList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2   , subj0         , "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testEff(s, uofc , subj0         , "members", i2, 1);
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 2, 1, 1);
  } // public void testIsMemberDefaultList()

  public void testIsMemberCustomList() {
    LOG.info("testIsMemberCustomList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
    PrivHelper.grantPriv(s, uofc, i2.toSubject(), AccessPrivilege.READ);
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 2, 2, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2   , subj0         , "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 3, 2, 1);
  } // public void testIsMemberCustomList()

  public void testIsMemberDefaultListHasMemberDefaultList() {
    LOG.info("testIsMemberDefaultListHasMemberDefaultList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2   , subj0          , "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject() , "members"); 
    MembershipTestHelper.testEff(s, uofc,  subj0          , "members", i2, 1);
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 2, 1, 1);
  } // public void testIsMemberDefaultListHasMemberDefaultList()

  public void testIsMemberDefaultListHasMemberCustomList() {
    LOG.info("testIsMemberDefaultListHasMemberCustomList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    MembershipTestHelper.testImm(s, i2   , subj0         , "readers");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 2, 2, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
  } // public void testIsMemberDefaultListHasMemberCustomList()

  public void testIsMemberCustomListHasMemberCustomList() {
    LOG.info("testIsMemberCustomListHasMemberCustomList");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 1, 1, 0);
    PrivHelper.grantPriv(s, uofc, i2.toSubject(), AccessPrivilege.READ);
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 2, 2, 0);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    MembershipTestHelper.testImm(s, i2   , subj0         , "readers");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(i2   , "readers", 2, 2, 0);
    MembershipTestHelper.testNumMship(uofc , "readers", 2, 2, 0);
  } // public void testIsMemberCustomListHasMemberCustomList()

  public void testHasMemberDefaultListIsMemberCustomListSelf() {
    LOG.info("testHasMemberDefaultListIsMemberCustomListSelf");
    MembershipTestHelper.testNumMship(i2  , "members" , 0, 0, 0);
    MembershipTestHelper.testNumMship(i2  , "readers" , 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2  , "members" , 1, 1, 0);
    MembershipTestHelper.testNumMship(i2  , "readers" , 1, 1, 0);
    PrivHelper.grantPriv(s, i2, i2.toSubject(), AccessPrivilege.READ);
    MembershipTestHelper.testNumMship(i2  , "members" , 1, 1, 0);
    MembershipTestHelper.testNumMship(i2  , "readers" , 3, 2, 1);
  } // public void testHasMemberDefaultListIsMemberCustomListSelf()

  public void testHasMemberViaTwoPaths() {
    LOG.info("testHasMemberViaTwoPaths");

    MembershipTestHelper.testNumMship(i2,   "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(uofc, "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(ub,   "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(uw,   "members",  0, 0, 0);

    // 0 -> I2^M
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(ub,   "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(uw,   "members",  0, 0, 0);

    // I2 -> UOFC^M
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc,  subj0, "members", i2, 1);
    MembershipTestHelper.testNumMship(ub,   "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(uw,   "members",  0, 0, 0);

    // I2 -> UB^M
    GroupHelper.addMember(ub, i2.toSubject(), "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc,  subj0, "members", i2, 1);
    MembershipTestHelper.testNumMship(ub, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, ub , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, ub,  subj0, "members", i2, 1);
    MembershipTestHelper.testNumMship(uw,   "members",  0, 0, 0);

    // UOFC -> UW^M
    GroupHelper.addMember(uw, uofc.toSubject(), "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc,  subj0, "members", i2, 1);
    MembershipTestHelper.testNumMship(ub, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, ub , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, ub,  subj0, "members", i2, 1);
    MembershipTestHelper.testNumMship(uw,   "members",  3, 1, 2);
    MembershipTestHelper.testImm(s, uw, uofc.toSubject(), "members");
    MembershipTestHelper.testEff(s, uw, i2.toSubject(), "members", uofc, 1);
    MembershipTestHelper.testEff(s, uw, subj0, "members", i2, 2);

    // UB -> UW^M
    GroupHelper.addMember(uw, ub.toSubject(), "members");

    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");

    MembershipTestHelper.testNumMship(uofc, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc,  subj0, "members", i2, 1);

    MembershipTestHelper.testNumMship(ub, "members",  2, 1, 1);
    MembershipTestHelper.testImm(s, ub , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, ub,  subj0, "members", i2, 1);

    MembershipTestHelper.testNumMship(uw, "members",  6, 2, 4);
    MembershipTestHelper.testImm(s, uw, uofc.toSubject(), "members");
    MembershipTestHelper.testImm(s, uw, ub.toSubject(), "members");
    MembershipTestHelper.testEff(s, uw, i2.toSubject(), "members", uofc, 1);
    MembershipTestHelper.testEff(s, uw, subj0, "members", i2, 2);
    MembershipTestHelper.testEff(s, uw, i2.toSubject(), "members", ub, 1);
    MembershipTestHelper.testEff(s, uw, subj0, "members", i2, 2);

  } // public void testHasMemberViaTwoPaths()

  public void testLoop() {
    LOG.info("testLoop");

    MembershipTestHelper.testNumMship(i2,   "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(uofc, "members",  0, 0, 0);

    // 0 -> I2^M
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  0, 0, 0);

    // 1 -> UOFC^M
    GroupHelper.addMember(uofc, subj1, "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, uofc , subj1, "members");

    // UOFC -> I2^M
    GroupHelper.addMember(i2, uofc.toSubject(), "members");

    MembershipTestHelper.testNumMship(i2,   "members",  3, 2, 1);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testImm(s, i2 , uofc.toSubject(), "members");
    MembershipTestHelper.testEff(s, i2, subj1, "members", uofc, 1);

    MembershipTestHelper.testNumMship(uofc, "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, uofc , subj1, "members");

    // I2 -> UOFC^M
    GroupHelper.addMember(uofc, i2.toSubject(), "members");

    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testImm(s, i2 , uofc.toSubject(), "members");
    MembershipTestHelper.testEff(s, i2, subj1, "members", uofc, 1);
    MembershipTestHelper.testEff(s, i2, i2.toSubject(), "members", uofc, 1);
    MembershipTestHelper.testEff(s, i2, subj0, "members", i2, 2);
    MembershipTestHelper.testEff(s, i2, uofc.toSubject(), "members", i2, 2);
    MembershipTestHelper.testEff(s, i2, subj1, "members", uofc, 3);
    MembershipTestHelper.testNumMship(i2,   "members",  7, 2, 5);

    MembershipTestHelper.testImm(s, uofc , subj1, "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc, subj0, "members", i2, 1);
    MembershipTestHelper.testEff(s, uofc, uofc.toSubject(), "members", i2, 1);
    MembershipTestHelper.testEff(s, uofc, subj1, "members", uofc, 2);
    MembershipTestHelper.testNumMship(uofc, "members",  5, 2, 3);

  } // public void testLoop()

  public void testHalfLoop() {
    LOG.info("testHalfLoop");

    MembershipTestHelper.testNumMship(i2,   "members",  0, 0, 0);
    MembershipTestHelper.testNumMship(uofc, "members",  0, 0, 0);

    // 0 -> I2^M
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);
    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(uofc, "members",  0, 0, 0);

    // I2 -> UOFC^M
    GroupHelper.addMember(uofc, i2.toSubject(), "members");

    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testNumMship(i2,   "members",  1, 1, 0);

    MembershipTestHelper.testImm(s, uofc, i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc, subj0, "members", i2, 1);
    MembershipTestHelper.testNumMship(uofc, "members",  2, 1, 1);

    // UOFC -> I2C^M
    GroupHelper.addMember(i2, uofc.toSubject(), "members");

    MembershipTestHelper.testImm(s, i2 , subj0, "members");
    MembershipTestHelper.testImm(s, i2 , uofc.toSubject(), "members");
    MembershipTestHelper.testEff(s, i2, i2.toSubject(), "members", uofc, 1);
    MembershipTestHelper.testEff(s, i2, subj0, "members", i2, 2);
    MembershipTestHelper.testNumMship(i2,   "members",  4, 2, 2);

    MembershipTestHelper.testImm(s, uofc, i2.toSubject(), "members");
    MembershipTestHelper.testEff(s, uofc, subj0, "members", i2, 1);
    MembershipTestHelper.testEff(s, uofc, uofc.toSubject(), "members", i2, 1);
    MembershipTestHelper.testEff(s, uofc, i2.toSubject(), "members", uofc, 2);
    MembershipTestHelper.testEff(s, uofc, subj0, "members", i2, 3);
    MembershipTestHelper.testNumMship(uofc, "members",  5, 1, 4);

  } // public void testLoop()
}

