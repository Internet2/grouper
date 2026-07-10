/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: AllLoaderDbTests.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.azure.AllAzureProvisionerTests;
import edu.internet2.middleware.grouper.app.duo.AllDuoProvisionerTests;
import edu.internet2.middleware.grouper.app.duo.role.AllDuoRoleProvisionerTests;
import edu.internet2.middleware.grouper.app.google.AllGoogleProvisionerTests;
import edu.internet2.middleware.grouper.app.ldapProvisioning.AllLdapProvisioningTests;
import edu.internet2.middleware.grouper.app.messagingProvisioning.AllMessagingProvisioningTests;
import edu.internet2.middleware.grouper.app.scim.AllScimProvisionerTests;
import edu.internet2.middleware.grouper.app.sqlProvisioning.AllSqlProvisioningTests;
import edu.internet2.middleware.grouper.app.usdu.UsduJobProvisionerSyncTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * test suite
 */
public class AllProvisioningTestsManual {

  public static void main(String[] args) {
    TestRunner.run(AllProvisioningTestsManual.suite());
  }
  
  /**
   * test
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.loader.db");
    suite.addTest(AllAzureProvisionerTests.suite());
    suite.addTest(AllDuoProvisionerTests.suite());
    suite.addTest(AllDuoRoleProvisionerTests.suite());
    suite.addTest(AllGoogleProvisionerTests.suite());
    suite.addTest(AllLdapProvisioningTests.suite());
    suite.addTest(AllMessagingProvisioningTests.suite());
    suite.addTest(AllScimProvisionerTests.suite());
    suite.addTest(AllSqlProvisioningTests.suite());
    suite.addTest(AllProvisioningTests.suite());

    suite.addTestSuite(UsduJobProvisionerSyncTest.class);

    return suite;
  }

}
