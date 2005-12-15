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
 * Test closed bugs.  
 * <p />
 * @author  blair christensen.
 * @version $Id: TestBugsClosed.java,v 1.5 2005-12-15 16:11:35 blair Exp $
 */
public class TestBugsClosed extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestBugsClosed.class);


  public TestBugsClosed(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  // @source  Gary Brown, 20051202, <C76C2307ED5A17415027C3D2@cse-gwb.cse.bris.ac.uk>
  // @status  fixed
  public void testGrantStemToGroup() {
    try {
      // Setup
      GrouperSession  s     = GrouperSession.start(
        SubjectFinder.findById("GrouperSystem", "application")
      );
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "educational");
      Group           i2    = edu.addChildGroup("i2", "internet2");
      Subject         subj0 = SubjectFinder.findById("test.subject.0");
      i2.addMember(subj0);
      Assert.assertTrue("i2 has mem subj0", i2.hasMember(subj0));
      Assert.assertTrue("i2 has imm mem subj0", i2.hasImmediateMember(subj0));
      // Test
      Stem            ns    = StemFinder.findByName(s, edu.getName());
      Assert.assertNotNull("ns !null", ns);
      Group           g     = GroupFinder.findByName(s, i2.getName());
      Assert.assertNotNull("g !null", g);

      // Without the pre-granting of CREATE, the later granting of STEM
      // is fine.
      ns.grantPriv(
        SubjectFinder.findById(g.getUuid()),
         Privilege.getInstance("create")
       );
      Assert.assertTrue("g (ns) has CREATE", g.toMember().hasCreate(ns));
      Assert.assertTrue("g (m) has CREATE",  ns.hasCreate(g.toSubject()));

      ns.grantPriv(
        SubjectFinder.findById(g.getUuid()),
        Privilege.getInstance("stem")
      );
      Assert.assertTrue("g (ns) has STEM", g.toMember().hasStem(ns));
      Assert.assertTrue("g (m) has STEM",  ns.hasStem(g.toSubject()));

      s.stop();
    }
    catch (Exception e) {
      Assert.fail("exception: " + e.getMessage());
    }
  } // public void testGrantStemToGroup()

  // @source  Gary Brown, 20051206, <6513d0390512060544q3fff7944vb8e1cedae7d4f92c@mail.gmail.com>
  // @status  fixed
  public void testBadEffMshipDepthCalcExposedByGroupDelete() {
    LOG.info("testBadEffMshipDepthCalcExposedByGroupDelete");
    try {
      Subject kebe = SubjectHelper.SUBJ0;
      Subject iata = SubjectHelper.SUBJ1;
      Subject iawi = SubjectHelper.SUBJ2;

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.0");
      Subject subj = SubjectFinder.findById("GrouperSystem");
      GrouperSession s = GrouperSession.start(subj);
      Stem root = StemFinder.findRootStem(s);
			Stem qsuob = root.addChildStem("qsuob","qsuob");
      Group admins = qsuob.addChildGroup("admins","admins");

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.0");
      admins.addMember(kebe);
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.1");
      Group staff = qsuob.addChildGroup("staff","staff");

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.2");
      staff.addMember(iata);
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testNumMship(staff, "members", 1, 1, 0);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.3");
      staff.addMember(iawi);
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.4");
      Group all_staff = qsuob.addChildGroup("all_staff","all staff");

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.5");
      all_staff.addMember(staff.toSubject());
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.6");
      admins.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testImm(s, admins, subj, "admins");
      MembershipHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testNumMship(admins, "admins", 3, 2, 1);
      MembershipHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.7");
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("create"));
      // TODO test

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.8");
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("stem"));
      // TODO test

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.9");
      staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
      // TODO test
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testImm(s, admins, subj, "admins");
      MembershipHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testNumMship(admins, "admins", 3, 2, 1);
      MembershipHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipHelper.testNumMship(all_staff, "members", 3, 1, 2);

      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipHelper.testImm(s, staff, all_staff.toSubject(), "readers");
      MembershipHelper.testEff(s, staff, iata, "readers", staff, 2);
      MembershipHelper.testEff(s, staff, iawi, "readers", staff, 2);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.10");
      staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      // TODO test
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testImm(s, admins, subj, "admins");
      MembershipHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testNumMship(admins, "admins", 3, 2, 1);
      MembershipHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipHelper.testNumMship(staff, "admins", 3, 2, 1);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11");
      all_staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
      // TODO test
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testImm(s, admins, subj, "admins");
      MembershipHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testNumMship(admins, "admins", 3, 2, 1);
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11.0");

      MembershipHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipHelper.testImm(s, all_staff, all_staff.toSubject(), "readers");
      MembershipHelper.testEff(s, all_staff, staff.toSubject(), "readers", all_staff, 1);
      MembershipHelper.testEff(s, all_staff, iata, "readers", staff, 2);
      MembershipHelper.testEff(s, all_staff, iawi, "readers", staff, 2);
      MembershipHelper.testNumMship(all_staff, "readers", 5, 2, 3);
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11.1");

      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipHelper.testNumMship(staff, "admins", 3, 2, 1);

      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.12");
      all_staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      // TODO test
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testImm(s, admins, subj, "admins");
      MembershipHelper.testImm(s, admins, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, admins, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testNumMship(admins, "admins", 3, 2, 1);

      MembershipHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipHelper.testImm(s, all_staff, all_staff.toSubject(), "readers");
      MembershipHelper.testEff(s, all_staff, staff.toSubject(), "readers", all_staff, 1);
      MembershipHelper.testEff(s, all_staff, iata, "readers", staff, 2);
      MembershipHelper.testEff(s, all_staff, iawi, "readers", staff, 2);
      MembershipHelper.testNumMship(all_staff, "readers", 5, 2, 3);
      MembershipHelper.testImm(s, all_staff, subj, "admins");
      MembershipHelper.testImm(s, all_staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, all_staff, kebe, "admins", admins, 1);

      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipHelper.testNumMship(staff, "admins", 3, 2, 1);

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

  // @source  Gary Brown, 20051212, <04A762113806B3F6EDBFD2F8@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testChildStemsLazyInitializationException() {
    LOG.info("testChildStemsLazyInitializationException");
    try {
      Subject         subj  = SubjectFinder.findById("GrouperSystem");
      GrouperSession  s     = GrouperSession.start(subj);
      Stem  root  = StemFinder.findRootStem(s);
      Stem  qsuob = root.addChildStem("qsuob", "qsuob");
      s.stop();

      s = GrouperSession.start(subj);
      Stem  a         = StemFinder.findByName(s,"qsuob");
      Set   children  = a.getChildStems();
      s.stop();

      Assert.assertTrue("no exceptions", true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testChildStemsLazyInitializationException() 

}

