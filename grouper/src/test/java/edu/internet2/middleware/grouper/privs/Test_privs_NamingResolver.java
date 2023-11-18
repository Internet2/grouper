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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * Test {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_privs_NamingResolver.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_privs_NamingResolver extends GrouperTest {


  private NamingResolver  resolver;
  private GrouperSession  s;



  public void setUp() {
    super.setUp();
    try {
      this.s        = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.resolver = NamingResolverFactory.getInstance(this.s);
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
  public void test_getStemsWhereSubjectHasPrivilege_nullSubject() {
    try {
      this.resolver.getStemsWhereSubjectHasPrivilege(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_getStemsWhereSubjectHasPrivilege_nullPrivilege() {
    try {
      this.resolver.getStemsWhereSubjectHasPrivilege( this.s.getSubject(), null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_getPrivileges_nullStem() {
    try {
      this.resolver.getPrivileges(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_getPrivileges_nullSubject() {
    try {
      this.resolver.getPrivileges( new Stem(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_getSubjectsWithPrivilege_nullStem() {
    try {
      this.resolver.getSubjectsWithPrivilege(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_getSubjectsWithPrivilege_nullPrivilege() {
    try {
      this.resolver.getSubjectsWithPrivilege( new Stem(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  /** 
   * @since   1.2.1
   */
  public void test_grantPrivilege_nullStem()
    throws  UnableToPerformException
  {
    try {
      this.resolver.grantPrivilege(null, null, null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_grantPrivilege_nullSubject()
    throws  UnableToPerformException
  {
    try {
      this.resolver.grantPrivilege( new Stem(), null, null, null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_grantPrivilege_nullPrivilege() 
    throws  UnableToPerformException
  {
    try {
      this.resolver.grantPrivilege( new Stem(), this.s.getSubject(), null, null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_nullStem() {
    try {
      this.resolver.hasPrivilege(null, null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_nullSubject() {
    try {
      this.resolver.hasPrivilege( new Stem(), null, null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /** 
   * @since   1.2.1
   */
  public void test_hasPrivilege_nullPrivilege() {
    try {
      this.resolver.hasPrivilege( new Stem(), this.s.getSubject(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePrivilege_groupAndPrivilege_nullStem() 
    throws  UnableToPerformException
  {
    try {
      this.resolver.revokePrivilege(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_revokePrivilege_groupAndPrivilege_nullPrivilege() 
    throws  UnableToPerformException
  {
    try {
      this.resolver.revokePrivilege( new Stem(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_revokePrivilege_groupAndSubjectAndPrivilege_nullStem() 
    throws  UnableToPerformException
  {
    try {
      this.resolver.revokePrivilege(null, null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_revokePrivilege_groupAndSubjectAndPrivilege_nullSubject() 
    throws  UnableToPerformException
  {
    try {
      this.resolver.revokePrivilege( new Stem(), null, null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   1.2.1
   */
  public void test_revokePrivilege_groupAndSubjectAndPrivilege_nullPrivilege() 
    throws  UnableToPerformException
  {
    try {
      this.resolver.revokePrivilege( new Stem(), SubjectFinder.findAllSubject(), null );
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

}

