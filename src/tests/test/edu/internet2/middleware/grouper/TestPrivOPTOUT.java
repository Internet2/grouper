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
 * Test use of the OPTOUT {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivOPTOUT.java,v 1.3 2006-02-03 19:38:53 blair Exp $
 */
public class TestPrivOPTOUT extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestPrivOPTOUT.class);

  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;


  public TestPrivOPTOUT(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    nrs   = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    subj0 = SubjectHelper.SUBJ0;
    m     = Helper.getMemberBySubject(nrs, subj0);
    GroupHelper.addMemberUpdate(i2, subj0, m);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  // Tests

  public void testDelSelfAsMemberWithoutADMIN() {
    LOG.info("testDelSelfAsMemberWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(a, subj0, m);
  } // public void testDelSelfAsMemberWithoutADMIN
 
  public void testDelSelfAsMemberWithADMIN() {
    LOG.info("testDelSelfAsMemberWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj0, m);
  } // public void testDelSelfAsMemberWithADMIN

  public void testDelSelfAsMemberWithAllADMIN() {
    LOG.info("testDelSelfAsMemberWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj0, m);
  } // public void testDelSelfAsMemberWithAllADMIN

  public void testDelSelfAsMemberWithoutOPTOUT() {
    LOG.info("testDelSelfAsMemberWithoutOPTOUT");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(a, subj0, m);
  } // public void testDelSelfAsMemberWithoutOPTOUT
 
  public void testDelSelfAsMemberWithOPTOUT() {
    LOG.info("testDelSelfAsMemberWithOPTOUT");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj0, m);
  } // public void testDelSelfAsMemberWithOPTOUT

  public void testDelSelfAsMemberWithAllOPTOUT() {
    LOG.info("testDelSelfAsMemberWithAllOPTOUT");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj0, m);
  } // public void testDelSelfAsMemberWithAllOPTOUT

  public void testDelSelfAsMemberWithoutUPDATE() {
    LOG.info("testDelSelfAsMemberWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(a, subj0, m);
  } // public void testDelSelfAsMemberWithoutUPDATE
 
  public void testDelSelfAsMemberWithUPDATE() {
    LOG.info("testDelSelfAsMemberWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj0, m);
  } // public void testDelSelfAsMemberWithUPDATE

  public void testDelSelfAsMemberWithAllUPDATE() {
    LOG.info("testDelSelfAsMemberWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj0, m);
  } // public void testDelSelfAsMemberWithAllUPDATE

}

