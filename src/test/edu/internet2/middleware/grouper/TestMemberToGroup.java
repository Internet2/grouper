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
import edu.internet2.middleware.grouper.registry.RegistryReset;
import  junit.framework.*;

/**
 * Test {@link Member.toGroup()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMemberToGroup.java,v 1.7 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestMemberToGroup extends TestCase {

  public TestMemberToGroup(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testToGroup() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Member          m     = MemberHelper.getMemberBySubject(
      s, SubjectTestHelper.getSubjectByIdType(i2.getUuid(), "group")
    );
    Group           g     = MemberHelper.toGroup(m);
    Assert.assertTrue("i2 == g", i2.equals(g));
  } // public void testToGroup()

}

