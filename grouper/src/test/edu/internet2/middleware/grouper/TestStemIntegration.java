/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

package edu.internet2.middleware.grouper;
import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;

/**
 * @author  blair christensen.
 * @version $Id: TestStemIntegration.java,v 1.2 2009-10-20 14:55:50 shilen Exp $
 * @since   1.2.0
 */
public class TestStemIntegration extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = GrouperUtil.getLog(TestStemIntegration.class);


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStemIntegration("testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion"));
    //TestRunner.run(TestStemIntegration.class);
  }
  
  private Stem            parent;
  private GrouperSession  s;


  // TESTING INFRASTRUCTURE //

  public void setUp() {
    super.setUp();
    try {  
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  
  // TESTS //  

  /**
   * 
   */
  public TestStemIntegration() {
    super();
  }

  /**
   * @param name
   */
  public TestStemIntegration(String name) {
    super(name);
  }

  public void testSetExtension_NotPrivileged() {
    try {
      LOG.info("testSetExtension_NotPrivileged");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      r.startAllSession();
      i2mi.setExtension("i should not be privileged to set this");
      i2mi.store();
      fail("should have thrown InsufficientPrivilegeException");
    }
    catch (InsufficientPrivilegeException eIP) {
      assertTrue(true);
      assertEquals( E.CANNOT_STEM, eIP.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_NotPrivileged()

  public void testSetExtension_ChangeAsRoot() {
    try {
      LOG.info("testSetExtension_ChangeAsRoot");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      String  val   = "new extension";
      i2mi.setExtension(val);
      i2mi.store();

      assertEquals( "extn updated within session", val, i2mi.getExtension() );
      assertEquals( "name updated within session", val, i2mi.getName() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAsRoot()

  public void testSetExtension_ChangeAsNonRoot() {
    try {
      LOG.info("testSetExtension_ChangeAsNonRoot");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      i2mi.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
    
      // Change and verify in a new session
      Stem    ns  = StemFinder.findByName( r.startAllSession(), i2mi.getName(), true );
      String  val = "new extension";
      ns.setExtension(val);
      ns.store();
      assertEquals( "extn updated within session", val, ns.getExtension() );
      assertEquals( "name updated within session", val, ns.getName() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAsNonRoot()

  public void testSetExtension_ChangeAsRootAndPersistAcrossSessions() {
    try {
      LOG.info("testSetExtension_ChangeAsRootAndPersistAcrossSessions");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      String  val   = "new extension";
      i2mi.setExtension(val);
      i2mi.store();

      // Verify in another session
      Stem ns = StemFinder.findByName( r.startAllSession(), i2mi.getName(), true );
      assertEquals( "extn verification", i2mi.getExtension(), ns.getExtension() );
      assertEquals( "name verification", i2mi.getName(), ns.getName() );
    }
    catch (StemNotFoundException eNSNF) {
      fail( "did not find renamed stem by name: " + eNSNF.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAsRootAndPersistAcrossSessions()

  public void testSetExtension_ChangeAsNonRootAndPersistAcrossSessions() {
    try {
      LOG.info("testSetExtension_ChangeAsNonRootAndPersistAcrossSessions");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      i2mi.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );

      // Change in a new session
      Stem    ns  = StemFinder.findByName( r.startAllSession(), i2mi.getName(), true );
      String  val = "new extension";
      ns.setExtension(val);
      ns.store();
      
      // Verify in another session
      Stem verify = StemFinder.findByName( r.startAllSession(), ns.getName(), true );
      assertEquals( "extn verification", ns.getExtension(), verify.getExtension() );
      assertEquals( "name verification", ns.getName(), verify.getName() );
    }
    catch (StemNotFoundException eNSNF) {
      fail( "did not find renamed stem by name: " + eNSNF.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAsNonRootAndPersistAcrossSessions()

  public void testSetExtension_ChangeAndPropagateAsRoot() {
    try {
      LOG.info("testSetExtension_ChangeAndPropagateAsRoot");
      R       r     = R.getContext("grouper");
      Stem    i2mi  = r.getStem("i2mi");
      String  val   = "new extension";
      i2mi.setExtension(val);
      i2mi.store();

      // Verify propagation in a new session
      GrouperSession  s       = r.startAllSession();
      Stem            grouper = StemFinder.findByName(s, "new extension:grouper", true);
      assertEquals( "child stem extn verification", "grouper", grouper.getExtension() );
      assertEquals( "child stem name verification", val + ":grouper", grouper.getName() );
      Group           dev     = GroupFinder.findByName( s, grouper.getName() + ":grouper-dev" , true);
      assertEquals( "child group 0 extn verification", "grouper-dev", dev.getExtension());
      assertEquals( "child group 0 name verification", grouper.getName() + ":" + dev.getExtension(), dev.getName() );
      Group           users   = GroupFinder.findByName( s, grouper.getName() + ":grouper-users", true );
      assertEquals( "child group 1 extn verification", "grouper-users", users.getExtension());
      assertEquals( "child group 1 name verification", grouper.getName() + ":" + users.getExtension(), users.getName() );
    }
    catch (GroupNotFoundException eGNF) {
      
      fail( "did not find renamed group by name: " + ExceptionUtils.getFullStackTrace(eGNF) );
    }  
    catch (StemNotFoundException eNSNF) {
      fail( "did not find renamed stem by name: " + ExceptionUtils.getFullStackTrace(eNSNF) );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAndPropagateAsRoot()

  public void testSetExtensionAndDisplayExtension_ChangeAndPropagateAsNonRoot() {
    try {
      LOG.info("testSetExtension_ChangeAndPropagateNonAsRoot");
      R       r     = R.getContext("grouper");
      Stem    i2mi  = r.getStem("i2mi");
      i2mi.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );

      // Change in a new session
      Stem    ns  = StemFinder.findByName( r.startAllSession(), i2mi.getName(), true );
      String  val = "new extension";
      ns.setExtension(val);
      ns.setDisplayExtension(val);
      
      ns.store();

      // Verify propagation in a new session
      GrouperSession  s       = r.startAllSession();
      Stem            grouper = StemFinder.findByName(s, "new extension:grouper", true);
      assertEquals( "child stem extn verification", "grouper", grouper.getExtension() );
      assertEquals( "child stem name verification", val + ":grouper", grouper.getName() );
      Group           dev     = GroupFinder.findByName( s, grouper.getName() + ":grouper-dev", true );
      assertEquals( "child group 0 extn verification", "grouper-dev", dev.getExtension());
      assertEquals( "child group 0 name verification", grouper.getName() + ":" + dev.getExtension(), dev.getName() );
      Group           users   = GroupFinder.findByName( s, grouper.getName() + ":grouper-users", true );
      assertEquals( "child group 1 extn verification", "grouper-users", users.getExtension());
      assertEquals( "child group 1 name verification", grouper.getName() + ":" + users.getExtension(), users.getName() );
    }
    catch (GroupNotFoundException eGNF) {
      fail( "did not find renamed group by name: " + eGNF.getMessage() );
    }  
    catch (StemNotFoundException eNSNF) {
      fail( "did not find renamed stem by name: " + eNSNF.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAndPropagateAsNonRoot()

  public void testSetExtension_ChangeAndPropagateAsNonRoot() {
    try {
      LOG.info("testSetExtension_ChangeAndPropagateNonAsRoot");
      R       r     = R.getContext("grouper");
      Stem    i2mi  = r.getStem("i2mi");
      i2mi.grantPriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );

      // Change in a new session
      Stem    ns  = StemFinder.findByName( r.startAllSession(), i2mi.getName(), true );
      String  val = "new extension";
      ns.setExtension(val);
      
      ns.store();

      // Verify propagation in a new session
      GrouperSession  s       = r.startAllSession();
      Stem            grouper = StemFinder.findByName(s, "new extension:grouper", true);
      assertEquals( "child stem extn verification", "grouper", grouper.getExtension() );
      assertEquals( "child stem name verification", val + ":grouper", grouper.getName() );
      Group           dev     = GroupFinder.findByName( s, grouper.getName() + ":grouper-dev", true );
      assertEquals( "child group 0 extn verification", "grouper-dev", dev.getExtension());
      assertEquals( "child group 0 name verification", grouper.getName() + ":" + dev.getExtension(), dev.getName() );
      Group           users   = GroupFinder.findByName( s, grouper.getName() + ":grouper-users", true );
      assertEquals( "child group 1 extn verification", "grouper-users", users.getExtension());
      assertEquals( "child group 1 name verification", grouper.getName() + ":" + users.getExtension(), users.getName() );
    }
    catch (GroupNotFoundException eGNF) {
      fail( "did not find renamed group by name: " + eGNF.getMessage() );
    }  
    catch (StemNotFoundException eNSNF) {
      fail( "did not find renamed stem by name: " + eNSNF.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ChangeAndPropagateAsNonRoot()

  public void testInternal_AddChildGroup_CreateNewMemberAfterRegistryReset() {
    try {
      LOG.info("testInternal_AddChildGroup_CreateNewMemberAfterRegistryReset");
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      Stem            root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      Stem            ns    = root.addChildStem("nsit", "nsit");
      Group           g     = ns.addChildGroup("nas", "nas");
      Member          m     = g.toMember();
  
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      ns    = root.addChildStem("nsit", "nsit");
      g     = ns.internal_addChildGroup("nas", "nas", g.getUuid() );
      assertTrue("new member definition created", !m.getUuid().equals( g.toMember().getUuid() ) );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testInternal_AddChildGroup_CreateNewMemberAfterRegistryReset()

  // TESTS //  
  
  public void testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion() {
    try {
      LOG.info("testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion");
      RegistryReset.reset();
      GrouperTest.initGroupsAndAttributes();
      R               r     = new R();
      GrouperSession  s     = r.getSession();
      Stem            root  = StemFinder.findRootStem(s).addChildStem("uchicago", "uchicago");
      Stem            ns    = root.addChildStem("nsit", "nsit");
      Group           g     = ns.addChildGroup("nas", "nas");
      Member          m     = g.toMember();
  
      g.delete();
      g = ns.internal_addChildGroup("nas", "nas", g.getUuid() );
      assertEquals( "reusing existing member definition", m, g.toMember() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testInternal_AddChildGroup_ReuseExistingMemberAfterGroupDeletion()

  /**
   * Verify that <i>Stem</i> is assigned UUID if blank UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildStem_assignUuidIfBlankUuidArgument() {
    try {
      Stem              ns  = parent.internal_addChildStem( "ns", "ns", GrouperConfig.EMPTY_STRING );
      GrouperValidator  v   = NotNullOrEmptyValidator.validate( ns.getUuid() );
      assertTrue( "assigned uuid when blank uuid arg", v.isValid() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
    catch (StemAddException eNSA) {
      fail( eNSA.getMessage() );
    }
  }

  // TESTS //
  
  /**
   * Verify that <i>Stem</i> is assigned UUID if null UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildStem_assignUuidIfNullUuidArgument() {
    try {
      Stem              ns  = parent.internal_addChildStem("ns", "ns", null);
      GrouperValidator  v   = NotNullOrEmptyValidator.validate( ns.getUuid() );
      assertTrue( "assigned uuid when null uuid arg", v.isValid() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
    catch (StemAddException eNSA) {
      fail( eNSA.getMessage() );
    }
  }

  /**
   * Verify that <i>Group</i> is assigned UUID if blank UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildGroup_assignUuidIfBlankUuidArgument() {
    try {
      Group             g = parent.internal_addChildGroup( "g", "g", GrouperConfig.EMPTY_STRING );
      GrouperValidator  v = NotNullOrEmptyValidator.validate( g.getUuid() );
      assertTrue( "assigned uuid when blank uuid arg", v.isValid() );
    }
    catch (GroupAddException eGA) {
      fail( eGA.getMessage() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
  }

  // TESTS //
  
  /**
   * Verify that <i>Group</i> is assigned UUID if null UUID argument.
   * @since   1.2.0
   */
  public void test_internal_addChildGroup_assignUuidIfNullUuidArgument() {
    try {
      Group             g = parent.internal_addChildGroup("g", "g", null);
      GrouperValidator  v = NotNullOrEmptyValidator.validate( g.getUuid() );
      assertTrue( "assigned uuid when null uuid arg", v.isValid() );
    }
    catch (GroupAddException eGA) {
      fail( eGA.getMessage() );
    }
    catch (InsufficientPrivilegeException eIP) {
      fail( eIP.getMessage() );
    }
  }
} // public class Test_Integration_Stem_setExtension extends GrouperTest

