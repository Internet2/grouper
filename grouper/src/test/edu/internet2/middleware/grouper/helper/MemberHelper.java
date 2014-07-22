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

package edu.internet2.middleware.grouper.helper;
import junit.framework.Assert;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.subject.Subject;

/**
 * {@link Member} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberHelper.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class MemberHelper {

  // public Class Methods

  // test converting a Member to a Group
  public static Group toGroup(Member m) {
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
        "m source",
        m.getSubjectSourceId().equals(SubjectFinder.internal_getGSA().getId())
      );
      return g;
    }
    catch (Exception e) {
      T.e(e);
    }
    throw new GrouperException(); 
  } // public static Group toGroup(m)

  // Get a member by subject
  // @return  A {link Member}
  public static Member getMemberBySubject(GrouperSession s, Subject subj) {
    try {
      Member m = MemberFinder.findBySubject(s, subj, true);
      Assert.assertNotNull("m !null", m);
      Assert.assertTrue(
        "m instanceof Member", m instanceof Member
      );
      Assert.assertNotNull("m uuid !null", m.getUuid());
      Assert.assertTrue("m has uuid", !m.getUuid().equals(""));
      Assert.assertNotNull("m subj !null", m.getSubject());
      Assert.assertNotNull("m subj id !null", m.getSubjectId());
      Assert.assertNotNull("m subj type id !null", m.getSubjectTypeId());
      return m;
    }
    catch (Exception e) {
      T.e(e);
    }
    throw new GrouperException();
  } // public static Member getMemberBySubject(s, subj)

} // public class MemberHelper

