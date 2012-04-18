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
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the OPTIN {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivOPTIN.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestPrivOPTIN extends TestCase {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestPrivOPTIN.class);

  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;


  public TestPrivOPTIN(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    nrs   = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    subj0 = SubjectTestHelper.SUBJ0;
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
    //TestRunner.run(new TestPrivOPTIN("testGrantedToCreator"));
    TestRunner.run(TestPrivOPTIN.class);
  }

  public void testAddSelfAsMemberWithoutADMIN() {
    LOG.info("testAddSelfAsMemberWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    final Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(nrs, a, subj0);
  } // public void testAddSelfAsMemberWithoutADMIN

  public void testAddSelfAsMemberWithADMIN() {
    LOG.info("testAddSelfAsMemberWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj0);
  } // public void testAddSelfAsMemberWithADMIN

  public void testAddSelfAsMemberWithAllADMIN() {
    LOG.info("testAddSelfAsMemberWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj0);
  } // public void testAddSelfAsMemberWithAllADMIN

  public void testAddSelfAsMemberWithoutOPTIN() {
    LOG.info("testAddSelfAsMemberWithoutOPTIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    final Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(nrs, a, subj0);
    
  } // public void testAddSelfAsMemberWithoutOPTIN
 
  public void testAddSelfAsMemberWithOPTIN() {
    LOG.info("testAddSelfAsMemberWithOPTIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj0);
  } // public void testAddSelfAsMemberWithOPTIN

  public void testAddSelfAsMemberWithAllOPTIN() {
    LOG.info("testAddSelfAsMemberWithAllOPTIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj0);
  } // public void testAddSelfAsMemberWithAllOPTIN

  public void testAddSelfAsMemberWithoutUPDATE() {
    LOG.info("testAddSelfAsMemberWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    final Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(nrs, a, subj0);
  } // public void testAddSelfAsMemberWithoutUPDATE
 
  public void testAddSelfAsMemberWithUPDATE() {
    LOG.info("testAddSelfAsMemberWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj0);
  } // public void testAddSelfAsMemberWithUPDATE

  public void testAddSelfAsMemberWithAllUPDATE() {
    LOG.info("testAddSelfAsMemberWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj0);
  } // public void testAddSelfAsMemberWithAllUPDATE

}

