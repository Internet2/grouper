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
 * Test use of the STEM {@link NamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivSTEM5.java,v 1.1 2006-03-10 19:36:36 blair Exp $
 */
public class TestPrivSTEM5 extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestPrivSTEM5.class);
  private static final Privilege  PRIV  = NamingPrivilege.STEM;


  public TestPrivSTEM5(String name) {
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

  // Revoke all CREATE without STEM 
  public void testRevokeAllCreateFail() {
    LOG.info("testRevokeAllCreateFail");
    // Get root and !root sessions
    LOG.debug("testRevokeAllCreateFail.0");
    GrouperSession  s       = SessionHelper.getRootSession();
    LOG.debug("testRevokeAllCreateFail.1");
    GrouperSession  nrs     = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
    LOG.info("testRevokeAllCreateFail.2");
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    LOG.debug("testRevokeAllCreateFail.3");
    // Now fail to revoke priv as !root 
    PrivHelper.revokePrivFail(nrs, nrroot, NamingPrivilege.CREATE); 
    LOG.debug("testRevokeAllCreateFail.4");
  } // public void testRevokeCreateFail()

}

