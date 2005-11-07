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
 * {@link Member} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberHelper.java,v 1.1.2.1 2005-11-07 20:39:13 blair Exp $
 */
public class MemberHelper {

  // Protected Class Methods

  // test converting a Member to a Group
  protected static Group toGroup(Member m) {
    try {
      Group g = m.toGroup();
      Assert.assertTrue("converted member to group", true);
      Assert.assertNotNull("g !null", g);
      Assert.assertTrue(
        "m subj id", m.getSubjectId().equals(g.getUuid())
      );
      Assert.assertTrue(
        "m type == group", m.getSubjectTypeId().equals("group")
      );
      Assert.assertTrue(
        "m source", m.getSubjectSourceId().equals("grouperAdapter")
      );
      return g;
    }
    catch (GroupNotFoundException eGNF) {
      Assert.fail("failed to convert member to group");
    }
    throw new RuntimeException(Helper.ERROR); 
  } // protected static Group toGroup(m)

}

