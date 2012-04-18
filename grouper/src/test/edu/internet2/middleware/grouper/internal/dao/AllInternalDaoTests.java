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
 * $Id: AllInternalDaoTests.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.internal.dao.hib3.AllDaoHib3Tests;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllInternalDaoTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.internal.dao");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestGrouperDAOFactory.class);
    suite.addTestSuite(QuerySortTest.class);
    suite.addTestSuite(QueryPagingTest.class);
    //$JUnit-END$
    
    suite.addTest(AllDaoHib3Tests.suite());
    return suite;
  }

}
