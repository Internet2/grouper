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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Member}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMember.java,v 1.2 2005-12-06 20:56:07 blair Exp $
 */
public class TestMember extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestMember.class);


  public TestMember(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    Db.refreshDb();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testGetSource() {
    LOG.info("testGetSource");
    GrouperSession s = SessionHelper.getRootSession();
    try {
      // I'm not sure what to test on source retrieval
      Member  m   = s.getMember();
      Source  src = m.getSubjectSource();
      Assert.assertNotNull("src !null", src);
      Assert.assertTrue("src instanceof Source", src instanceof Source);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetSource()

}

