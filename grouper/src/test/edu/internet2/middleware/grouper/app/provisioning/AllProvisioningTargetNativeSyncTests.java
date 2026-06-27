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
package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.adobe.GrouperAdobeProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.datadog.DatadogProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.okta.GrouperOktaProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.remedyV2.GrouperRemedyProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.scim.GrouperScim2ProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.teamDynamix.TeamDynamixProvisioningTargetNativeSyncTest;
import edu.internet2.middleware.grouper.app.truefoundry.TrueFoundryProvisioningTargetNativeSyncTest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * The fast, no-Tomcat half of the provisioner sync-back test surface: every provisioner's
 * "build native object from raw JSON" unit test, in one suite.
 *
 * <p>Each {@code *ProvisioningTargetNativeSyncTest} exercises one provisioner's
 * {@code GrouperProvisioningTargetNativeSync#buildNativeGroupFromJson} /
 * {@code buildNativeUserFromJson} in isolation -- no Tomcat, no mock, no provisioning cycle. They
 * lock in the move off the typed beans: the friendly default attribute keys, exclusion of the
 * target-id field, type coercion, and (the whole point of capturing from raw JSON) that an operator
 * can capture ANY target JSON field by name/path, including one the typed bean does not model.
 *
 * <p>Run this on its own for the quick sweep (Eclipse Run As {@literal >} JUnit Test on this class,
 * or {@link #main(String[])}) -- it needs no Tomcat. The heavier
 * {@link SyncBackAllProtocolsTestHarness} also calls {@link #suite()}, so its full cross-protocol
 * run leads with this fast unit pass before the Tomcat-gated full-sync / incremental tests.
 *
 * <p>When a new provisioner gains a native-sync test, add it to {@link #suite()} below; both this
 * harness and {@link SyncBackAllProtocolsTestHarness} pick it up automatically.
 */
public class AllProvisioningTargetNativeSyncTests {

  /**
   * Run every provisioner native-sync unit test from the command line.
   * @param args ignored
   */
  public static void main(String[] args) {
    TestRunner.run(suite());
  }

  /**
   * the suite of all per-provisioner native-sync (raw-JSON capture) unit tests
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(AllProvisioningTargetNativeSyncTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperAdobeProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperAzureProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperBoxProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(DatadogProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperDuoProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(FreshRequesterProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperGoogleProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperOktaProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperRemedyProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(GrouperScim2ProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(TeamDynamixProvisioningTargetNativeSyncTest.class);
    suite.addTestSuite(TrueFoundryProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
