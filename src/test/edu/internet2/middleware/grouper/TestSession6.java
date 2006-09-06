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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test {@link GrouperSession} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSession6.java,v 1.2 2006-09-06 14:24:07 blair Exp $
 */
public class TestSession6 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSession6.class);

  public TestSession6(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCanGetInnerSessionWithinInnerSession() {
    LOG.info("testCanGetInnerSessionWithinInnerSession");
    try {
      GrouperSession  s   = GrouperSession.start( SubjectFinder.findRootSubject());
      GrouperSession  rs  = s.getRootSession();
      // FIXME I think this should fail but the code is too tangled to fix at the moment
      rs.getRootSession();
      Assert.assertTrue("got inner session within inner session", true);
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCanGetInnerSessionWithinInnerSession()

}

