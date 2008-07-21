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

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSession5.java,v 1.7 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestSession5 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSession5.class);

  public TestSession5(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testNotEqual() {
    LOG.info("testNotEqual");
    try {
      GrouperSession  a = SessionHelper.getRootSession();
      GrouperSession  b = SessionHelper.getRootSession();
      Assert.assertFalse("a != b", a.equals(b));
      a.stop();
      b.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testNotEqual()

}

