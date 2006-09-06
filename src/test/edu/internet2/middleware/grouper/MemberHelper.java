/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import edu.internet2.middleware.subject.Subject;
import  junit.framework.*;

/**
 * {@link Member} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberHelper.java,v 1.5 2006-09-06 19:50:21 blair Exp $
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
        "m source",
        m.getSubjectSourceId().equals(SubjectFinder.getGSA().getId())
      );
      return g;
    }
    catch (Exception e) {
      T.e(e);
    }
    throw new GrouperRuntimeException(); 
  } // protected static Group toGroup(m)

  // Get a member by subject
  // @return  A {link Member}
  protected static Member getMemberBySubject(GrouperSession s, Subject subj) {
    try {
      Member m = MemberFinder.findBySubject(s, subj);
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
    throw new GrouperRuntimeException();
  } // protected static Member getMemberBySubject(s, subj)

  // Get a member by bad subject
  protected static void getMemberBySubjectBad(GrouperSession s, Subject subj) {
    try {
      MemberFinder.findBySubject(s, subj);
      Assert.fail("found invalid member");
    }
    catch (MemberNotFoundException e) {
      Assert.assertTrue("invalid member not found", true);
    }
  } // protected static void getMemberBySubjectBad(s, subj)

}

