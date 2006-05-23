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


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test use of the CREATE {@link NamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivCREATE.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class TestPrivCREATE extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestPrivCREATE.class); 
  private static final Privilege  PRIV  = NamingPrivilege.CREATE;


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
    RegistryReset.resetRegistryAndAddTestSubjects();
    a     = null;
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    subj0 = SubjectTestHelper.SUBJ0;
    nrs   = SessionHelper.getSession(subj0.getId());
    GroupHelper.addMember(i2, subj0, "members");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testCreateChildGroupWithoutCREATE() {
    LOG.info("testCreateChildGroupWithoutCREATE");
    a = StemHelper.findByName(nrs, edu.getName());
    StemHelper.addChildGroupFail(a, "uofc", "uchicago");
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
    MembershipTestHelper.testImm(s, i2, subj0, "members");
    PrivHelper.grantPriv(s, edu, i2.toSubject(), NamingPrivilege.CREATE);
    Assert.assertTrue("i2 has priv", edu.hasCreate(i2.toSubject()));
    Assert.assertTrue("subj0 has priv", edu.hasCreate(subj0));
    a = StemHelper.findByName(nrs, edu.getName());
    StemHelper.addChildGroup(a, "uofc", "uchicago");
  } // public void testCreateChildGroupWithGroupCREATE()

} 

