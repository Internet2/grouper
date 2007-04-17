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
import  edu.internet2.middleware.grouper.internal.cache.SimpleWheelPrivilegeCache;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSimpleWPCache3.java,v 1.7 2007-04-17 17:13:27 blair Exp $
 * @since   1.2.0
 */
public class TestSimpleWPCache3 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestSimpleWPCache3.class);

  public TestSimpleWPCache3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetMaxWheelAge() {
    LOG.info("testGetMaxWheelAge");
    try {
      // No value configured
      assertEquals( 
        "not configured",   
        GrouperConfig.EMPTY_STRING, 
        GrouperConfig.getProperty(GrouperConfig.MAX_WHEEL_AGE) 
      );
      assertEquals( 
        "not configured so using default",
        SimpleWheelPrivilegeCache.DEFAULT_MAX_AGE, 
        SimpleWheelPrivilegeCache.internal_getMaxWheelAge()
      );

      // Improper value configured
      String val = "not a long value";
      GrouperConfig.internal_setProperty(GrouperConfig.MAX_WHEEL_AGE, val);
      assertEquals( 
        "inappropriately configured", val, GrouperConfig.getProperty(GrouperConfig.MAX_WHEEL_AGE) 
      );
      assertEquals( 
        "inappropriately configured",
        SimpleWheelPrivilegeCache.DEFAULT_MAX_AGE, 
        SimpleWheelPrivilegeCache.internal_getMaxWheelAge()
      );

      // Custom value
      val = "5555";
      GrouperConfig.internal_setProperty(GrouperConfig.MAX_WHEEL_AGE, val);
      assertEquals( 
        "configured", val, GrouperConfig.getProperty(GrouperConfig.MAX_WHEEL_AGE) 
      );
      assertEquals( 
        "using custom value", Long.parseLong(val), SimpleWheelPrivilegeCache.internal_getMaxWheelAge()
      );

      // Reset
      GrouperConfig.internal_setProperty(
        GrouperConfig.MAX_WHEEL_AGE, new String(GrouperConfig.MAX_WHEEL_AGE)
      );
      assertEquals( 
        "reset to default value", 
        SimpleWheelPrivilegeCache.DEFAULT_MAX_AGE, 
        SimpleWheelPrivilegeCache.internal_getMaxWheelAge()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testGetMaxWheelAge()

} // public class TestSimpleWPCache3

