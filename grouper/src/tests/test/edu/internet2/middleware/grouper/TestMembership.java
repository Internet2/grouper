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
 * Test {@link Membership}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMembership.java,v 1.3 2005-12-13 18:00:57 blair Exp $
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
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testNoParentAndNoChildMemberships() {
    LOG.info("testNoParentAndNoChildMemberships");
    MembershipHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2, subj0, "members");
    MembershipHelper.testNumMship(i2, "members", 1, 1, 0);
    Membership imm = MembershipHelper.getImm(s, i2, subj0, "members");
    MembershipHelper.testNoParent(imm);
    MembershipHelper.testNoChildren(imm);
  } // public void testNoParentAndNoChildMemberships()

  public void testParentAndChildMemberships() {
    LOG.info("testParentAndChildMemberships");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);

    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 1, 1, 0);
    Membership uofc_i2 = MembershipHelper.getImm(s, uofc, i2.toSubject(), "members");
    MembershipHelper.testNoParent(uofc_i2);
    MembershipHelper.testNoChildren(uofc_i2);

    GroupHelper.addMember(i2, subj0, "members");
    MembershipHelper.testImm(s, i2   , subj0          , "members");
    MembershipHelper.testImm(s, uofc , i2.toSubject() , "members"); 
    MembershipHelper.testEff(s, uofc,  subj0          , "members", i2, 1);
    MembershipHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "members", 2, 1, 1);
    Membership i2_subj0 = MembershipHelper.getImm(s, i2, subj0, "members");
    MembershipHelper.testNoParent(i2_subj0);
    MembershipHelper.testNoChildren(i2_subj0);
    Set uofc_i2_subj0 = MembershipHelper.getEff(s, uofc, subj0, "members", 1, i2);
    Set children      = new LinkedHashSet();
    Iterator iter = uofc_i2_subj0.iterator();
    while (iter.hasNext()) {
      Membership eff = (Membership) iter.next();
      MembershipHelper.testParent(uofc_i2, eff);
      children.add(eff);
    }
    MembershipHelper.testChildren(uofc_i2, children);
  } // public void testParentAndChildMemberships()

}

