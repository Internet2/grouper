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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

/**
 * Circular memberships are only vetoed for the members list
 * @author Shilen Patel.
 */
public class TestMembership8 extends GrouperTest {

  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership8("testCircularMembershipOnlyVetoedForMembersList"));
  }

  /**
   * @param name
   */
  public TestMembership8(String name) {
    super(name);
  }

  /**
   * A -> B -> A in the members list is vetoed, but custom list fields and privileges are not checked
   */
  public void testCircularMembershipOnlyVetoedForMembersList() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group gA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:gA").save();
    Group gB = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:gB").save();

    GroupType customType = GroupType.createType(grouperSession, "customType");
    gB.addType(customType);
    Field fieldCustom = customType.addList(grouperSession, "customField1", AccessPrivilege.READ, AccessPrivilege.UPDATE);

    gA.addMember(gB.toSubject());
    gA.addMember(SubjectTestHelper.SUBJ0);

    try {
      gB.addMember(gA.toSubject());
      fail("Expected circular membership veto");
    } catch (RuntimeException re) {
      assertNotNull("Expected circular membership veto but was: " + re, GroupSet.retrieveCircularMembershipVeto(re));
    }
    assertFalse(gB.hasMember(gA.toSubject()));

    // the same groups in a custom list or a privilege are allowed
    gB.addMember(gA.toSubject(), fieldCustom);
    gB.addMember(gB.toSubject(), fieldCustom);
    gB.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
    gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    assertTrue(gB.hasMember(gA.toSubject(), fieldCustom));
    assertTrue(gB.hasMember(SubjectTestHelper.SUBJ0, fieldCustom));
    assertTrue(gB.hasUpdate(SubjectTestHelper.SUBJ0));
    assertTrue(gA.hasUpdate(SubjectTestHelper.SUBJ0));
  }
}
