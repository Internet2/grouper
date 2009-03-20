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

package edu.internet2.middleware.grouper.membership;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Membership}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMembership.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestMembership extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = GrouperUtil.getLog(TestMembership.class); 


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
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
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

  // @source  Gary Brown, 20051206, <6513d0390512060544q3fff7944vb8e1cedae7d4f92c@mail.gmail.com>
  // @status  fixed
  public void testBadEffMshipDepthCalcExposedByGroupDelete() {
    LOG.info("testBadEffMshipDepthCalcExposedByGroupDelete");
    try {
      Subject kebe = SubjectTestHelper.SUBJ0;
      Subject iata = SubjectTestHelper.SUBJ1;
      Subject iawi = SubjectTestHelper.SUBJ2;
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.0");
      Subject subj = SubjectFinder.findById("GrouperSystem", true);
      GrouperSession s = GrouperSession.start(subj);
      Stem root = StemFinder.findRootStem(s);
  		Stem qsuob = root.addChildStem("qsuob","qsuob");
      Group admins = qsuob.addChildGroup("admins","admins");
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.0");
      admins.addMember(kebe);
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.1");
      Group staff = qsuob.addChildGroup("staff","staff");
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.2");
      staff.addMember(iata);
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testNumMship(staff, "members", 1, 1, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.3");
      staff.addMember(iawi);
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.4");
      Group all_staff = qsuob.addChildGroup("all_staff","all staff");
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.5");
      all_staff.addMember(staff.toSubject());
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.6");
      admins.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, "admins");
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, "admins", 3, 2, 1);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.7");
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("create"));
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.8");
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("stem"));
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.9");
      staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, "admins");
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, "admins", 3, 2, 1);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
  
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testImm(s, staff, all_staff.toSubject(), "readers");
      MembershipTestHelper.testEff(s, staff, iata, "readers", staff, 2);
      MembershipTestHelper.testEff(s, staff, iawi, "readers", staff, 2);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.10");
      staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, "admins");
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, "admins", 3, 2, 1);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testNumMship(staff, "admins", 3, 2, 1);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11");
      all_staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, "admins");
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, "admins", 3, 2, 1);
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11.0");
  
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, all_staff, all_staff.toSubject(), "readers");
      MembershipTestHelper.testEff(s, all_staff, staff.toSubject(), "readers", all_staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iata, "readers", staff, 2);
      MembershipTestHelper.testEff(s, all_staff, iawi, "readers", staff, 2);
      MembershipTestHelper.testNumMship(all_staff, "readers", 5, 2, 3);
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11.1");
  
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testNumMship(staff, "admins", 3, 2, 1);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.12");
      all_staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, "admins");
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, "admins", 3, 2, 1);
  
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, all_staff, all_staff.toSubject(), "readers");
      MembershipTestHelper.testEff(s, all_staff, staff.toSubject(), "readers", all_staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iata, "readers", staff, 2);
      MembershipTestHelper.testEff(s, all_staff, iawi, "readers", staff, 2);
      MembershipTestHelper.testNumMship(all_staff, "readers", 5, 2, 3);
      MembershipTestHelper.testImm(s, all_staff, subj, "admins");
      MembershipTestHelper.testImm(s, all_staff, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, all_staff, kebe, "admins", admins, 1);
  
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipTestHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testNumMship(staff, "admins", 3, 2, 1);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.13");
      GroupHelper.delete(s, admins, admins.getName());
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.14");
      GroupHelper.delete(s, staff, staff.getName());
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.15");
      GroupHelper.delete(s, all_staff, all_staff.getName());
  
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("exception: " + e.getMessage());
    }
  } // public void testBadEffMshipDepthCalcExposedByGroupDelete() 

  public void testChildrenOfViaInMofDeletion() {
    LOG.info("testChildrenOfViaInMofDeletion");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   gA    = r.getGroup("a", "a");   
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
  
      gB.addMember( gA.toSubject() );
      // gA -> gB
  
      gC.addMember( gB.toSubject() );
      // gA -> gB
      // gB -> gC
      // gA -> gB -> gC
  
      gA.addMember(subjA);
      // gA -> gB
      // gB -> gC
      // gA -> gB -> gC
      // sA -> gA
      // sA -> gA -> gB
      // sA -> gA -> gB -> gC
  
      try {
        gB.deleteMember( gA.toSubject() );
        // gB -> gC
        // sA -> gA
        Assert.assertTrue("no exception thrown", true);
        T.getMemberships( gA, 1 );
        T.getMemberships( gB, 0 );
        T.getMemberships( gC, 1 );
      }
      catch (GrouperException eGRT) {
        Assert.fail("runtime exception thrown: " + eGRT.getMessage());
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testChildrenOfViaInMofDeletion()

}

