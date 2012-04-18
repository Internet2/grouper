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

package edu.internet2.middleware.grouper.cfg;
import edu.internet2.middleware.grouper.cfg.ConfigurationHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;

/**
 * Test {@link ConfigurationHelper}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_cfg_ConfigurationHelper.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_cfg_ConfigurationHelper extends GrouperTest {

  ConfigurationHelper helper;


  public void setUp() {
    super.setUp();
    this.helper = new ConfigurationHelper();
  }

  public void tearDown() {
    super.tearDown();
  }


  public void test_validateParamsNotNull_nullProperty() {
    try {
      this.helper.validateParamsNotNull(null);
    }
    catch (IllegalArgumentException eExpected) {
      assertEquals( "null property", eExpected.getMessage() );
    }
  }
  public void test_validateParamsNotNull_nullValue() {
    try {
      this.helper.validateParamsNotNull("dao.factory", null);
    }
    catch (IllegalArgumentException eExpected) {
      assertEquals( "null value", eExpected.getMessage() );
    }
  }
}

