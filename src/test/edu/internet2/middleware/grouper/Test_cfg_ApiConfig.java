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
import  edu.internet2.middleware.grouper.cfg.Configuration;
import  edu.internet2.middleware.grouper.cfg.ApiConfig;

/**
 * Test {@link ApiConfig}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_cfg_ApiConfig.java,v 1.2 2007-08-27 15:46:24 blair Exp $
 * @since   @HEAD@
 */
public class Test_cfg_ApiConfig extends GrouperTest {


  Configuration cfg;
  String        prop_invalid  = "this.property.should.not.exist";
  String        prop_valid    = "dao.factory";



  public void setUp() {
    super.setUp();
    this.cfg = new ApiConfig();
  }
  public void tearDown() {
    super.tearDown();
  }



  public void test_constant_ACCESS_PRIVILEGE_INTERFACE() {
    assertEquals( "privileges.access.interface", ApiConfig.ACCESS_PRIVILEGE_INTERFACE );
  }

  public void test_constant_NAMING_PRIVILEGE_INTERFACE() {
    assertEquals( "privileges.naming.interface", ApiConfig.NAMING_PRIVILEGE_INTERFACE );
  }


  public void test_getProperty_nullProperty() {
    try {
      this.cfg.getProperty(null);
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getProperty_nonExistentProperty() {
    assertNull( this.cfg.getProperty(this.prop_invalid) );
  }

  public void test_getProperty_validProperty() {
    assertNotNull( this.cfg.getProperty(this.prop_valid) );
  }


  public void test_setProperty_nullProperty() {
    try {
      this.cfg.setProperty(null, null);
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_setProperty_setNull() {
    try {
      this.cfg.setProperty(this.prop_valid, null);
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_setProperty_settingPropertyReturnsSetValue() {
    try {
      String val = "new value";
      assertFalse( val.equals( this.cfg.getProperty(this.prop_valid) ) );
      assertEquals( val, this.cfg.setProperty(this.prop_valid, val) );
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

}

