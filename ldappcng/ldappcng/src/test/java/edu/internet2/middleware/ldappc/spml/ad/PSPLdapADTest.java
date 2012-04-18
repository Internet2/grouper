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

import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.StatusCode;

import edu.internet2.middleware.ldappc.spml.BasePSPProvisioningTest;
import edu.internet2.middleware.ldappc.spml.PSPLdapTest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncResponse;

public class PSPLdapADTest extends BasePSPProvisioningTest {

  private static final String CONFIG_PATH = TEST_PATH + "/spml/ad";

  private static final String DATA_PATH = CONFIG_PATH + "/data/";

  public PSPLdapADTest(String name) {
    super(name, CONFIG_PATH);
  }

  public static void main(String[] args) {
    TestRunner.run(PSPLdapADTest.class);
    // TestRunner.run(new PSPLdapADTest("testBulkSyncBushyAddSubgroupPhasingTwoStep"));
  }

  public void testBulkSyncBushyAddSubgroupPhasing() throws Exception {

    loadLdif(PSPLdapTest.DATA_PATH + "PSPTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    assertEquals(StatusCode.FAILURE, response.getStatus());

    verifySpml(response, DATA_PATH + "PSPLdapADTest.testBulkSyncBushyAddSubgroupPhasing.response.xml");
  }

  public void testBulkSyncBushyAddSubgroupPhasingTwoStep() throws Exception {

    loadLdif(PSPLdapTest.DATA_PATH + "PSPTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    BulkSyncRequest dataRequest = new BulkSyncRequest();
    dataRequest.setRequestID(REQUESTID_TEST);
    dataRequest.setReturnData(ReturnData.DATA);
    BulkSyncResponse dataResponse = psp.execute(dataRequest);

    verifySpml(dataResponse, DATA_PATH + "PSPLdapADTest.testBulkSyncBushyAddSubgroupPhasingTwoStep.data.response.xml");

    BulkSyncRequest everythingRequest = new BulkSyncRequest();
    everythingRequest.setRequestID(REQUESTID_TEST);
    everythingRequest.setReturnData(ReturnData.EVERYTHING);
    BulkSyncResponse everythingResponse = psp.execute(everythingRequest);

    verifySpml(everythingResponse, DATA_PATH
        + "PSPLdapADTest.testBulkSyncBushyAddSubgroupPhasingTwoStep.everything.response.xml");

    verifyLdif(DATA_PATH + "PSPLdapADTest.testBulkSyncBushyAddSubgroupPhasing.after.ldif");
  }

}
