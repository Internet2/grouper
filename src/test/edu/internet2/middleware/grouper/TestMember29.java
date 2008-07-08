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
 * @version $Id: TestMember29.java,v 1.5 2008-07-08 14:50:15 mchyzer Exp $
 * @since   1.1.0
 */
public class TestMember29 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestMember29.class);

  public TestMember29(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testSetSubjectSourceIdFailAsNonRoot() {
    LOG.info("testSetSubjectSourceIdFailAsNonRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 2);
      Subject   subjA = r.getSubject("a");
      Subject   subjB = r.getSubject("b");
      r.rs.stop();

      GrouperSession  nrs   = GrouperSession.start(subjA);
      Member          m     = MemberFinder.findBySubject(nrs, subjB);
      String          orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id when not root-like");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        nrs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailAsNonRoot

} // public class TestMember29

