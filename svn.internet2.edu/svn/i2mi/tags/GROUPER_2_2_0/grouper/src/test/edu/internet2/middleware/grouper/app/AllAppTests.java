/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: AllAppTests.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.app.gsh.AllGshTests;
import edu.internet2.middleware.grouper.app.loader.AllLoaderTests;
import edu.internet2.middleware.grouper.app.usdu.AllUsduTests;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;


/**
 *
 */
public class AllAppTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app");
    //$JUnit-BEGIN$

    //$JUnit-END$
    if (GrouperConfig.getPropertyBoolean("junit.test.gsh", false)) {
      suite.addTest(AllGshTests.suite());
    }
    
    if (GrouperConfig.getPropertyBoolean("junit.test.loader", true)) {

      suite.addTest(AllLoaderTests.suite());
    }
    
    suite.addTest(AllUsduTests.suite());
    return suite;
  }

}
