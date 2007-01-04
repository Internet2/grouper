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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGAttr6.java,v 1.3 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestGAttr6 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestGAttr6.class);

  public TestGAttr6(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailSetAttributeBlankAttribute() {
    LOG.info("testFailSetAttributeBlankAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");

      try {
        gA.setAttribute("", "foo");
        T.fail("set blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not set blank attribute");
      }

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailSetAttributeBlankAttribute()

} // public class TestGAttr6

