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
 * Test use of the READ {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivREAD2.java,v 1.1 2006-03-23 18:36:31 blair Exp $
 */
public class TestPrivREAD2 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestPrivREAD2.class);

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


  public TestPrivREAD2(String name) {
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
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0   = SubjectHelper.SUBJ0;
    subj1   = SubjectHelper.SUBJ1;
    m     = Helper.getMemberBySubject(nrs, subj1);
    GroupHelper.addMember(i2, subj1, m);
    GroupHelper.setAttr(i2, "description", "a description");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testReadAttrsWithAllADMIN() {
    LOG.info("testReadAttrsWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithAllADMIN()

}

