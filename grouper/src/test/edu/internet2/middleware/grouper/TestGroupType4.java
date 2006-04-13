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
 * @author  blair christensen.
 * @version $Id: TestGroupType4.java,v 1.1.2.1 2006-04-13 00:35:33 blair Exp $
 */
public class TestGroupType4 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupType4.class);

  public TestGroupType4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddAndDeleteCustomTypeAsNonRoot() {
    LOG.info("testAddAndDeleteCustomTypeAsNonRoot");
    try {
      R r = R.populateRegistry(1, 1, 1);

      String    tName = "test type";
      // Create the custom type as root
      GroupType type  = GroupType.createType(r.rs, tName);

      // Now allow a non-root subj to admin this group
      Group   g     = r.getGroup("a", "a");
      Subject subj  = r.getSubject("a");
      g.grantPriv(subj, AccessPrivilege.ADMIN);

      // Now start non-root session and add+delete group type as non-root
      GrouperSession nrs = GrouperSession.start(subj);
      g.setSession(nrs);
      Assert.assertFalse( "no custom type"      , g.hasType(type) );
      g.addType(type);
      Assert.assertTrue(  "now has custom type" , g.hasType(type) );
      g.deleteType(type);
      Assert.assertFalse( "custom type removed" , g.hasType(type) );
      nrs.stop();

      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testAddAndDeleteCustomTypeAsNonRoot()

}

