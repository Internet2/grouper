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
import  junit.framework.*;

/**
 * {@link Group} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupHelper.java,v 1.1.2.2 2005-11-07 16:22:36 blair Exp $
 */
public class GroupHelper {

  // Protected Class Methods

  // Add a member to a group
  protected static void addMember(Group g, Member m) {
    try {
      g.addMember(m);
      Assert.assertTrue("added member", true);
      Assert.assertTrue("g hasMember m", g.hasMember(m));
      Assert.assertTrue("m isMember g", m.isMember(g));
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to add member: " + e0.getMessage());
    }
    catch (MemberAddException e1) {
      Assert.fail("failed to add member: " + e1.getMessage());
    }
  }

  // Delete a member from a group
  protected static void deleteMember(Group g, Member m) {
    try {
      g.deleteMember(m);
      Assert.assertTrue("deleted member", true);
      Assert.assertFalse("g !hasMember m", g.hasMember(m));
      Assert.assertFalse("m !isMember g", m.isMember(g));
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to delete member: " + e0.getMessage());
    }
    catch (MemberDeleteException e1) {
      Assert.fail("failed to delete member: " + e1.getMessage());
    }
  }

}

