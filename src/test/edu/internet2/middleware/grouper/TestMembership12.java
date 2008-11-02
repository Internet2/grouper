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

package edu.internet2.middleware.grouper;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership12 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership12("testCircularWithComposites"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestMembership12.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Group   gF;

  Field fieldMembers;
  Field fieldUpdaters;

  public TestMembership12(String name) {
    super(name);
  }

  public void testCircularWithComposites() {
    LOG.info("testCircularWithComposites");
    try {
      before   = DateHelper.getPastDate();

      r     = R.populateRegistry(1, 14, 5);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      gF    = r.getGroup("a", "f");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find("updaters");

      // Test 1
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      try {
        gB.addMember(gA.toSubject());
        fail("Should throw MemberAddException");
      } catch (MemberAddException e) {
        // good
      }

      // Test 2
      try {
        gC.addMember(gA.toSubject());
        fail("Should throw MemberAddException");
      } catch (MemberAddException e) {
        // good
      }

      // Test 3
      gB.addMember(gD.toSubject());
      try {
        gD.addMember(gA.toSubject());
        fail("Should throw MemberAddException");
      } catch (MemberAddException e) {
        // good
      }

      // Test 4
      gB.deleteMember(gD.toSubject());
      gD.addMember(gA.toSubject());
      try {
        gB.addMember(gD.toSubject());
        fail("Should throw MemberAddException");
      } catch (MemberAddException e) {
        // good
      }

      // Test 5
      gD.deleteMember(gA.toSubject());
      gB.addMember(gD.toSubject());
      gD.addMember(gE.toSubject());
      gE.addMember(gF.toSubject());
      try {
        gF.addMember(gA.toSubject());
        fail("Should throw MemberAddException");
      } catch (MemberAddException e) {
        // good
      }

      // Test 5
      gE.deleteMember(gF.toSubject());
      gF.addMember(gA.toSubject());
      try {
        gE.addMember(gF.toSubject());
        fail("Should throw MemberAddException");
      } catch (MemberAddException e) {
        // good
      }

      // Test 6
      gA.deleteCompositeMember();
      gE.addMember(gF.toSubject());
      try {
        gA.addCompositeMember(CompositeType.UNION, gB, gC);
        fail("Should throw IllegalStateException");
      } catch (IllegalStateException e) {
        // good
      }


      // privileges should still work though
      gE.deleteMember(gF.toSubject());
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      gB.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gD.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gE.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      verifyMemberships();

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {

    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 8, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 9, updateMemberships.size());

    Set<Group> goodGroups = new LinkedHashSet<Group>();
    Set<Group> badGroups = new LinkedHashSet<Group>();
      
    goodGroups.add(gA);
    goodGroups.add(gB);
    goodGroups.add(gC);
    goodGroups.add(gD);
    goodGroups.add(gE);
    goodGroups.add(gF);

    MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
    Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());
  }
}

