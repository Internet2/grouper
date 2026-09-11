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

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipSave;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * A group cannot be a member of itself at any depth
 * @author Shilen Patel.
 */
public class TestMembership7 extends GrouperTest {

  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership7("testRemoveMembershipFromExistingCircularMembership"));
  }

  /** root session */
  private GrouperSession grouperSession;

  /** */
  private Group gA;

  /** */
  private Group gB;

  /** */
  private Group gC;

  /** */
  private Group gD;

  /**
   * @param name
   */
  public TestMembership7(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.grouperSession = GrouperSession.startRootSession();
    this.gA = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:gA").save();
    this.gB = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:gB").save();
    this.gC = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:gC").save();
    this.gD = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:gD").save();
  }

  /**
   * A -> B -> A
   */
  public void testCircularMembershipDepth2Vetoed() {
    this.gA.addMember(this.gB.toSubject());
    this.gB.addMember(SubjectTestHelper.SUBJ0);

    assertCircularMembershipVetoed(this.gB, this.gA);

    // nothing changed
    assertTrue(this.gA.hasMember(this.gB.toSubject()));
    assertTrue(this.gA.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(this.gB.hasMember(this.gA.toSubject()));
  }

  /**
   * A -> B -> C -> A
   */
  public void testCircularMembershipDepth3Vetoed() {
    this.gA.addMember(this.gB.toSubject());
    this.gB.addMember(this.gC.toSubject());
    this.gC.addMember(SubjectTestHelper.SUBJ0);

    assertCircularMembershipVetoed(this.gC, this.gA);

    // other add member paths are vetoed too
    try {
      new MembershipSave().assignGroup(this.gC).assignSubject(this.gA.toSubject()).save();
      fail("Expected circular membership veto from MembershipSave");
    } catch (RuntimeException re) {
      assertNotNull("Expected circular membership veto but was: " + re, GroupSet.retrieveCircularMembershipVeto(re));
    }

    // nothing changed
    assertFalse(this.gC.hasMember(this.gA.toSubject()));
    assertTrue(this.gA.hasMember(this.gC.toSubject()));
    assertTrue(this.gA.hasMember(SubjectTestHelper.SUBJ0));

    // adding the top group to a group outside the chain is not circular
    this.gD.addMember(this.gA.toSubject());
    assertTrue(this.gD.hasMember(SubjectTestHelper.SUBJ0));
  }

  /**
   * A loop that was created before circular memberships were vetoed can still be broken by removing a membership,
   * and once it is broken it cannot be created again
   */
  public void testRemoveMembershipFromExistingCircularMembership() {
    this.gA.addMember(this.gB.toSubject());
    createExistingCircularMembership(this.gB, this.gA);
    assertTrue(this.gB.hasMember(this.gA.toSubject()));

    // an existing loop does not block adding a group in the loop to another group
    this.gC.addMember(this.gA.toSubject());
    assertTrue(this.gC.hasMember(this.gB.toSubject()));

    this.gB.deleteMember(this.gA.toSubject());
    assertFalse(this.gB.hasMember(this.gA.toSubject()));
    assertNull(GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(
        this.gB.getUuid(), this.gA.getUuid(), Group.getDefaultList()));

    assertCircularMembershipVetoed(this.gB, this.gA);
  }

  /**
   * Adding the member group to the owner group should be vetoed and leave nothing behind
   * @param owner
   * @param member
   */
  private void assertCircularMembershipVetoed(Group owner, Group member) {
    try {
      owner.addMember(member.toSubject());
      fail("Expected circular membership veto adding " + member.getName() + " to " + owner.getName());
    } catch (RuntimeException re) {
      assertNotNull("Expected circular membership veto but was: " + re, GroupSet.retrieveCircularMembershipVeto(re));
    }

    String memberUuid = MemberFinder.findBySubject(this.grouperSession, member.toSubject(), true).getUuid();
    assertNull(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        owner.getUuid(), memberUuid, Group.getDefaultList(), "immediate", false, false));
    assertNull(GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(
        owner.getUuid(), member.getUuid(), Group.getDefaultList()));
  }

  /**
   * Create a circular membership the way it looks if it was added before circular memberships were vetoed:
   * an enabled immediate membership and its immediate group set, but no effective group sets through the loop
   * @param owner
   * @param member
   */
  private void createExistingCircularMembership(Group owner, Group member) {

    // add it disabled so there is no group set yet
    new MembershipSave().assignGroup(owner).assignSubject(member.toSubject())
      .assignImmediateMshipEnabledTime(System.currentTimeMillis() + 86400000L).save();

    String memberUuid = MemberFinder.findBySubject(this.grouperSession, member.toSubject(), true).getUuid();
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        owner.getUuid(), memberUuid, Group.getDefaultList(), "immediate", true, false);

    new GcDbAccess().sql("update grouper_memberships set enabled = 'T', enabled_timestamp = null where id = ?")
      .addBindVar(membership.getImmediateMembershipId()).executeSql();

    GroupSet ownerSelfGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(
        owner.getUuid(), Group.getDefaultList().getUuid());

    new GcDbAccess().sql("insert into grouper_group_set (id, owner_group_id, owner_group_id_null, owner_stem_id_null, "
        + "owner_attr_def_id_null, member_group_id, member_id, field_id, member_field_id, owner_id, mship_type, depth, "
        + "via_group_id, parent_id, creator_id, create_time, hibernate_version_number) "
        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'effective', 1, ?, ?, ?, ?, 0)")
      .addBindVar(GrouperUuid.getUuid())
      .addBindVar(owner.getUuid())
      .addBindVar(owner.getUuid())
      .addBindVar(GroupSet.nullColumnValue)
      .addBindVar(GroupSet.nullColumnValue)
      .addBindVar(member.getUuid())
      .addBindVar(member.getUuid())
      .addBindVar(Group.getDefaultList().getUuid())
      .addBindVar(Group.getDefaultList().getUuid())
      .addBindVar(owner.getUuid())
      .addBindVar(member.getUuid())
      .addBindVar(ownerSelfGroupSet.getId())
      .addBindVar(ownerSelfGroupSet.getCreatorId())
      .addBindVar(System.currentTimeMillis())
      .executeSql();
  }
}
