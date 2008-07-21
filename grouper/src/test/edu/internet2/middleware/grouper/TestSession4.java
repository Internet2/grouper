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
import junit.textui.TestRunner;

import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSession4.java,v 1.10 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestSession4 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSession4.class);

  public TestSession4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public static void main(String[] args) {
    TestRunner.run(new TestSession4("testUseStoppedSession"));
  }

  public void testUseStoppedSession() {
    LOG.info("testUseStoppedSession");
    GrouperSession s = SessionHelper.getRootSession();
    try { 
      s.stop();
      Assert.assertTrue("stopped session", true);
      try {
        s.getMember();
        Assert.fail("FAIL: got member");
      }
      catch (IllegalStateException eNullMember) {
        Assert.assertTrue("OK: failed to get member", true);
      }
      try {
        s.getSubject();
        Assert.fail("FAIL: got subject");
      }
      catch (IllegalStateException eNullSubject) {
        Assert.assertTrue("OK: failed to get subject", true);
      }
      try {
        Assert.assertNull("null session id", s.getSessionId());
      }
      catch (IllegalStateException eNullSessionId) {
        Assert.assertTrue("OK: failed to get session id", true);
      }
      try {
        Assert.assertNull("null start time", s.getStartTime());
      }
      catch (IllegalStateException eNullStartTime) {
        Assert.assertTrue("OK: failed to get start time", true);
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testUseStoppedSession()

}

