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
 * @version $Id: Test_UnresolvedBugs.java,v 1.5 2007-04-17 18:45:13 blair Exp $
 * @since   1.2.0
 */
public class Test_UnresolvedBugs extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  // commented out to silence warnings in eclipse:
  // private static final String KLASS = Test_UnresolvedBugs.class.getName();
  private static final Log    LOG   = LogFactory.getLog(Test_UnresolvedBugs.class);

  /*
   * Use "TestLog.resolved(KLASS, msg)" and "TestLog.unresolved(KLASS, msg)"
   */

  // TESTS //  

  public void testUnresolvedBug0() {
    try {
      LOG.info("testUnresolvedBug0");
      assertTrue("no known unresolved bugs", true);
    }
    catch (Exception e) {
      e.printStackTrace();
      unexpectedException(e);
    }
  } // public void testUnresolvedBug0()

} // public class Test_UnresolvedBugs extends GrouperTest

