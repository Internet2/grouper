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
import  edu.internet2.middleware.grouper.AccessAdapter;
import  edu.internet2.middleware.grouper.cfg.ApiConfig;
import  edu.internet2.middleware.grouper.privs.AccessWrapper;
import  edu.internet2.middleware.grouper.internal.util.Realize;
import  edu.internet2.middleware.subject.Subject;


/**
 * Test {@link AccessWrapper}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_privs_AccessWrapper.java,v 1.1 2007-08-24 14:18:16 blair Exp $
 * @since   @HEAD@
 */
public class Test_privs_AccessWrapper extends GrouperTest {


  private GrouperSession s;



  public void setUp() {
    super.setUp();
    try {
      this.s = GrouperSession.start( SubjectFinder.findAllSubject() );
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
  public void test_constructor_nullSession() {
    try {
      new AccessWrapper(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  /**
   * @since   @HEAD@
   */
  public void test_constructor_nullAdapter() {
    try {
      new AccessWrapper(this.s, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

}

