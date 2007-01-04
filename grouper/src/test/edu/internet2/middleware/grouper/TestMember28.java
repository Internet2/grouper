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
import  org.apache.commons.logging.*;
import  edu.internet2.middleware.subject.*;

/**
 * @author  blair christensen.
 * @version $Id: TestMember28.java,v 1.2 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestMember28 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestMember28.class);

  public TestMember28(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testSetSubjectSourceIdFailNullValue() {
    LOG.info("testSetSubjectSourceIdFailNullValue");
    try {
      R       r     = R.populateRegistry(0, 0, 1);
      Subject subjA = r.getSubject("a");
      Member  m     = MemberFinder.findBySubject(r.rs, subjA);
      try {
        m.setSubjectSourceId(null);
        fail("unexpectedly changed subject source id when value null");
      }
      catch (IllegalArgumentException eIA) {
        assertTrue(true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testSetSubjectSourceIdFailNullValue

} // public class TestMember28

