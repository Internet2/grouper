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
 * Test open bugs.  
 * <p />
 * @author  blair christensen.
 * @version $Id: TestBugsOpen.java,v 1.3 2005-12-06 17:40:21 blair Exp $
 */
public class TestBugsOpen extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestBugsOpen.class);


  public TestBugsOpen(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  // @source  Gary Brown, 20051206, <6513d0390512060544q3fff7944vb8e1cedae7d4f92c@mail.gmail.com>
  // @status  awaiting fixed confirmation
  // TODO Convert into _TestMemberOf_ test case
  public void testGrantNamingPrivsToGroupAndAccessPrivsToSelf() {
    LOG.info("testGrantNamingPrivsToGroupAndAccessPrivsToSelf");
    try {
      Subject kebe = SubjectHelper.SUBJ0;
      Subject iata = SubjectHelper.SUBJ1;
      Subject iawi = SubjectHelper.SUBJ2;

      Subject subj = SubjectFinder.findById("GrouperSystem");
      GrouperSession s = GrouperSession.startSession(subj);
      Stem root = StemFinder.findRootStem(s);
			Stem qsuob = root.addChildStem("qsuob","qsuob");
      Group admins = qsuob.addChildGroup("admins","admins");

      admins.addMember(kebe);
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);

      Group staff = qsuob.addChildGroup("staff","staff");
      staff.addMember(iata);
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testNumMship(staff, "members", 1, 1, 0);
      staff.addMember(iawi);
      MembershipHelper.testImm(s, admins, kebe, "members");
      MembershipHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);

      Group all_staff = qsuob.addChildGroup("all_staff","all staff");
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

      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("create"));
      // TODO test
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("stem"));
      // TODO test

      staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
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

      staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
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

      all_staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
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
      MembershipHelper.testNumMship(all_staff, "readers", 4, 1, 3);
      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipHelper.testNumMship(staff, "admins", 3, 2, 1);

      all_staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
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
      MembershipHelper.testNumMship(all_staff, "readers", 4, 1, 3);
      MembershipHelper.testImm(s, all_staff, subj, "admins");
      MembershipHelper.testImm(s, all_staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, all_staff, kebe, "admins", admins, 1);

      MembershipHelper.testImm(s, staff, iata , "members");
      MembershipHelper.testImm(s, staff, iawi , "members");
      MembershipHelper.testImm(s, staff, admins.toSubject(), "admins");
      MembershipHelper.testEff(s, staff, kebe, "admins", admins, 1);
      MembershipHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipHelper.testNumMship(staff, "admins", 3, 2, 1);

      // TODO s.stopSession();
    }
    catch (Exception e) {
      Assert.fail("exception: " + e.getMessage());
    }
  } // public void testGrantNamingPrivsToGroupAndAccessPrivsToSelf() 

}

