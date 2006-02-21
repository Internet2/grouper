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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Group.addMember()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupAddMember.java,v 1.10 2006-02-21 17:11:33 blair Exp $
 */
public class TestGroupAddMember extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupAddMember.class);

  public TestGroupAddMember(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  // Tests

  public void testAddMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Subject         subj  = SubjectHelper.getSubjectById(
      SubjectHelper.SUBJ_ROOT
    );
    Member          m     = Helper.getMemberBySubject(s, subj);
    GroupHelper.addMember(i2, subj, m);
    // mships
    MembershipHelper.testNumMship(i2, Group.getDefaultList(), 1, 1, 0);
    MembershipHelper.testImmMship(s, i2, subj, Group.getDefaultList());
  } // public void testAddMember()

}

