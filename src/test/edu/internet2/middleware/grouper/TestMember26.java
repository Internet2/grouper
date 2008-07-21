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
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import  edu.internet2.middleware.subject.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestMember26.java,v 1.6 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestMember26 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestMember26.class);

  public TestMember26(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testSetRootSubjectId() {
    LOG.info("testSetRootSubjectId");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findRootSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId on GrouperSystem");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetRootSubjectId()

} // public class TestMember26

