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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestAddMember3.java,v 1.1 2006-03-06 20:21:50 blair Exp $
 */
public class TestAddMember3 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestAddMember3.class);

  public TestAddMember3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testAddMemberToGroupThatIsMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Subject         subj  = SubjectHelper.SUBJ0;
    Member          m     = Helper.getMemberBySubject(s, subj);
    // add uofc to i2
    GroupHelper.addMember(i2, uofc);
    MembershipHelper.testNumMship(uofc, Group.getDefaultList(), 0, 0, 0);
    MembershipHelper.testNumMship(i2,   Group.getDefaultList(), 1, 1, 0);
    MembershipHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
    // add subj to uofc   
    GroupHelper.addMember(uofc, subj, "members");
    MembershipHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
    MembershipHelper.testNumMship(i2,   Group.getDefaultList(), 2, 1, 1);
    MembershipHelper.testImmMship(s, uofc, subj, Group.getDefaultList());
    MembershipHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
    MembershipHelper.testEffMship(s, i2, subj, Group.getDefaultList(), uofc, 1);
  } // public void testAddMemberToGroupThatIsMember()

}

