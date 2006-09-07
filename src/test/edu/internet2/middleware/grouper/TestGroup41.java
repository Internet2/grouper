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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup41.java,v 1.1 2006-09-07 15:08:15 blair Exp $
 * @since   1.1.0
 */
public class TestGroup41 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestGroup41.class);

  public TestGroup41(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testRevokeDefaultPrivilegeForEntireGroup() {
    LOG.info("testRevokeDefaultPrivilegeForEntireGroup");
    try {
      R         r   = R.populateRegistry(1, 1, 0);
      Group     gA  = r.getGroup("a", "a");
      Subject   all = SubjectFinder.findAllSubject();
      Assert.assertTrue("ALL has VIEW", gA.hasView(all));
      Assert.assertTrue("ALL has READ", gA.hasRead(all));
      gA.revokePriv(AccessPrivilege.VIEW);
      gA.revokePriv(AccessPrivilege.READ);
      Assert.assertFalse("ALL !has VIEW", gA.hasView(all));
      Assert.assertFalse("ALL !has READ", gA.hasRead(all));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testRevokeDefaultPrivilegeForEntireGroup()

} // public class TestGroup41

