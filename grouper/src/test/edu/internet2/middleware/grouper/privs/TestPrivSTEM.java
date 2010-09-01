/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.privs;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test use of the STEM {@link NamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivSTEM.java,v 1.2 2009-11-01 20:49:03 shilen Exp $
 */
public class TestPrivSTEM extends GrouperTest {

  // Private Class Constants
  private static final Log        LOG   = GrouperUtil.getLog(TestPrivSTEM.class);
  private static final Privilege  PRIV  = NamingPrivilege.STEM;


  public TestPrivSTEM(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new TestPrivSTEM("testRevokeStem"));
    //TestRunner.run(TestPrivSTEM.class);
  }

  // Tests

  // Grant CREATE with STEM 
  public void testGrantCreate() {
    LOG.info("testGrantCreate");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    // Other !root should now have STEM 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, true);
  } // public void testGrantCreate()

  // Grant STEM with STEM 
  public void testGrantStem() {
    LOG.info("testGrantStem");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV);
    // Other !root should now have STEM 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV, true);
  } // public void testGrantStem()

  // Revoke all CREATE with STEM 
  public void testRevokeAllCreate() {
    LOG.info("testRevokeAllCreate");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    // Other !root should now have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, true);
    // Now revoke priv as !root 
    PrivHelper.revokePriv(nrroot, NamingPrivilege.CREATE);
    // Other !root should now not have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, false);
  } // public void testRevokeAllCreate()

  // Revoke all STEM with STEM 
  public void testRevokeAllStem() {
    LOG.info("testRevokeAllStem");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV);
    // Other !root should now have priv 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV, true);
    // Now revoke priv as !root 
    PrivHelper.revokePriv(nrroot, PRIV);
    // Other !root should no longer have priv 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV, false);
  } // public void testRevokeAllStem()

  // Revoke CREATE with STEM 
  public void testRevokeCreate() {
    LOG.info("testRevokeCreate");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    // Other !root should now have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, true);
    // Now revoke priv as !root from another !root
    PrivHelper.revokePriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    // Other !root should now not have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, false);
  } // public void testRevokeCreate()

  // Revoke STEM with STEM 
  public void testRevokeStem() {
    LOG.info("testRevokeStem");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV);
    // Other !root should now have STEM 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV, true);
    // Now revoke priv as !root from another !root
    PrivHelper.revokePriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV);
    // Other !root should no longer have STEM 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV, false);
  } // public void testRevokeStem()

  // Create child stem without STEM
  public void testCreateChildStemFail() {
    LOG.info("testCreateChildStemFail");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    StemHelper.findRootStem(s);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Fail to add child stem
    StemHelper.addChildStemFail(nrroot, "edu", "educational");
  } // public void testCreateChildStemFail()

  // Create child stem with STEM
  public void testCreateChildStem() {
    LOG.info("testCreateChildStem");
    // Get root and !root sessions
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    GrouperSession  s       = SessionHelper.getRootSession();
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now grant priv as !root to another !root
    PrivHelper.grantPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV);
    // Add child stem
    StemHelper.addChildStem(nrroot, "edu", "educational");
  } // public void testCreateChildStem()

  // Modify stem attrs without STEM
  public void testModifyAttrsFail() {
    LOG.info("testModifyAttrsFail");
    
    Stem root = StemHelper.findRootStem(SessionHelper.getRootSession());
    root.addChildStem("test", "test");
    
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    Stem nonRoot = StemHelper.findByName(nrs, "test");
    // Now fail to modify stem attrs
    StemHelper.setAttrFail(nrroot, "description", "foo");
    StemHelper.setAttrFail(nonRoot, "description", "foo");
    StemHelper.setAttrFail(nonRoot, "displayExtension", "foo");
  } // public void testModifyAttrsFail()

  // Modify stem attrs with STEM
  public void testModifyttrs() {
    try {
      LOG.info("testModifyAttrs");
      // Get root and !root sessions
      GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
      GrouperSession  s       = SessionHelper.getRootSession();
      // Get root and non-root stem and grant STEM on it to !root subject
      Stem            root    = StemHelper.findRootStem(s);
      PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
      
      Stem nonRoot = root.addChildStem("test", "test");
      PrivHelper.grantPriv(s, nonRoot, nrs.getSubject(), PRIV);
      
      // Now get root and nonRoot as !root subject 
      Stem            nrroot  = StemHelper.findRootStem(nrs);
      nonRoot = StemHelper.findByName(nrs, "test");
      // Now modify stem attrs
      StemHelper.setAttr(nrroot, "description", "foo");
      StemHelper.setAttr(nonRoot, "description", "foo");
      StemHelper.setAttr(nonRoot, "displayExtension", "foo");
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testModifyAttrs()

} // public class TestPrivSTEM extends GrouperTest

