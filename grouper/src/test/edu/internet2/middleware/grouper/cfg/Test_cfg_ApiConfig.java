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

package edu.internet2.middleware.grouper.cfg;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;

/**
 * Test {@link ApiConfig}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_cfg_ApiConfig.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_cfg_ApiConfig extends GrouperTest {


  /**
   * 
   */
  public Test_cfg_ApiConfig() {
    super();
    
  }
  /**
   * @param name
   */
  public Test_cfg_ApiConfig(String name) {
    super(name);
    
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_cfg_ApiConfig("test_getProperty_nullProperty"));
  }
  
  String        prop_invalid  = "this.property.should.not.exist";
  String        prop_valid    = "dao.factory";



  public void setUp() {
    super.setUp();
  }
  public void tearDown() {
    super.tearDown();
  }




  public void test_getProperty_nonExistentProperty() {
    assertNull( GrouperConfig.retrieveConfig().propertyValueString(this.prop_invalid) );
  }

  public void test_getProperty_validProperty() {
    assertNotNull( GrouperConfig.retrieveConfig().propertyValueString(this.prop_valid) );
  }


  public void test_setProperty_settingPropertyReturnsSetValue() {
    try {
      String val = "new value";
      assertFalse( val.equals( GrouperConfig.retrieveConfig().propertyValueString(this.prop_valid) ) );
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(this.prop_valid, val);
      assertEquals( val,  GrouperConfig.retrieveConfig().propertyValueString(this.prop_valid, val) );
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(this.prop_valid);
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

}

