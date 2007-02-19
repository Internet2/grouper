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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSimpleWPCache0.java,v 1.5 2007-02-19 20:43:29 blair Exp $
 * @since   1.1.0
 */
public class TestSimpleWPCache0 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestSimpleWPCache0.class);

  public TestSimpleWPCache0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testInstantiateSimpleWheelPrivilegeCache() {
    LOG.info("testInstantiateSimpleWheelPrivilegeCache");
    try {
      Class           klass = SimpleWheelPrivilegeCache.class;
      PrivilegeCache  pc    = BasePrivilegeCache.getCache( klass.getName() );
      assertNotNull(pc);
      assertTrue( klass.isInstance(pc) );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testInstantiateSimpleWheelPrivilegeCache()

} // public class TestSimpleWPCache0

