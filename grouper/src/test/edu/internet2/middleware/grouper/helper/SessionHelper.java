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

package edu.internet2.middleware.grouper.helper;
import org.apache.commons.lang.StringUtils;

import junit.framework.Assert;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * {@link GrouperSession} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: SessionHelper.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class SessionHelper {

  // public CLASS METHODS //

  // @return  A root {@link GrouperSession}
  public static GrouperSession getRootSession() {
    return SessionHelper.getSession(GrouperConfig.ROOT);
  } // public static GrouperSession getRootSession()

  // Get a session by id
  // @return  A {@link GrouperSession}
  public static GrouperSession getSession(String id) {
    try {
      
      GrouperSession s = StringUtils.equals(id, GrouperConfig.ROOT) ?
          GrouperSession.startRootSession() : GrouperSession.start(
        SubjectFinder.findById(id, true)
      );
      Assert.assertNotNull("s !null", s);
      Assert.assertNotNull("session_id !null", s.getSessionId());
      Assert.assertTrue("has sessionID", !s.getSessionId().equals(""));
      Assert.assertNotNull("subject !null", s.getSubject());
      Assert.assertTrue(
        "subject instanceof Subject", s.getSubject() instanceof Subject
      );
      Assert.assertNotNull("member !null", s.getMember());
      Assert.assertTrue(
        "member instanceof Member", s.getMember() instanceof Member
      );
      Assert.assertTrue(
        "subject id", s.getMember().getSubjectId().equals(id)
      );
      return s;
    }
    catch (Exception e) {
      T.e(e);
    }
    throw new GrouperException();
  } // public static GrouperSession getSession(id)

  // Get a session by id and type
  // @return  A {@link GrouperSession}
  public static GrouperSession getSession(String id, String type) {
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(id, type, true)
      );
      Assert.assertNotNull("s !null", s);
      Assert.assertNotNull("session_id !null", s.getSessionId());
      Assert.assertTrue("has sessionID", !s.getSessionId().equals(""));
      Assert.assertNotNull("subject !null", s.getSubject());
      Assert.assertTrue(
        "subject instanceof Subject", s.getSubject() instanceof Subject
      );
      Assert.assertNotNull("member !null", s.getMember());
      Assert.assertTrue(
        "member instanceof Member", s.getMember() instanceof Member
      );
      Assert.assertTrue(
        "subject id", s.getMember().getSubjectId().equals(id)
      );
      Assert.assertTrue(
        "subject type", s.getMember().getSubjectTypeId().equals(type)
      );
      Assert.assertTrue(
        "access priv klass",
        s.getAccessClass().equals("edu.internet2.middleware.grouper.GrouperAccessAdapter")
      );
      Assert.assertTrue(
        "naming priv klass",
        s.getNamingClass().equals("edu.internet2.middleware.grouper.GrouperNamingAdapter")
      );
      return s;
    }
    catch (Exception e) {
      T.e(e);
    }
    throw new GrouperException();
  } // public static GrouperSession getSession(id, type)

  public static void stop(GrouperSession s) {
    try {
      s.stop();
    }
    catch (SessionException eS) {
      Assert.fail(eS.getMessage());
    }
  } // public static void stop(s)

  // Fail to get a session by id
  public static void getBadSession(String id) {
    try {
      GrouperSession s = GrouperSession.start(
        SubjectFinder.findById(id, true)
      );
      Assert.fail("started session with bad subject: " + s);
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.assertTrue("OK: did not start session with bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public static void getBadSession(id)

} // class SessionHelper

