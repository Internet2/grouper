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
import  edu.internet2.middleware.grouper.cfg.ApiConfig;


/**
 * Test {@link GrouperSession}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_GrouperSession.java,v 1.1 2007-08-02 16:46:51 blair Exp $
 * @since   @HEAD@
 */
public class Test_api_GrouperSession extends GrouperTest {


  private ApiConfig       cfg;
  private GrouperSession  s;
  private String          prop_valid    = "privileges.access.interface";
  private String          prop_invalid  = "invalid property";


  public void setUp() {
    super.setUp();
    try {
      this.cfg  = new ApiConfig();
      this.s    = GrouperSession.start( SubjectFinder.findAllSubject() );
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }


  public void test_getAccessClass_notNull() {
    assertNotNull( this.s.getAccessClass() );
  }

  public void test_getAccessClass_defaultAccessAdapter() {
    assertEquals( this.cfg.getProperty(ApiConfig.ACCESS_PRIVILEGE_INTERFACE), this.s.getAccessClass() );
  }


  public void test_getConfig_null() {
    try {
      this.s.getConfig(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getConfig_invalidProperty() {
    assertNull( this.s.getConfig(prop_invalid) );
  }

  public void test_getConfig_validProperty() {
    assertEquals( this.cfg.getProperty(ApiConfig.ACCESS_PRIVILEGE_INTERFACE), this.s.getConfig(prop_valid) );
  }


  public void test_getNamingClass_notNull() {
    assertNotNull( this.s.getNamingClass() );
  }

  public void test_getNamingClass_defaultAccessAdapter() {
    assertEquals( this.cfg.getProperty(ApiConfig.NAMING_PRIVILEGE_INTERFACE), this.s.getNamingClass() );
  }

}

