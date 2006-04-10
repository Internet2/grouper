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
 * Test use of the wheel group.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestWheelGroup.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
 */
public class TestWheelGroup extends TestCase {
  // @test  MANUAL

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestWheelGroup.class);


  // Private Class Variables
  private static Group          a;
  private static Stem           edu;
  private static Stem           grpr;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;
  private static Group          wheel;
  private static  Stem          your;


  public TestWheelGroup(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    your  = StemHelper.addChildStem(root, "your", "your");
    grpr  = StemHelper.addChildStem(your, "grouper", "grouper");
    wheel = StemHelper.addChildGroup(grpr, "wheel", "wheel");
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectTestHelper.SUBJ0;
    subj1 = SubjectTestHelper.SUBJ1;
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.VIEW);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  // Tests

  public void testGrantAdminWithoutWHEEL() {
    LOG.info("testGrantAdminWithoutWHEEL");
    nrs = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    a   = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithoutWHEEL()

  public void testGrantAdminWithWHEEL() {
    LOG.info("testGrantAdminWithWHEEL");
    GroupHelper.addMember(wheel, subj0, "members");
    MembershipHelper.testImm(s, wheel, subj0, "members");
    nrs = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    a   = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithWHEEL()

  public void testGrantAdminWithAllWHEEL() {
    LOG.info("testGrantAdminWithAllWHEEL");
    GroupHelper.addMember(wheel, SubjectFinder.findAllSubject(), "members");    
    nrs = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    a   = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithWHEEL()

}

