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
import  edu.internet2.middleware.subject.*;

/**
 * Test naming privilege use cases.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_uc_NamingPrivs.java,v 1.2 2007-08-24 19:42:50 blair Exp $
 * @since   @HEAD@
 */
public class Test_uc_NamingPrivs extends GrouperTest {


  private R       r;
  private Subject subjA, subjB;


  public void setUp() {
    super.setUp();
    try {
      r     = R.getContext("grouper");
      r.root.addChildStem("etc", "etc");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  /**
   * @since   @HEAD@
   */
  public void test_grant_stemGrantedToCreator() 
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException,
            SessionException,
            StemAddException
  {
    r.root.grantPriv(this.subjA, NamingPrivilege.STEM);
    Stem top = StemFinder.findRootStem( GrouperSession.start(this.subjA) ).addChildStem("child", "child"); 
    assertTrue( top.hasStem(this.subjA) );
  }
  /**
   * @since   @HEAD@
   */
  public void test_grant_cannotGrantCreateWithoutStem() 
    throws  InsufficientPrivilegeException,
            SchemaException,
            SessionException
  {
    try {
      StemFinder.findRootStem( GrouperSession.start(this.subjA) ).grantPriv(this.subjB, NamingPrivilege.CREATE);
      fail("failed to throw expected GrantPrivilegeException");
    }
    catch (GrantPrivilegeException eExpected) {
      assertTrue(true);
    }
  }
  /**
   * @since   @HEAD@
   */
  public void test_grant_cannotGrantStemWithoutStem() 
    throws  InsufficientPrivilegeException,
            SchemaException,
            SessionException
  {
    try {
      StemFinder.findRootStem( GrouperSession.start(this.subjA) ).grantPriv(this.subjB, NamingPrivilege.STEM);
      fail("failed to throw expected GrantPrivilegeException");
    }
    catch (GrantPrivilegeException eExpected) {
      assertTrue(true);
    }
  }



  /** 
   * @since   @HEAD@
   */
  public void test_revokeAll_cannotRevokeAllCreateWithoutStem() 
    throws  InsufficientPrivilegeException,
            SchemaException,
            SessionException
  {
    try {
      StemFinder.findRootStem( GrouperSession.start(this.subjA) ).revokePriv(NamingPrivilege.CREATE);
      fail("failed to throw expected RevokePrivilegeException");
    } 
    catch (RevokePrivilegeException eExpected) {
      assertTrue(true);
    }
  }
  /**
   * @since   @HEAD@
   */
  public void test_revokeAll_cannotRevokeAllStemWithoutStem() 
    throws  InsufficientPrivilegeException,
            SchemaException,
            SessionException
  {
    try {
      StemFinder.findRootStem( GrouperSession.start(this.subjA) ).revokePriv(NamingPrivilege.STEM);
      fail("failed to throw expected RevokePrivilegeException");
    } 
    catch (RevokePrivilegeException eExpected) {
      assertTrue(true);
    }
  }



  /**
   * @since   @HEAD@
   */
  public void test_revoke_cannotRevokeCreateWithoutStem()
    throws  InsufficientPrivilegeException,
            SchemaException,
            SessionException
  {
    try {
      StemFinder.findRootStem( GrouperSession.start(this.subjA) ).revokePriv(this.subjB, NamingPrivilege.CREATE);
      fail("failed to throw expected RevokePrivilegeException");
    } 
    catch (RevokePrivilegeException eExpected) {
      assertTrue(true);
    }
  }
  /**
   * @since   @HEAD@
   */
  public void test_revoke_cannotRevokeStemWithoutStem()
    throws  InsufficientPrivilegeException,
            SchemaException,
            SessionException
  {
    try {
      StemFinder.findRootStem( GrouperSession.start(this.subjA) ).revokePriv(this.subjB, NamingPrivilege.STEM);
      fail("failed to throw expected RevokePrivilegeException");
    } 
    catch (RevokePrivilegeException eExpected) {
      assertTrue(true);
    }
  }

}

