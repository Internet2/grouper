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
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestWheelGroup2.java,v 1.1 2006-09-11 18:53:12 blair Exp $
 * @since   1.1.0
 */
public class TestWheelGroup2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestWheelGroup2.class);

  public TestWheelGroup2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGrantAdminWithAllWheel() {
    LOG.info("testGrantAdminWithAllWheel");
    try {
      R       r     = R.populateRegistry(1, 1, 2);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      
      // Enable wheel
      Stem    etc   = r.root.addChildStem("etc", "etc");
      Group   wheel = etc.addChildGroup("wheel", "wheel");
      wheel.addMember(SubjectFinder.findAllSubject());
      GrouperConfig.setProperty(GrouperConfig.GWU, "true"     );
      GrouperConfig.setProperty(GrouperConfig.GWG, "etc:wheel");

      GrouperSession nrs = GrouperSession.start(subjA);
      Assert.assertTrue("is root due to all", RootPrivilegeResolver.isRoot(nrs));
      gA.setSession(nrs);
      gA.grantPriv(subjB, AccessPrivilege.ADMIN);
      Assert.assertTrue("granted privilege due to all being wheel", true);
      nrs.stop();
      r.rs.stop(); 
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGrantAdminWithAllWheel()

} // public class TestWheelGroup2

