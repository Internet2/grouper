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
 * Test memberOf calculations.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMemberOf8.java,v 1.1 2006-03-06 20:21:50 blair Exp $
 */
public class TestMemberOf8 extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestMemberOf8.class); 


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;
  private static Group          ub;
  private static Group          uw;
  


  public TestMemberOf8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    ub    = StemHelper.addChildGroup(edu, "ub", "ub");
    uw    = StemHelper.addChildGroup(edu, "uw", "uw");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testIsMemberCustomListHasMemberCustomList() {
    LOG.info("testIsMemberCustomListHasMemberCustomList");
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "readers", 1, 1, 0);
    PrivHelper.grantPriv(s, uofc, i2.toSubject(), AccessPrivilege.READ);
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 1, 1, 0);
    MembershipHelper.testNumMship(uofc , "readers", 2, 2, 0);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    MembershipHelper.testImm(s, i2   , subj0         , "readers");
    MembershipHelper.testImm(s, uofc , i2.toSubject(), "readers"); 
    MembershipHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipHelper.testNumMship(uofc , "members", 0, 0, 0);
    MembershipHelper.testNumMship(i2   , "readers", 2, 2, 0);
    MembershipHelper.testNumMship(uofc , "readers", 2, 2, 0);
  } // public void testIsMemberCustomListHasMemberCustomList()

}

