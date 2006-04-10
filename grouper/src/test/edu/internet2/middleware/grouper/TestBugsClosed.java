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
 * Test closed bugs.  
 * <p />
 * @author  blair christensen.
 * @version $Id: TestBugsClosed.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
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
      Subject kebe = SubjectTestHelper.SUBJ0;
      Subject iata = SubjectTestHelper.SUBJ1;
      Subject iawi = SubjectTestHelper.SUBJ2;

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

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testHasAccessPrivButCannotReadPriv() {
    LOG.info("testHasAccessPrivButCannotReadPriv");
    try {
      // Setup
      Subject         all   = SubjectFinder.findAllSubject();
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      Group           a     = edu.addChildGroup("a", "a");
      Group           b     = edu.addChildGroup("b", "b");
      Group           c     = edu.addChildGroup("c", "c");
      Group           d     = edu.addChildGroup("d", "d");
      PrivHelper.grantPriv(s, a, all, AccessPrivilege.OPTIN);
      PrivHelper.grantPriv(s, b, all, AccessPrivilege.OPTOUT);
      PrivHelper.grantPriv(s, c, all, AccessPrivilege.UPDATE);
      s.stop();

      // Test
      Subject         subj  = SubjectTestHelper.SUBJ0;
      GrouperSession  nrs   = GrouperSession.start(subj);
      Member          m     = MemberFinder.findBySubject(nrs, subj);
      Group           A     = GroupFinder.findByName(nrs, "edu:a");
      Group           B     = GroupFinder.findByName(nrs, "edu:b");
      Group           C     = GroupFinder.findByName(nrs, "edu:c");
      Group           D     = GroupFinder.findByName(nrs, "edu:d");

      Set view    = m.hasView();
      Assert.assertTrue(
        "VIEW/4 ("    + view.size()   + ")", view.size()    == 4
      );
      Assert.assertTrue("VIEW/A/HAS",   A.hasView(subj)   );
      Assert.assertTrue("VIEW/B/HAS",   B.hasView(subj)   );
      Assert.assertTrue("VIEW/C/HAS",   C.hasView(subj)   );
      Assert.assertTrue("VIEW/D/HAS",   D.hasView(subj)   );
      Assert.assertTrue("VIEW/A/IS" ,   m.hasView(A)      );
      Assert.assertTrue("VIEW/B/IS" ,   m.hasView(B)      );
      Assert.assertTrue("VIEW/C/IS" ,   m.hasView(C)      );
      Assert.assertTrue("VIEW/D/IS" ,   m.hasView(D)      );

      Set read    = m.hasRead();
      Assert.assertTrue(
        "READ/4 ("    + read.size()   + ")", read.size()    == 4
      );
      Assert.assertTrue("READ/A/HAS",   A.hasRead(subj)   );
      Assert.assertTrue("READ/B/HAS",   B.hasRead(subj)   );
      Assert.assertTrue("READ/C/HAS",   C.hasRead(subj)   );
      Assert.assertTrue("READ/D/HAS",   D.hasRead(subj)   );
      Assert.assertTrue("READ/A/IS" ,   m.hasRead(A)      );
      Assert.assertTrue("READ/B/IS" ,   m.hasRead(B)      );
      Assert.assertTrue("READ/C/IS" ,   m.hasRead(C)      );
      Assert.assertTrue("READ/D/IS" ,   m.hasRead(D)      );

      Set optin   = m.hasOptin();
      Assert.assertTrue(
        "OPTIN/1 ("   + optin.size()  + ")", optin.size()   == 1
      );
      Assert.assertTrue("OPTIN/A/HAS",  A.hasOptin(subj)  );
      Assert.assertTrue("OPTIN/B/HAS",  !B.hasOptin(subj) );
      Assert.assertTrue("OPTIN/C/HAS",  !C.hasOptin(subj) );
      Assert.assertTrue("OPTIN/D/HAS",  !D.hasOptin(subj) );
      Assert.assertTrue("OPTIN/A/IS" ,  m.hasOptin(A)     );
      Assert.assertTrue("OPTIN/B/IS" ,  !m.hasOptin(B)    );
      Assert.assertTrue("OPTIN/C/IS" ,  !m.hasOptin(C)    );
      Assert.assertTrue("OPTIN/D/IS" ,  !m.hasOptin(D)    );

      Set optout  = m.hasOptout();
      Assert.assertTrue(
        "OPTOUT/1 ("  + optout.size() + ")", optout.size()  == 1
      );
      Assert.assertTrue("OPTOUT/A/HAS", !A.hasOptout(subj));
      Assert.assertTrue("OPTOUT/B/HAS", B.hasOptout(subj) );
      Assert.assertTrue("OPTOUT/C/HAS", !C.hasOptout(subj));
      Assert.assertTrue("OPTOUT/D/HAS", !D.hasOptout(subj));
      Assert.assertTrue("OPTOUT/A/IS" , !m.hasOptout(A)   );
      Assert.assertTrue("OPTOUT/B/IS" , m.hasOptout(B)    );
      Assert.assertTrue("OPTOUT/C/IS" , !m.hasOptout(C)   );
      Assert.assertTrue("OPTOUT/D/IS" , !m.hasOptout(D)   );

      Set update  = m.hasUpdate();
      Assert.assertTrue(
        "UPDATE/1 ("  + update.size() + ")", update.size()  == 1
      );
      Assert.assertTrue("UPDATE/A/HAS", !A.hasUpdate(subj));
      Assert.assertTrue("UPDATE/B/HAS", !B.hasUpdate(subj));
      Assert.assertTrue("UPDATE/C/HAS", C.hasUpdate(subj) );
      Assert.assertTrue("UPDATE/D/HAS", !D.hasUpdate(subj));
      Assert.assertTrue("UPDATE/A/IS" , !m.hasUpdate(A)   );
      Assert.assertTrue("UPDATE/B/IS" , !m.hasUpdate(B)   );
      Assert.assertTrue("UPDATE/C/IS" , m.hasUpdate(C)    );
      Assert.assertTrue("UPDATE/D/IS" , !m.hasUpdate(D)   );

      Set admin   = m.hasAdmin();
      Assert.assertTrue(
        "ADMIN/0 ("   + admin.size()  + ")", admin.size()   == 0
      );
      Assert.assertTrue("ADMIN/A/HAS",  !A.hasAdmin(subj) );
      Assert.assertTrue("ADMIN/B/HAS",  !B.hasAdmin(subj) );
      Assert.assertTrue("ADMIN/C/HAS",  !C.hasAdmin(subj) );
      Assert.assertTrue("ADMIN/D/HAS",  !D.hasAdmin(subj) );
      Assert.assertTrue("ADMIN/A/IS" ,  !m.hasAdmin(A)    );
      Assert.assertTrue("ADMIN/B/IS" ,  !m.hasAdmin(B)    );
      Assert.assertTrue("ADMIN/C/IS" ,  !m.hasAdmin(C)    );
      Assert.assertTrue("ADMIN/D/IS" ,  !m.hasAdmin(D)    );

      nrs.stop();

    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testHasAccessPrivButCannotReadPriv()

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testHasNamingPrivButCannotReadPriv() {
    LOG.info("testHasNamingPrivButCannotReadPriv");
    try {
      // Setup
      Subject         all   = SubjectFinder.findAllSubject();
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      Stem            a     = edu.addChildStem("a", "a");
      Stem            b     = edu.addChildStem("b", "b");
      Stem            c     = edu.addChildStem("c", "c");
      PrivHelper.grantPriv(s, a, all, NamingPrivilege.CREATE);
      PrivHelper.grantPriv(s, b, all, NamingPrivilege.STEM);
      s.stop();

      // Test
      Subject         subj  = SubjectTestHelper.SUBJ0;
      GrouperSession  nrs   = GrouperSession.start(subj);
      Member          m     = MemberFinder.findBySubject(nrs, subj);
      Stem            A     = StemFinder.findByName(nrs, "edu:a");
      Stem            B     = StemFinder.findByName(nrs, "edu:b");
      Stem            C     = StemFinder.findByName(nrs, "edu:c");

      Set create  = m.hasCreate();
      Assert.assertTrue(
        "CREATE/1 ("  + create.size() + ")", create.size()  == 1
      );
      Assert.assertTrue("CREATE/A/HAS", A.hasCreate(subj) );
      Assert.assertTrue("CREATE/B/HAS", !B.hasCreate(subj));
      Assert.assertTrue("CREATE/C/HAS", !C.hasCreate(subj));
      Assert.assertTrue("CREATE/A/IS" , m.hasCreate(A)    );
      Assert.assertTrue("CREATE/B/IS" , !m.hasCreate(B)   );
      Assert.assertTrue("CREATE/C/IS" , !m.hasCreate(C)   );

      Set stem    = m.hasStem();
      Assert.assertTrue(
        "STEM/1   ("  + stem.size()   + ")", stem.size()    == 1
      );
      Assert.assertTrue("STEM/A/HAS"  , !A.hasStem(subj)  );
      Assert.assertTrue("STEM/B/HAS"  , B.hasStem(subj)   );
      Assert.assertTrue("STEM/C/HAS"  , !C.hasStem(subj)  );
      Assert.assertTrue("STEM/A/IS"   , !m.hasStem(A)     );
      Assert.assertTrue("STEM/B/IS"   , m.hasStem(B)      );
      Assert.assertTrue("STEM/C/IS"   , !m.hasStem(C)     );

      nrs.stop();

    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testHasNamingPrivButCannotReadPriv()

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testGrantAllAccessPriv() {
    LOG.info("testGrantAllAccessPriv");
    try {   
      Subject         all   = SubjectFinder.findAllSubject();
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns    = root.addChildStem("ns", "ns");
      Group           a     = ns.addChildGroup("a", "a");
      Group           b     = ns.addChildGroup("b", "b");
      PrivHelper.grantPriv(s, a, all, AccessPrivilege.ADMIN);
      PrivHelper.grantPriv(s, b, all, AccessPrivilege.UPDATE);
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGrantAllAccessPriv()

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testGrantAllNamingPriv() {
    LOG.info("testGrantAllNamingPriv");
    try {   
      Subject         all   = SubjectFinder.findAllSubject();
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            a     = root.addChildStem("a", "a");
      Stem            b     = root.addChildStem("b", "b");
      PrivHelper.grantPriv(s, a, all, NamingPrivilege.CREATE);
      PrivHelper.grantPriv(s, b, all, NamingPrivilege.STEM);
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGrantAllNamingPriv()

  // @source  Gary Brown, 20051221, <B96A40BBB6DC736573C06C6D@cse-gwb.cse.bris.ac.uk>
  // @status  fixed
  public void testSetStemDisplayName() {
    LOG.info("testSetStemDisplayName");
    // Setup
    Subject subj0 = SubjectTestHelper.SUBJ0;
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            qsuob = root.addChildStem("qsuob", "qsuob");
      qsuob.grantPriv(subj0, NamingPrivilege.STEM);
      Stem            cs    = qsuob.addChildStem("cs", "child stem");
      // These weren't explicitly listed in the test report but I can't
      // replicate unless I have at least two groups.
      Group           cg    = qsuob.addChildGroup("cg", "child group");
      Group           gcg   = cs.addChildGroup("gcg", "grandchild group");
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    // Test
    try {
      GrouperSession  nrs   = GrouperSession.start(subj0);
      Stem            qsuob = StemFinder.findByName(nrs, "qsuob");
      String          de    = "QS University of Bristol";
      qsuob.setDisplayExtension(de);
      String          val   = qsuob.getDisplayExtension();
      Assert.assertTrue("updated displayExtn: " + val, de.equals(val));
      val                   = qsuob.getDisplayName();
      Assert.assertTrue("updated displayName: " + val, de.equals(val));
      nrs.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testStemDisplayName()

}

