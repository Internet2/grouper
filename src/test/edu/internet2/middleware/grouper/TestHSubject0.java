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
import junit.textui.TestRunner;

import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestHSubject0.java,v 1.7 2008-07-21 04:43:57 mchyzer Exp $
 * @since   1.1.0
 */
public class TestHSubject0 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestHSubject0.class);

  public TestHSubject0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public static void main(String[] args) {
    TestRunner.run(TestHSubject0.class);
  }
  public void testAddRegistrySubjectAsRoot() {
    LOG.info("testAddRegistrySubjectAsRoot");
    try {
      RegistrySubject.add(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        "subj id", "subj type", "subj name"
      );
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testAddRegistrySubjectAsRoot()

} // public class TestHSubject0

