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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 * Test {@link GrouperConfig}.
 * 
 * @author  blair christensen.
 * @version $Id: Test_api_GrouperConfig.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_api_GrouperConfig extends GrouperTest {


  String        prop_invalid      = "this.property.should.not.exist";
  String        prop_valid_api    = "dao.factory";
  String        prop_valid_build  = "build.dir";
  String        prop_valid_hib    = "hibernate.dialect";


  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_getHibernateProperty_nonExistentProperty() {
    assertEquals( GrouperConfig.EMPTY_STRING, GrouperConfig.getHibernateProperty(this.prop_invalid) );
  }

  public void test_getHibernateProperty_validProperty() {
    assertNotNull( GrouperConfig.getHibernateProperty(this.prop_valid_hib) );
  }


  public void test_getProperty_nonExistentProperty() {
    assertEquals( GrouperConfig.EMPTY_STRING, GrouperConfig.getProperty(this.prop_invalid) );
  }

  public void test_getProperty_validProperty() {
    assertNotNull( GrouperConfig.getProperty(this.prop_valid_api) );
  }

}

