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
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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
package edu.internet2.middleware.ldappc.spml.ad;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.ldappc.LdappcTestHelper;
import edu.internet2.middleware.ldappc.spml.BasePSPProvisioningTest;
import edu.internet2.middleware.ldappc.spml.PSPLdapTest;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.DiffRequest;
import edu.internet2.middleware.ldappc.spml.request.DiffResponse;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;

public class PSPLdapADRangeTest extends BasePSPProvisioningTest {

  private static final String CONFIG_PATH = TEST_PATH + "/spml/ad";

  private static final String DATA_PATH = CONFIG_PATH + "/data/";

  public PSPLdapADRangeTest(String name) {
    super(name, CONFIG_PATH);
  }

  public static void main(String[] args) {
    TestRunner.run(PSPLdapADRangeTest.class);
    // TestRunner.run(new PSPLdapADRangeTest("testBulkSyncBushyAddSubgroupPhasingTwoStep"));
  }

  public void testGroup3000() throws Exception {

    this.makeGroupDNStructureFlat();

    loadLdif(PSPLdapTest.DATA_PATH + "PSPTest.before.ldif");
    String personLdif = LdappcTestHelper.readFile(LdappcTestHelper.getFile(DATA_PATH + "PSPTest.person.ldif"));

    int subjects = 3002;
    for (int i = 2; i < subjects; i++) {
      String loadLdif = personLdif.replace("${i}", Integer.toString(i));
      LdappcTestHelper.loadLdif(loadLdif, propertiesFile, ldap);
    }

    RegistryReset._addSubjects(10, subjects);

    Group groupC = StemHelper.addChildGroup(this.edu, "groupC", "Group C");
    groupC.setDescription("descriptionC");
    groupC.store();
    for (int i = 0; i < subjects; i++) {
      groupC.addMember(SubjectFinder.findById("test.subject." + i, true));
    }

    CalcRequest calcRequest = new CalcRequest();
    calcRequest.setRequestID(REQUESTID_TEST);
    calcRequest.setId(groupC.getName());
    CalcResponse calcResponse = psp.execute(calcRequest);
    verifySpml(calcResponse, DATA_PATH + "PSPLdapADTest.testGroup3000.calcResponse.xml");

    DiffRequest diffRequest = new DiffRequest();
    diffRequest.setRequestID(REQUESTID_TEST);
    diffRequest.setId(groupC.getName());
    DiffResponse diffResponse = psp.execute(diffRequest);
    verifySpml(diffResponse, DATA_PATH + "PSPLdapADTest.testGroup3000.diffResponse.xml");

    SyncRequest syncRequest = new SyncRequest();
    syncRequest.setRequestID(REQUESTID_TEST);
    syncRequest.setId(groupC.getName());
    SyncResponse syncResponse = psp.execute(syncRequest);
    verifySpml(syncResponse, DATA_PATH + "PSPLdapADTest.testGroup3000.syncResponse.xml");

    verifyLdif(DATA_PATH + "PSPLdapADTest.testGroup3000.after.ldif", "ou=testgroups," + base);
  }
}
