/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * Test open bugs.  
 * <p />
 * @author  blair christensen.
 * @version $Id: TestBugsOpen.java,v 1.9.2.1 2006-01-09 16:42:02 blair Exp $
 */
public class TestBugsOpen extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestBugsOpen.class);


  public TestBugsOpen(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testNoOpenBugs() {
    LOG.info("testNoOpenBugs");
    Assert.assertTrue("to keep junit from kvetching about no tests", true);
  } // public void testNoOpenBugs()

  // @source  Gary Brown, 20051221, <B96A40BBB6DC736573C06C6D@cse-gwb.cse.bris.ac.uk>
  // @status  potentially confirmed, potentially fixed
  public void testSetStemDisplayName() {
    LOG.info("testSetStemDisplayName");
    // Setup
    Subject subj0 = SubjectHelper.SUBJ0;
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            qsuob = root.addChildStem("qsuob", "qsuob");
      qsuob.grantPriv(subj0, NamingPrivilege.STEM);
      Stem            cs    = qsuob.addChildStem("cs", "child stem");
      // These weren't explicitly listed in the test report but I can't
      // replicate unless I have at least two groups.
      Group           cg    = qsuob.addChildGroup("cg", "child group");
      Group           gcg   = cs.addChildGroup("gcg", "grandchild group");
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    // Test
    try {
      GrouperSession  nrs   = GrouperSession.start(subj0);
      Stem            qsuob = StemFinder.findByName(nrs, "qsuob");
      String          de    = "QS University of Bristol";
      qsuob.setDisplayExtension(de);
      String          val   = qsuob.getDisplayExtension();
      Assert.assertTrue("updated displayExtn: " + val, de.equals(val));
      val                   = qsuob.getDisplayName();
      Assert.assertTrue("updated displayName: " + val, de.equals(val));
      nrs.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testStemDisplayName()

}

