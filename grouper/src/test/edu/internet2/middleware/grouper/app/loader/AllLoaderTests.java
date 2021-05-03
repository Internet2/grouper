/**
 * Copyright 2014 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * @author mchyzer $Id: AllLoaderTests.java,v 1.2 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.app.loader.db.AllLoaderDbTests;
import edu.internet2.middleware.grouper.app.loader.ldap.AllLoaderLdapTests;

/**
 *
 */
public class AllLoaderTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.app.loader");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperDaemonDeleteOldRecordsTest.class);
    suite.addTestSuite(GrouperLoaderCleanLogsTest.class);
    suite.addTestSuite(GrouperLoaderOtherJobsTest.class);
    suite.addTestSuite(GrouperLoaderQuartzTest.class);
    suite.addTestSuite(GrouperLoaderSecurityTest.class);
    suite.addTestSuite(GrouperLoaderTest.class);
    suite.addTestSuite(GrouperDaemonSchedulerCheckTest.class);
    suite.addTestSuite(NotificationDaemonTest.class);
    suite.addTestSuite(OtherJobScriptTest.class);
    //$JUnit-END$
    suite.addTest(AllLoaderDbTests.suite());
    suite.addTest(AllLoaderLdapTests.suite());
    return suite;
  }

}
