/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.privs;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MemberHelper;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the VIEW {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivVIEW.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestPrivVIEW extends TestCase {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestPrivVIEW.class);

  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;


  public TestPrivVIEW(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    s       = SessionHelper.getRootSession();
    root    = StemHelper.findRootStem(s);
    edu     = StemHelper.addChildStem(root, "edu", "educational");
    i2      = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc    = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0   = SubjectTestHelper.SUBJ0;
    subj1   = SubjectTestHelper.SUBJ1;
    m       = MemberHelper.getMemberBySubject(nrs, subj1);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new TestPrivVIEW("testGrantedToCreator"));
    TestRunner.run(TestPrivVIEW.class);
  }

  // Tests

  public void testFindGroupWithoutADMIN() { 
    LOG.info("testFindGroupWithoutADMIN");
    // ALL has VIEW 
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithoutADMIN()

  public void testFindGroupWithADMIN() {
    LOG.info("testFindGroupWithADMIN");
    PrivHelper.grantPriv(s, i2, nrs.getSubject(), AccessPrivilege.ADMIN);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithADMIN()

  public void testFindGroupWithAllADMIN() {
    LOG.info("testFindGroupWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithAllADMIN()

  public void testFindGroupWithoutOPTIN() {
    LOG.info("testFindGroupWithoutOPTIN");
    // ALL has VIEW
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithoutOPTIN()

  public void testFindGroupWithOPTIN() {
    LOG.info("testFindGroupWithOPTIN");
    PrivHelper.grantPriv(s, i2, nrs.getSubject(), AccessPrivilege.OPTIN);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithOPTIN()

  public void testFindGroupWithAllOPTIN() {
    LOG.info("testFindGroupWithAllOPTIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithAllOPTIN()

  public void testFindGroupWithoutREAD() {
    LOG.info("testFindGroupWithoutREAD");
    // ALL has VIEW
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithoutREAD()

  public void testFindGroupWithREAD() {
    LOG.info("testFindGroupWithREAD");
    PrivHelper.grantPriv(s, i2, nrs.getSubject(), AccessPrivilege.READ);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithREAD()

  public void testFindGroupWithAllREAD() {
    LOG.info("testFindGroupWithAllREAD");
    // Already exists
    // PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithAllREAD()

  public void testFindGroupWithoutUPDATE() {
    LOG.info("testFindGroupWithoutUPDATE");
    // ALL has VIEW
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithoutUPDATE()

  public void testFindGroupWithUPDATE() {
    LOG.info("testFindGroupWithUPDATE");
    PrivHelper.grantPriv(s, i2, nrs.getSubject(), AccessPrivilege.UPDATE);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithUPDATE()

  public void testFindGroupWithAllUPDATE() {
    LOG.info("testFindGroupWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithAllUPDATE()

  public void testFindChildGroupWithoutVIEW() {
    LOG.info("testFindChildGroupWithoutVIEW");
    // Revoke ALL VIEW + READ
    PrivHelper.revokePriv(s, i2,    SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    PrivHelper.revokePriv(s, i2,    SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    PrivHelper.revokePriv(s, uofc,  SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    PrivHelper.revokePriv(s, uofc,  SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    // Now get parent stem
    final Stem  parent    = StemHelper.findByName(nrs, edu.getName());
    GrouperSession.callbackGrouperSession(nrs, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Set   children  = parent.getChildGroups();
        Assert.assertTrue(
          "children == " + children.size() + " (exp 0)",
          children.size() == 0
        );
        return null;
      }
      
    });
  } // public void testFindGroupWithoutVIEW()

  public void testFindChildGroupWithVIEW() {
    LOG.info("testFindChildGroupWithVIEW");
    // Now get parent stem
    Stem  parent    = StemHelper.findByName(nrs, edu.getName());
    Set   children  = parent.getChildGroups();
    Assert.assertTrue(
      "children == " + children.size() + " (exp 2)",
      children.size() == 2
    );
    Iterator iter = children.iterator();
    while (iter.hasNext()) {
      Group child = (Group) iter.next();
      if      (child.getName().equals(i2.getName())) {
        Assert.assertTrue("i2", true);
      } 
      else if (child.getName().equals(uofc.getName())) {
        Assert.assertTrue("uofc", true);
      }
      else {
        Assert.fail("unknown child: " + child.getName());
      }
    }
  } // public void testFindGroupWithVIEW()

  public void testFindChildGroupWithAllVIEW() {
    LOG.info("testFindChildGroupWithAllVIEW");
    // Now get parent stem
    Stem  parent    = StemHelper.findByName(nrs, edu.getName());
    Set   children  = parent.getChildGroups();
    Assert.assertTrue(
      "children == " + children.size() + " (exp 2)",
      children.size() == 2
    );
    Iterator iter = children.iterator();
    while (iter.hasNext()) {
      Group child = (Group) iter.next();
      if      (child.getName().equals(i2.getName())) {
        Assert.assertTrue("i2", true);
        Assert.assertTrue("i2 parent", child.getParentStem().equals(parent));
      } 
      else if (child.getName().equals(uofc.getName())) {
        Assert.assertTrue("uofc", true);
        Assert.assertTrue("uofc parent", child.getParentStem().equals(parent));
      }
      else {
        Assert.fail("unknown child: " + child.getName());
      }
    }
  } // public void testFindGroupWithAllVIEW()

  public void testFindGroupWithoutVIEW() {
    LOG.info("testFindGroupWithoutVIEW");
    // ALL has VIEW
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithoutVIEW()

  public void testFindGroupWithVIEW() {
    LOG.info("testFindGroupWithVIEW");
    PrivHelper.grantPriv(s, i2, nrs.getSubject(), AccessPrivilege.VIEW);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithVIEW()

  public void testFindGroupWithAllVIEW() {
    LOG.info("testFindGroupWithAllVIEW");
    // Already exists
    // PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.findByUuid(nrs, i2.getUuid());
  } // public void testFindGroupWithAllVIEW()

  public void testAddGroupAsMemberWithADMIN() {
    LOG.info("testAddGroupAsMemberWithADMIN");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithADMIN()

  public void testAddGroupAsMemberWithAllADMIN() {
    LOG.info("testAddGroupAsMemberWithAllADMIN");
    PrivHelper.grantPriv(s, uofc, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithAllADMIN()

  public void testAddGroupAsMemberWithOPTIN() {
    LOG.info("testAddGroupAsMemberWithOPTIN");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithOPTIN()

  public void testAddGroupAsMemberWithAllOPTIN() {
    LOG.info("testAddGroupAsMemberWithAllOPTIN");
    PrivHelper.grantPriv(s, uofc, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithAllOPTIN()

  public void testAddGroupAsMemberWithOPTOUT() {
    LOG.info("testAddGroupAsMemberWithOPTOUT");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithOPTOUT()

  public void testAddGroupAsMemberWithAllOPTOUT() {
    LOG.info("testAddGroupAsMemberWithAllOPTOUT");
    PrivHelper.grantPriv(s, uofc, SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithAllOPTOUT()

  public void testAddGroupAsMemberWithREAD() {
    LOG.info("testAddGroupAsMemberWithREAD");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithREAD()

  public void testAddGroupAsMemberWithAllREAD() {
    LOG.info("testAddGroupAsMemberWithAllREAD");
    // Already exists
    // PrivHelper.grantPriv(s, uofc, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithAllREAD()

  public void testAddGroupAsMemberWithUPDATE() {
    LOG.info("testAddGroupAsMemberWithUPDATE");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithUPDATE()

  public void testAddGroupAsMemberWithAllUPDATE() {
    LOG.info("testAddGroupAsMemberWithAllUPDATE");
    PrivHelper.grantPriv(s, uofc, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithAllUPDATE()

  public void testAddGroupAsMemberWithVIEW() {
    LOG.info("testAddGroupAsMemberWithVIEW");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithVIEW()

  public void testAddGroupAsMemberWithAllVIEW() {
    LOG.info("testAddGroupAsMemberWithAllVIEW");
    // Already exists
    // PrivHelper.grantPriv(s, uofc, SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2,   subj0, AccessPrivilege.ADMIN);
    // Already exists
    GroupHelper.addMember(uofc, subj1, m);
    Group a = GroupHelper.findByName(nrs, uofc.getName());
    Group b = GroupHelper.findByName(nrs, i2.getName());
    // add uofc (a) to i2 (b)
    GroupHelper.addMember(b, a);
    MembershipTestHelper.testNumMship(b, Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(nrs, b, a, Group.getDefaultList());
    MembershipTestHelper.testEffMship(nrs, b, subj1, Group.getDefaultList(), a, 1);
  } // public void testAddGroupAsMemberWithAllVIEW()

}

