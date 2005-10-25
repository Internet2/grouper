/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperSession.java,v 1.1.2.1 2005-10-25 20:10:14 blair Exp $
 */
public class TestGrouperSession extends TestCase {

  private Source sa;

  public TestGrouperSession(String name) {
    super(name);
  }

  protected void setUp () {
    // Nothing
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testStartSessionBadSubject() {
    try {
      GrouperSession s = GrouperSession.startSession(
        SubjectFinder.findById("bad subject")
      );
      Assert.fail("started session with bad subject: " + s);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("could not start session with bad subject", true);
    }
  } // public void testStartSessionBadSubject()

  public void testStartSessionGoodSubject() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try {
      GrouperSession s = GrouperSession.startSession(
        SubjectFinder.findById(id)
      );
      Assert.assertTrue("started session: " + s, true);
      Assert.assertNotNull("session id !null", s.getSessionId());
      Member  m     = s.getMember();
      Assert.assertNotNull("m !null", m);
      Subject subj  = s.getSubject();
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue("subj type", subj.getType().getName().equals(type));
    }
    catch (SubjectNotFoundException e) {
      Assert.fail(
        "failed to start session with good subject: " + e.getMessage()
      );
    }
  } // public void testStartSessionGoodSubject()

}

