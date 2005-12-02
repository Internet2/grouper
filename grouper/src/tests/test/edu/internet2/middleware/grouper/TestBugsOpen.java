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

/**
 * Test open bugs.  
 * <p />
 * @author  blair christensen.
 * @version $Id: TestBugsOpen.java,v 1.1 2005-12-02 18:39:42 blair Exp $
 */
public class TestBugsOpen extends TestCase {

  public TestBugsOpen(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  // Gary Brown, 20051202, <C76C2307ED5A17415027C3D2@cse-gwb.cse.bris.ac.uk>
  public void testGrantStemToGroup() {
    try {
      // Setup
      GrouperSession  s     = GrouperSession.startSession(
        SubjectFinder.findById("GrouperSystem", "application")
      );
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "educational");
      Group           i2    = edu.addChildGroup("i2", "internet2");
      Subject         subj0 = SubjectFinder.findById("test.subject.0");
      i2.addMember(subj0);
      Assert.assertTrue("i2 has mem subj0", i2.hasMember(subj0));
      Assert.assertTrue("i2 has imm mem subj0", i2.hasImmediateMember(subj0));
      // Test
      Stem            ns    = StemFinder.findByName(s, edu.getName());
      Assert.assertNotNull("ns !null", ns);
      Group           g     = GroupFinder.findByName(s, i2.getName());
      Assert.assertNotNull("g !null", g);
      ns.grantPriv(
        SubjectFinder.findById(g.getUuid()),
        Privilege.getInstance("stem")
      );
      Assert.assertTrue("g (ns) has STEM", g.toMember().hasStem(ns));
      Assert.assertTrue("g (m) has STEM",  ns.hasStem(g.toSubject()));
    }
    catch (Exception e) {
      Assert.fail("exception: " + e.getMessage());
    }
  } // public void testGrantStemToGroup()


}

