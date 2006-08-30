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
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSession4.java,v 1.4 2006-08-30 18:35:38 blair Exp $
 */
public class TestSession4 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSession4.class);


  // Private Class Variables
  private Source sa;

  public TestSession4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
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
      catch (NullPointerException eNP) {
        Assert.assertTrue("OK: failed to get member", true);
      }
      try {
        s.getSubject();
        Assert.fail("FAIL: got subject");
      }
      catch (RuntimeException eR) {
        Assert.assertTrue("OK: failed to get subject", true);
      }
      Assert.assertNull("null session id", s.getSessionId());
      Assert.assertNull("null start time", s.getStartTime());
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testUseStoppedSession()

}

