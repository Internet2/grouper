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
import  edu.internet2.middleware.grouper.internal.util.Realize;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Unit_API_GrouperDAOFactory_getFactory.java,v 1.2 2007-05-14 16:12:56 blair Exp $
 * @since   1.2.0
 */
public class Test_Unit_API_GrouperDAOFactory_getFactory extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Unit_API_GrouperDAOFactory_getFactory.class);


  // TESTS //  

  public void test_GetDefaultFactory_InvalidFactory() {
    String orig = GrouperConfig.getProperty( GrouperConfig.PROP_DAO_FACTORY );
    GrouperDAOFactory.internal_resetFactory(); 
    GrouperConfig.internal_setProperty( GrouperConfig.PROP_DAO_FACTORY, "this class does not exist" );
    try {
      GrouperDAOFactory gdf = GrouperDAOFactory.getFactory();
      fail("unexpectedly returned gdf: (" + gdf + ")");
    }
    catch (GrouperRuntimeException eExpected) {
      // expected
    }
    finally {
      // TODO 20070427 this is *far* too fragile
      GrouperConfig.internal_setProperty( GrouperConfig.PROP_DAO_FACTORY, orig );
    }
  }

  public void test_GetDefaultFactory_NotNull() {
    assertNotNull( GrouperDAOFactory.getFactory() );  
  } 

  public void test_GetDefaultFactory_InstanceOf() {
    assertTrue( "returned factory is instanceof GrouperDAOFactory", GrouperDAOFactory.getFactory() instanceof GrouperDAOFactory );
  } 

  public void test_GetDefaultFactory_WhenNotConfigured() {
    // TODO 20070514 this is extremely fragile
    String orig = GrouperConfig.getProperty( GrouperConfig.PROP_DAO_FACTORY );
    GrouperDAOFactory.internal_resetFactory();
    try {
      GrouperConfig.internal_setProperty( GrouperConfig.PROP_DAO_FACTORY, GrouperConfig.EMPTY_STRING );
      assertEquals(
        "default dao when not configured",
        Realize.instantiate( GrouperConfig.DEFAULT_DAO_FACTORY ).getClass(),
        GrouperDAOFactory.getFactory().getClass()
      );
    }
    finally {
      GrouperConfig.internal_setProperty( GrouperConfig.PROP_DAO_FACTORY, orig );
    }
  }

  /* TODO 20070427 what am i even trying to test here?
  public void test_GetDefaultFactory_ReturnedMatchesConfiguration() {
    GrouperConfig.internal_setProperty( GrouperConfig.PROP_DAO_FACTORY, GrouperConfig.DEFAULT_DAO_FACTORY );
    assertEquals(
      "returned dao matches configured dao",
      GrouperConfig.DEFAULT_DAO_FACTORY,
      GrouperDAOFactory.getFactory().getClass().getName()
    );
  }
  */

  public void test_GetDefaultFactory_FactoryIsReused() {
    assertEquals(
      "instantiated dao factory is reused",
      GrouperDAOFactory.getFactory(),
      GrouperDAOFactory.getFactory()
    );
  }

} 

