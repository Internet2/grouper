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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_GrouperSourceAdapter_search.java,v 1.2 2008-07-21 04:43:57 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_Integration_GrouperSourceAdapter_search extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Integration_GrouperSourceAdapter_search.class);


  // TESTS //  

  public void testSearch_NullParameter() {
    try {
      LOG.info("testSearch_NullParameter");
      GrouperSourceAdapter gsa = new GrouperSourceAdapter();
      try {
        gsa.search(null);
        fail("failed to throw IllegalArgumentException on null parameter");
      }
      catch (IllegalArgumentException eExpected) {
        assertTrue("threw expected IllegalArgumentException on null parameter", true); 
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSearch_NullParameter()

  public void testSearch_BlankParameter() {
    try {
      LOG.info("testSearch_BlankParameter");
      GrouperSourceAdapter gsa = new GrouperSourceAdapter();
      gsa.search(GrouperConfig.EMPTY_STRING);
      assertTrue("blank parameter is not an error", true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSearch_BlankParameter()

  public void testSearch_PercentageParameter() {
    try {
      LOG.info("testSearch_PercentageParameter");
      GrouperSourceAdapter gsa = new GrouperSourceAdapter();
      gsa.search("%"); // this is competely undocumented behavior
      assertTrue("% parameter is not an error", true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSearch_PercentageParameter()

} // public class Test_Integration_GrouperSourceAdapter_search extends GrouperTest

