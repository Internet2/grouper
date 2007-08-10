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
import  edu.internet2.middleware.grouper.subj.SourcesXmlResolver;


/**
 * Test {@link SourcesXmlResolver}.
 * @author  blair christensen.
 * @version $Id: Test_subj_SourcesXmlResolver.java,v 1.2 2007-08-10 13:19:14 blair Exp $
 * @since   @HEAD@
 */
public class Test_subj_SourcesXmlResolver extends GrouperTest {


  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }


  public void test_constructor_nullSourceManager() {
    try {
      new SourcesXmlResolver(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

}

