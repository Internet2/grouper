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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test use of the STEM {@link NamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivSTEM.java,v 1.4 2006-08-30 19:31:02 blair Exp $
 */
public class TestPrivSTEM extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestPrivSTEM.class);
  private static final Privilege  PRIV  = NamingPrivilege.STEM;


  public TestPrivSTEM(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  // Tests

  public void testGrantedToCreator() {
    LOG.info("testGrantedToCreator");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Create stem as !root subject.  
    Stem            edu     = StemHelper.addChildStem(nrroot, "edu", "education");
    // !root subject should have STEM on the new child stem.
    PrivHelper.hasPriv(nrs, edu, nrs.getSubject(), PRIV, true);
  } // public void testGrantedToCreator()

  // Grant CREATE without STEM 
  public void testGrantCreateFail() {
    LOG.info("testGranteCreateFail");
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now fail to grant priv as !root to another !root
    PrivHelper.grantPrivFail(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE); 
  } // public void testGrantCreateFail()

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

  // Grant STEM without STEM 
  public void testGrantStemFail() {
    LOG.info("testGrantStemFail");
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now fail to grant priv as !root to another !root
    PrivHelper.grantPrivFail(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV); 
  } // public void testGrantStemFail()

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

  // Revoke all CREATE without STEM 
  public void testRevokeAllCreateFail() {
    LOG.info("testRevokeAllCreateFail");
    // Get root and !root sessions
    LOG.debug("testRevokeAllCreateFail.0");
    LOG.debug("testRevokeAllCreateFail.1");
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    LOG.info("testRevokeAllCreateFail.2");
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    LOG.debug("testRevokeAllCreateFail.3");
    // Now fail to revoke priv as !root 
    PrivHelper.revokePrivFail(nrs, nrroot, NamingPrivilege.CREATE); 
    LOG.debug("testRevokeAllCreateFail.4");
  } // public void testRevokeCreateFail()

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
    PrivHelper.revokePriv(nrs, nrroot, NamingPrivilege.CREATE);
    // Other !root should now not have CREATE 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE, false);
  } // public void testRevokeAllCreate()

  // Revoke all STEM without STEM 
  public void testRevokeAllStemFail() {
    LOG.info("testRevokeAllStemFail");
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now fail to revoke priv as !root from another !root
    PrivHelper.revokePrivFail(nrs, nrroot, PRIV);
  } // public void testRevokeAllStemFail()

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
    PrivHelper.revokePriv(nrs, nrroot, PRIV);
    // Other !root should no longer have priv 
    PrivHelper.hasPriv(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV, false);
  } // public void testRevokeAllStem()

  // Revoke CREATE without STEM 
  public void testRevokeCreateFail() {
    LOG.info("testRevokeCreateFail");
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now fail to revoke priv as !root from another !root
    PrivHelper.revokePrivFail(nrs, nrroot, SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE); 
  } // public void testRevokeCreateFail()

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

  // Revoke STEM without STEM 
  public void testRevokeStemFail() {
    LOG.info("testRevokeStemFail");
    // Get !root session
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now fail to revoke priv as !root from another !root
    PrivHelper.revokePrivFail(nrs, nrroot, SubjectTestHelper.SUBJ1, PRIV);
  } // public void testRevokeStemFail()

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
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
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
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now fail to modify stem attrs
    StemHelper.setAttrFail(nrroot, "description", "foo");
    StemHelper.setAttrFail(nrroot, "displayExtension", "foo");
  } // public void testModifyAttrsFail()

  // Modify stem attrs with STEM
  public void testModifyttrs() {
    LOG.info("testModifyAttrs");
    // Get root and !root sessions
    GrouperSession  s       = SessionHelper.getRootSession();
    GrouperSession  nrs     = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    // Get root stem and grant STEM on it to !root subject
    Stem            root    = StemHelper.findRootStem(s);
    PrivHelper.grantPriv(s, root, nrs.getSubject(), PRIV);
    // Now get root as !root subject 
    Stem            nrroot  = StemHelper.findRootStem(nrs);
    // Now modify stem attrs
    StemHelper.setAttr(nrroot, "description", "foo");
    StemHelper.setAttr(nrroot, "displayExtension", "foo");
  } // public void testModifyAttrs()

}

