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
 * Test {@link Membership}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMembership.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class TestMembership extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestMembership.class); 


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;


  public TestMembership(String name) {
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
    subj0 = SubjectTestHelper.SUBJ0;
    subj1 = SubjectTestHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testNoParentAndNoChildMemberships() {
    LOG.info("testNoParentAndNoChildMemberships");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2, "members", 1, 1, 0);
    Membership imm = MembershipTestHelper.getImm(s, i2, subj0, "members");
    MembershipTestHelper.testNoParent(imm);
    MembershipTestHelper.testNoChildren(imm);
  } // public void testNoParentAndNoChildMemberships()

  public void testParentAndChildMemberships() {
    LOG.info("testParentAndChildMemberships");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);

    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    Membership uofc_i2 = MembershipTestHelper.getImm(s, uofc, i2.toSubject(), "members");
    MembershipTestHelper.testNoParent(uofc_i2);
    MembershipTestHelper.testNoChildren(uofc_i2);

    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2   , subj0          , "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject() , "members"); 
    MembershipTestHelper.testEff(s, uofc,  subj0          , "members", i2, 1);
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 2, 1, 1);
    Membership i2_subj0 = MembershipTestHelper.getImm(s, i2, subj0, "members");
    MembershipTestHelper.testNoParent(i2_subj0);
    MembershipTestHelper.testNoChildren(i2_subj0);
    Set uofc_i2_subj0 = MembershipTestHelper.getEff(s, uofc, subj0, "members", 1, i2);
    Set children      = new LinkedHashSet();
    Iterator iter = uofc_i2_subj0.iterator();
    while (iter.hasNext()) {
      Membership eff = (Membership) iter.next();
      MembershipTestHelper.testParent(uofc_i2, eff);
      children.add(eff);
    }
    MembershipTestHelper.testChildren(uofc_i2, children);
  } // public void testParentAndChildMemberships()

  public void testEqualNotEqual() {
    LOG.info("testEqualNotEqual");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    GroupHelper.addMember(i2, subj1, "members");
    Membership imm0 = MembershipTestHelper.getImm(s, i2, subj0, "members");
    Membership imm1 = MembershipTestHelper.getImm(s, i2, subj1, "members");
    Membership imm2 = MembershipTestHelper.getImm(s, i2, subj0, "members");
    Assert.assertTrue("equal",      imm0.equals(imm2));
    Assert.assertTrue("not equal",  !imm0.equals(imm1));
  } // public void testEqualNotEqual()

}

