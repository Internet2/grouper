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
 * @version $Id: TestPrivSTEM10.java,v 1.1 2006-03-10 19:36:36 blair Exp $
 */
public class TestPrivSTEM10 extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestPrivSTEM10.class);
  private static final Privilege  PRIV  = NamingPrivilege.STEM;


  public TestPrivSTEM10(String name) {
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

  // Revoke CREATE with STEM 
  public void testRevokeCreate() {
    LOG.info("testRevokeCreate");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectHelper.SUBJ1, NamingPrivilege.CREATE);
    // Other !root should now have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectHelper.SUBJ1, NamingPrivilege.CREATE, true);
    // Now revoke priv as !root from another !root
    PrivHelper.revokePriv(nrs, nrroot, SubjectHelper.SUBJ1, NamingPrivilege.CREATE);
    // Other !root should now not have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectHelper.SUBJ1, NamingPrivilege.CREATE, false);
  } // public void testRevokeCreate()

}

