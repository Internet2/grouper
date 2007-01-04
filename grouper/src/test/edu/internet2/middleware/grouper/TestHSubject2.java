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

/**
 * @author  blair christensen.
 * @version $Id: TestHSubject2.java,v 1.2 2007-01-04 17:17:46 blair Exp $
 * @since   1.1.0
 */
public class TestHSubject2 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestHSubject2.class);

  public TestHSubject2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToAddAlreadyExistingSubject() {
    LOG.info("testFailToAddAlreadyExistingSubject");
    try {
      HibernateSubject.add(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        "subj id", "subj type", "subj name"
      );
      // Now add it again
      try {
        HibernateSubject.add(
          GrouperSession.start( SubjectFinder.findRootSubject() ),
          "subj id", "subj type", "subj name"
        );
        fail("added already existing HibernateSubject");
      }
      catch (GrouperException eG) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      e(e);
    }
  } // public void testFailToAddAlreadyExistingSubject()

} // public class TestHSubject2

