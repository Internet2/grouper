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
import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the CREATE {@link NamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivCREATE.java,v 1.8 2008-06-24 06:07:03 mchyzer Exp $
 */
public class TestPrivCREATE extends GrouperTest {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestPrivCREATE.class); 
  
  // Private Class Variables
  private static Stem           a;
  private static Stem           edu;
  private static Group          i2;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;


  public TestPrivCREATE(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    a     = null;
    subj0 = SubjectTestHelper.SUBJ0;
    nrs   = SessionHelper.getSession(subj0.getId());
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    GroupHelper.addMember(i2, subj0, "members");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    //TestRunner.run(new TestPrivCREATE("testDelMembersWithADMIN"));
    TestRunner.run(TestPrivCREATE.class);
  }


  // Tests

  public void testCreateChildGroupWithoutCREATE() {
    LOG.info("testCreateChildGroupWithoutCREATE");
    a = StemHelper.findByName(nrs, edu.getName());
    GrouperSession.callbackGrouperSession(nrs, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        StemHelper.addChildGroupFail(a, "uofc", "uchicago");
        return null;
      }
      
    });
  } // public void testCreateChildGroupWithoutCREATE()

  public void testCreateChildGroupWithCREATE() {
    LOG.info("testCreateChildGroupWithCREATE");
    PrivHelper.grantPriv(s, edu, subj0, NamingPrivilege.CREATE);
    a = StemHelper.findByName(nrs, edu.getName());
    StemHelper.addChildGroup(a, "uofc", "uchicago");
  } // public void testCreateChildGroupWithCREATE()

  public void testCreateChildGroupWithAllCREATE() {
    LOG.info("testCreateChildGroupWithAllCREATE");
    PrivHelper.grantPriv(s, edu, SubjectFinder.findAllSubject(), NamingPrivilege.CREATE);
    a = StemHelper.findByName(nrs, edu.getName());
    StemHelper.addChildGroup(a, "uofc", "uchicago");
  } // public void testCreateChildGroupWithAllCREATE()

  public void testCreateChildGroupWithGroupCREATE() {
    LOG.info("testCreateChildGroupWithGroupCREATE");
    try {
      MembershipTestHelper.testImm(s, i2, subj0, "members");
      PrivHelper.grantPriv(s, edu, i2.toSubject(), NamingPrivilege.CREATE);
      Assert.assertTrue("i2 has priv", edu.hasCreate(i2.toSubject()));
      Assert.assertTrue("subj0 has priv", edu.hasCreate(subj0));
      a = StemHelper.findByName(nrs, edu.getName());
      StemHelper.addChildGroup(a, "uofc", "uchicago");
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCreateChildGroupWithGroupCREATE()

} // public class TestPrivCREATE extends GrouperTest 

