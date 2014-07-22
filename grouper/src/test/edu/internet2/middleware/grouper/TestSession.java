/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSession.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestSession extends GrouperTest {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestSession.class);

  public TestSession(String name) {
    super(name);
  }

  public void testStartSessionBadSubject() {
    LOG.info("testStartSessionBadSubject");
    SessionHelper.getBadSession("bad subject");
  } // public void testStartSessionBadSubject()

  public void testCanGetInnerSessionWithinInnerSession() {
    LOG.info("testCanGetInnerSessionWithinInnerSession");
    try {
      GrouperSession  s   = GrouperSession.start( SubjectFinder.findRootSubject());
      // I don't think I should allow this but the code is too tangled to fix at the moment.
      // And isn't the above just a damning statement.
      s.internal_getRootSession();
      Assert.assertTrue("got inner session within inner session", true);
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanGetInnerSessionWithinInnerSession()

  public void testGetStartTime() {
    LOG.info("testGetStartTime");
    GrouperSession s = SessionHelper.getRootSession();
    Assert.assertNotNull("start time !null", s.getStartTime());
    long  start = s.getStartTime().getTime();
    long  epoch = new Date(0).getTime();
    Assert.assertFalse(
      "start[" + start + "] != epoch[" + epoch + "]",
      start == epoch
    );
  } // public void testGetStartTime()

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

  public void testStartSessionGoodSubject() {
    LOG.info("testStartSessionGoodSubject");
    SessionHelper.getSession("GrouperSystem", "application");
  } // public void testStartSessionGoodSubject()

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

