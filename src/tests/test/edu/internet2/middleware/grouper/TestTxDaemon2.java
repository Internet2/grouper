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
import  junit.extensions.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * <p />
 * @author  blair christensen.
 * @version $Id: TestTxDaemon2.java,v 1.1 2006-02-21 20:55:46 blair Exp $
 */
public class TestTxDaemon2 extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(TestTxDaemon2.class); 

  // Private Class Variables
/*
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;
*/
  

  public TestTxDaemon2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
/*
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
*/
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestTxDaemon2.class);
    return new RepeatedTest(suite, 10);
  } // static public Test suite()

  public void testHeisenbug() {
    LOG.info("testHeisenbug");
    Assert.assertTrue(true);
    // testGroupDelete(test.edu.internet2.middleware.grouper.TestGroupDelete)
    // testDeleteGroupIsMemberWithADMIN(test.edu.internet2.middleware.grouper.TestPrivADMIN)
    // testGroupAnyAttributeFilterSomething(test.edu.internet2.middleware.grouper.TestGQGroupAnyAttribute)
    // testToGroup(test.edu.internet2.middleware.grouper.TestMemberToGroup)
  } // public void testHeisenbug()

}

