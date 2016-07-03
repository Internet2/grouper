/**
 * Copyright 2014 Internet2
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
 */
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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.subject.Subject;

/**
 * Test naming privilege use cases.
 * 
 * @author  blair christensen.
 * @version $Id: Test_uc_NamingPrivs.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_uc_NamingPrivs extends GrouperTest {


  private R       r;
  private Subject subjA, subjB;


  public void setUp() {
    super.setUp();
    try {
      r     = R.getContext("grouper");
      GrouperSession s = GrouperSession.startRootSession();
      try {
        StemFinder.findByName(s, "etc", true);
      } catch (StemNotFoundException snfe) {
        r.root.addChildStem("etc", "etc");
      }
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
    }
    catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  /**
   * @since   1.2.1
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
    assertTrue( top.hasStemAdmin(this.subjA) );
  }
  /**
   * @since   1.2.1
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
   * @since   1.2.1
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
   * @since   1.2.1
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
   * @since   1.2.1
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
   * @since   1.2.1
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
   * @since   1.2.1
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

