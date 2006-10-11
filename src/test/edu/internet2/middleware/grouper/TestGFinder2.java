/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * @version $Id: TestGFinder2.java,v 1.1 2006-10-11 18:16:09 blair Exp $
 * @since   1.1.0
 */
public class TestGFinder2 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestGFinder2.class);

  public TestGFinder2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToFindGroupByAttributeNullSession() {
    LOG.info("testFailToFindGroupByAttributeNullSession");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GroupFinder.findByAttribute(null, "description", "i2:a:a");
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eIA) {
      assertTrue(true);
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testFailToFindGroupByAttributeNullSession()

} // public class TestGFinder2

