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
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership8 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMembership8.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Subject subjA;
  Subject subjB;
  Subject subjC;

  Field fieldMembers;
  Field fieldUpdaters;

  public TestMembership8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCircularMembershipsWithoutComposites2() {
    LOG.info("testCircularMembershipsWithoutComposites2");
    try {
      before   = DateHelper.getPastDate();

      r     = R.populateRegistry(1, 2, 3);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find("updaters");
      Set<Membership> listMemberships;
      Set<Membership> updateMemberships;

      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();

      goodGroups.add(gA);
      goodGroups.add(gB);


      // Test 1
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 1", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 1", 0, updateMemberships.size());

      // Test 2
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 2", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 2", 0, updateMemberships.size());

      // Test 3
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 3", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 3", 0, updateMemberships.size());

      // Test 4
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 4", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 4", 0, updateMemberships.size());

      // Test 5
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 5", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 5", 0, updateMemberships.size());

      // Test 6
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 6", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 6", 0, updateMemberships.size());

      // Test 7
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);

      verifyMemberships();

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // clear out memberships
      gA.deleteMember(gB.toSubject());
      gB.deleteMember(gA.toSubject());
      gA.deleteMember(subjA);
      gA.deleteMember(subjC);
      gB.deleteMember(subjA);
      gB.deleteMember(subjB);
      gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships after test 7", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges after test 7", 0, updateMemberships.size());



      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {
    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 10, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 6, updateMemberships.size());
  }

}

