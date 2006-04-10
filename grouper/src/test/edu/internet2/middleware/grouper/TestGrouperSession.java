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


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperSession.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
 */
public class TestGrouperSession extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGrouperSession.class);


  // Private Class Variables
  private Source sa;

  public TestGrouperSession(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testStartSessionBadSubject() {
    LOG.info("testStartSessionBadSubject");
    Helper.getBadSession("bad subject");
  } // public void testStartSessionBadSubject()

  public void testStartSessionGoodSubject() {
    LOG.info("testStartSessionGoodSubject");
    GrouperSession s = SessionHelper.getSession("GrouperSystem", "application");
  } // public void testStartSessionGoodSubject()

  public void testGetStartTime() {
    LOG.info("testGetStartTime");
    GrouperSession s = SessionHelper.getRootSession();
    Date d = s.getStartTime();
    Assert.assertNotNull("start time !null", d);
    Assert.assertTrue("start time != epoch (" + d + ")", !d.equals(new Date()));
  } // public void testGetStartTime()

  public void testStopSession() {
    LOG.info("testStopSession");
    GrouperSession s = SessionHelper.getRootSession();
    try { 
      s.stop();
      Assert.assertTrue("stopped session", true);
    }
    catch (Exception e) {
      Assert.fail("failed to stop session: " + e.getMessage());
    }
  } // public void testStopSession()

  public void testUseStoppedSession() {
    LOG.info("testUseStoppedSession");
    GrouperSession s = SessionHelper.getRootSession();
    try { 
      s.stop();
      Assert.assertTrue("stopped session", true);
      Assert.assertNull("null member", s.getMember());
      try {
        Subject subj = s.getSubject();
        Assert.fail("got subject");
      }
      catch (RuntimeException eR) {
        Assert.assertTrue("failed to get subject", true);
      }
      Assert.assertNull("null session id", s.getSessionId());
      Assert.assertNull("null start time", s.getStartTime());
    }
    catch (Exception e) {
      Assert.fail("failed to stop session: " + e.getMessage());
    }
  } // public void testUseStoppedSession()

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

