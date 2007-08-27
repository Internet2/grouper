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
import  edu.internet2.middleware.grouper.internal.util.Realize;


/**
 * Test {@link GrouperDAOFactory}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_GrouperDAOFactory.java,v 1.3 2007-08-27 15:53:53 blair Exp $
 * @since   1.2.1
 */
public class Test_api_GrouperDAOFactory extends GrouperTest {


  private String            invalid = "this class does not exist";
  private String            prop    = "dao.factory";


  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }


  public void test_getFactory_nullConfig() {
    try {
      GrouperDAOFactory.getFactory(null);
      fail("did not throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getFactory_getInvalidFactory() {
    ApiConfig cfg = new ApiConfig();
    cfg.setProperty(prop, invalid);
    try {
      GrouperDAOFactory.getFactory(cfg);
      fail("did not throw GrouperRuntimeException");
    }
    catch (GrouperRuntimeException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getFactory_defaultNotNull() {
    assertNotNull( GrouperDAOFactory.getFactory() );  
  } 

  public void test_getFactory_defaultInstanceOfRightClass() {
    assertTrue( "returned factory is instanceof GrouperDAOFactory", GrouperDAOFactory.getFactory() instanceof GrouperDAOFactory );
  } 

  public void test_getFactory_returnDefaultFactoryWhenNotConfigured() {
    ApiConfig cfg = new ApiConfig();
    cfg.setProperty( prop, GrouperConfig.EMPTY_STRING );
    assertEquals(
      "returns default factory when not configured", 
      Realize.instantiate( GrouperConfig.DEFAULT_DAO_FACTORY ).getClass(),
      GrouperDAOFactory.getFactory(cfg).getClass()
    );
  }

  public void test_getFactory_factoryIsReused() {
    assertEquals(
      "instantiated dao factory is reused", GrouperDAOFactory.getFactory(), GrouperDAOFactory.getFactory()
    );
  }

} 

