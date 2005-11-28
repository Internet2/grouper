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

/**
 * {@link Group} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupHelper.java,v 1.6 2005-11-28 19:14:19 blair Exp $
 */
public class GroupHelper {

  // Protected Class Methods

  // Add a group as a member to a group
  protected static void addMember(Group g, Group gm) {
    try {
      Member m = gm.toMember();
      g.addMember(gm.toSubject());
      Assert.assertTrue("added member", true);
      MembershipHelper.testImm(g, gm.toSubject(), m);
      MembershipHelper.testEff(g, gm, m);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("not privileged to add member: " + eIP.getMessage());
    }
    catch (MemberAddException eMA) {
      Assert.fail("failed to add member: " + eMA.getMessage());
    }
  } // protected static void addMember(g, gm)

  // Add a member to a group
  protected static void addMember(Group g, Subject subj, Member m) {
    try {
      g.addMember(subj);
      Assert.assertTrue("added member", true);
      MembershipHelper.testImm(g, subj, m);
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to add member: " + e0.getMessage());
    }
    catch (MemberAddException e1) {
      Assert.fail("failed to add member: " + e1.getMessage());
    }
  } // protected static void addMember(g, subj, m)

  // Delete a group as a member from a group
  protected static void deleteMember(Group g, Group gm) {
    try {
      Member m = gm.toMember();
      g.deleteMember(gm.toSubject());
      Assert.assertTrue("deleted member", true);
      Assert.assertFalse("g !hasMember m", g.hasMember(gm.toSubject()));
      Assert.assertFalse("m !isMember g", m.isMember(g));
      // TODO Assert immediate and effective in some manner or another
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("not privileged to delete member: " + eIP.getMessage());
    }
    catch (MemberDeleteException eMA) {
      Assert.fail("failed to delete member: " + eMA.getMessage());
    }
  } // protected static void deleteMember(g, gm)

  // Delete a member from a group
  protected static void deleteMember(Group g, Subject subj, Member m) {
    try {
      g.deleteMember(subj);
      Assert.assertTrue("deleted member", true);
      Assert.assertFalse("g !hasMember m", g.hasMember(subj));
      Assert.assertFalse("m !isMember g", m.isMember(g));
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to delete member: " + e0.getMessage());
    }
    catch (MemberDeleteException e1) {
      Assert.fail("failed to delete member: " + e1.getMessage());
    }
  } // protected static void deleteMember(g, subj, m)

  // test converting a Group to a Member
  protected static Member toMember(Group g) {
    try {
      Member m = g.toMember();
      Assert.assertTrue("converted group to member", true);
      Assert.assertNotNull("m !null", m);
      Assert.assertTrue(
        "m subj id", m.getSubjectId().equals(g.getUuid())
      );
      Assert.assertTrue(
        "m type == group", m.getSubjectTypeId().equals("group")
      );
      Assert.assertTrue(
        "m source", m.getSubjectSourceId().equals("grouper group adapter")
      );
      return m;
    }
    catch (RuntimeException e) {
      Assert.fail("failed to convert group to member");
    }
    throw new RuntimeException(Helper.ERROR); 
  } // protected static Member toMember(g)

}

