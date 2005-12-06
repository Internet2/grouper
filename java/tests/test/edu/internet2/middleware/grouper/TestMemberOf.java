/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * @version $Id: TestMemberOf.java,v 1.1 2005-12-06 05:35:03 blair Exp $
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


  public TestMemberOf(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    Db.refreshDb();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testNoEffMshipsDefaultList() {
    LOG.info("testNoEffMshipsDefaultList");
    MembershipHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2, subj0, "members");
    MembershipHelper.testNumMship(i2, "members", 1, 1, 0);
  } // public void testNoEffMshipsDefaultList()    

  public void testNoEffMshipsCustomList() {
    LOG.info("testNoEffMshipsCustomList");
    MembershipHelper.testNumMship(i2, "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2, "readers", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "readers");
    MembershipHelper.testImm(s, i2, subj0, "readers");
    MembershipHelper.testNumMship(i2, "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2, "readers", 1, 1, 0);
  } // public void testNoEffMshipsCustomList()    

  public void testHasMemberDefaultList() {
    LOG.info("testHasMemberDefaultList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2, subj0, "members");
    MembershipHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testImm(s, i2   , subj0         , "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testEff(s, uofc , subj0         , "members", i2, 1);
    MembershipHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "members", 2, 1, 1);
  } // public void testHasMemberDefaultList()

  public void testHasMemberCustomList() {
    LOG.info("testHasMemberCustomList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "readers");
    MembershipHelper.testImm(s, i2, subj0, "readers");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testImm(s, i2   , subj0         , "readers");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 1, 1, 0);
    MembershipHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
  } // public void testHasMemberCustomList()

  public void testIsMemberDefaultList() {
    LOG.info("testIsMemberDefaultList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2   , subj0         , "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testEff(s, uofc , subj0         , "members", i2, 1);
    MembershipHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "members", 2, 1, 1);
  } // public void testIsMemberDefaultList()

  public void testIsMemberCustomList() {
    LOG.info("testIsMemberCustomList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "readers");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2   , subj0         , "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 2, 1, 1);
  } // public void testIsMemberCustomList()

  public void testIsMemberDefaultListHasMemberDefaultList() {
    LOG.info("testIsMemberDefaultListHasMemberDefaultList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2   , subj0          , "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject() , "members"); 
    MembershipHelper.testEff(s, uofc,  subj0          , "members", i2, 1);
    MembershipHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "members", 2, 1, 1);
  } // public void testIsMemberDefaultListHasMemberDefaultList()

  public void testIsMemberDefaultListHasMemberCustomList() {
    LOG.info("testIsMemberDefaultListHasMemberCustomList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 1, 1, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "readers");
    MembershipHelper.testImm(s, i2   , subj0         , "readers");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 1, 1, 0);
    MembershipHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
  } // public void testIsMemberDefaultListHasMemberCustomList()

  public void testIsMemberCustomListHasMemberCustomList() {
    LOG.info("testIsMemberCustomListHasMemberCustomList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 0, 0, 0);
    GroupHelper.addMember(uofc, i2.toSubject(), "readers");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "readers", 1, 1, 0);
    GroupHelper.addMember(i2, subj0, "readers");
    MembershipHelper.testImm(s, i2   , subj0         , "readers");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "readers", 1, 1, 0);
  } // public void testIsMemberCustomListHasMemberCustomList()
}

