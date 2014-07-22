/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership8 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership8("testCircularMembershipsWithoutComposites2"));
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TestMembership8.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Subject subjA;
  Subject subjB;
  Subject subjC;

  Field fieldMembers;
  Field fieldUpdaters;
  Field fieldCustom1;
  Field fieldCustom2;

  public TestMembership8(String name) {
    super(name);
  }

  public void testCircularMembershipsWithoutComposites2() {
    LOG.info("testCircularMembershipsWithoutComposites2");
    try {
      //sleep so if auto added members in config check, doesnt mess things up here
      GrouperUtil.sleep(50);
//      before   = DateHelper.getPastDate();
      before   = new Date();

      r     = R.populateRegistry(1, 4, 3);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");

      GroupType customType  = GroupType.createType(r.rs, "customType");
      gB.addType(customType);
      fieldCustom1 = customType.addList(r.rs, "customField1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
      fieldCustom2 = customType.addList(r.rs, "customField2", AccessPrivilege.READ, AccessPrivilege.UPDATE);

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true);
      Set<Membership> listMemberships;
      Set<Membership> updateMemberships;
      Set<Membership> custom1Memberships;
      Set<Membership> custom2Memberships;


      // Test 1
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);

      verifyMemberships();
      deleteMemberships();


      // Test 2
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());

      verifyMemberships();
      deleteMemberships();


      // Test 3
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());

      verifyMemberships();
      deleteMemberships();


      // Test 4
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);

      verifyMemberships();
      deleteMemberships();


      // Test 5
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);

      verifyMemberships();
      deleteMemberships();


      // Test 6
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);

      verifyMemberships();
      deleteMemberships();


      // Test 7
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);

      verifyMemberships();
      deleteMemberships();


      // Test 8
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      verifyMemberships();
      deleteMemberships();


      // Test 9
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());

      verifyMemberships();
      deleteMemberships();


      // Test 10
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());

      verifyMemberships();
      deleteMemberships();


      // Test 11
      gB.addMember(gA.toSubject(), fieldCustom2);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);

      verifyMemberships();
      deleteMemberships();



      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public void verifyMemberships() throws Exception {
    // SC -> gB (custom1 field)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SC -> gB", gB, subjC, gA, 2, gB, gA.toSubject(), gB, 1, fieldCustom1);

    // SA -> gB (custom2 field)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gB", gB, subjA, gB, 2, gB, gB.toSubject(), gA, 1, fieldCustom2);

    // SA -> gC (default field)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gC", gC, subjA, gB, 2, gC, gB.toSubject(), gA, 1, fieldMembers);

    // SA -> gA (update field)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gA", gA, subjA, gB, 2, gA, gB.toSubject(), gA, 1, fieldUpdaters);

    // SA -> gD (default field)
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SA -> gD", gD, subjA, gA, 2, gD, gA.toSubject(), gB, 1, fieldMembers);


    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 26, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 7, updateMemberships.size());

    // verify the total number of custom1 privileges
    Set<Membership> custom1Memberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCustom1);
    T.amount("Number of custom1 privileges", 7, custom1Memberships.size());

    // verify the total number of custom2 privileges
    Set<Membership> custom2Memberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCustom2);
    T.amount("Number of custom2 privileges", 7, custom2Memberships.size());
  }

  public void deleteMemberships() throws Exception {

    // clear out memberships
    gA.deleteMember(gB.toSubject());
    gB.deleteMember(gA.toSubject());
    gA.deleteMember(subjA);
    gA.deleteMember(subjC);
    gB.deleteMember(subjA);
    gB.deleteMember(subjB);
    gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
    gC.deleteMember(gA.toSubject());
    gD.deleteMember(gB.toSubject());
    gB.deleteMember(gB.toSubject(), fieldCustom1);

    //try again
    try {
      gB.deleteMember(gB.toSubject(), fieldCustom1);
      fail("should fail since already done");
    } catch (MemberDeleteAlreadyDeletedException mdade) {
      //good
    }
    
    //try again but dont throw exception if problem
    assertFalse(gB.deleteMember(gB.toSubject(), fieldCustom1, false));
    
    gB.deleteMember(gA.toSubject(), fieldCustom2);
      
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 0, listMemberships.size());

    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 0, updateMemberships.size());

    Set<Membership> custom1Memberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCustom1);
    T.amount("Number of custom1 privileges", 0, custom1Memberships.size());

    Set<Membership> custom2Memberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCustom2);
    T.amount("Number of custom2 privileges", 0, custom2Memberships.size());
  }

}

