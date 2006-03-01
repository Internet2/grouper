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
 * @version $Id: TestTxDaemon2.java,v 1.2 2006-03-01 19:52:58 blair Exp $
 */
public class TestTxDaemon2 extends TestCase {

  private static final Log  LOG = LogFactory.getLog(TestTxDaemon2.class); 

  public TestTxDaemon2(String name) {
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

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestTxDaemon2.class);
    return new RepeatedTest(suite, 150); // Failure rate: ~0.5-1.5%
  } // static public Test suite()

  public void testHeisenbug() {
    LOG.info("testHeisenbug");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemHelper.findRootStem(s);
      Stem            edu   = root.addChildStem("edu" , "education");
      Group           uc    = edu.addChildGroup("uc"  , "uchicago");
      Subject         subj  = SubjectFinder.findById(uc.getUuid(), "group");
      Assert.assertNotNull("TH.FIND.SUBJ.NULL!", subj);
      //  For reasons I'm still not clear on, this would fail ~0.5-1.5%
      //  of the time after I added the updater thread, reporting a
      //  jdbc batch update error.  As I couldn't
      //  identify-and-eliminate this particular Heisenbug I decided to
      //  bypass it and all groups get created as members upon group
      //  creation now, rather than when first referenced as a member.
      Member            m   = MemberFinder.findBySubject(uc.getSession(), subj);
      Assert.assertNotNull("TH.FIND.MEMBER.NULL!", m);
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("fail: " + e.getMessage());
    }
  } // public void testHeisenbug()

}

