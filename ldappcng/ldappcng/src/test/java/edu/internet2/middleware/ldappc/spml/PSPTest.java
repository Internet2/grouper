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

package edu.internet2.middleware.ldappc.spml;

import junit.textui.TestRunner;

import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.ExecutionMode;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.msg.spmlupdates.UpdatesRequest;

import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.LdapSearchRequest;

public class PSPTest extends BasePSPProvisioningTest {

  public static final String CONFIG_PATH = TEST_PATH + "/spml";

  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  public static void main(String[] args) {
    TestRunner.run(PSPTest.class);
    // TestRunner.run(new PSPTest("testSearchRequestNoQuery"));
  }

  public PSPTest(String name) {
    super(name, CONFIG_PATH);
  }

  public void setUp() {
    super.setUp();
    setUpPSP();
  }
  
  public void testAddRequestNoPSOId() throws Exception {

    AddRequest request = new AddRequest();
    request.setRequestID(REQUESTID_TEST);
    AddResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testAddRequestNoPSOId.response.xml");
  }

  public void testAddRequestNoTargetId() throws Exception {

    AddRequest request = new AddRequest();
    request.setRequestID(REQUESTID_TEST);
    PSOIdentifier psoID = new PSOIdentifier();
    psoID.setID("ID");
    request.setPsoID(psoID);
    AddResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testAddRequestNoTargetId.response.xml");
  }

  public void testCalcMalformedRequest() throws Exception {

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    CalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testCalcMalformedRequest.response.xml");
  }

  public void testLookupMalformedRequest() throws Exception {

    LookupRequest request = new LookupRequest();
    request.setRequestID(REQUESTID_TEST);
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupMalformedRequest.response.xml");
  }

  public void testSearchRequestNoBase() throws Exception {

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setFilter("cn=test.subject.0");
    ldapSearchRequest.setTargetId("ldap");
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestNoBase.response.xml");
  }

  public void testSearchRequestNoFilter() throws Exception {

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setBase("ou=testpeople," + base);
    ldapSearchRequest.setTargetId("ldap");
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestNoFilter.response.xml");
  }

  public void testSearchRequestNoQuery() throws Exception {

    SearchRequest request = new SearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestNoQuery.response.xml");
  }

  public void testSearchRequestNoTargetId() throws Exception {

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestNoTargetId.response.xml");
  }

  public void testUnsupportedExecutionMode() {

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setRequestID(REQUESTID_TEST);
    searchRequest.setExecutionMode(ExecutionMode.ASYNCHRONOUS);
    Response response = psp.execute((Request) searchRequest);
    verifySpml(response, DATA_PATH + "PSPTest.unsupportedExecutionMode.response.xml");
  }

  public void testUnsupportedOperation() {

    UpdatesRequest updatesRequest = new UpdatesRequest();
    updatesRequest.setRequestID(REQUESTID_TEST);
    Response response = psp.execute(updatesRequest);
    verifySpml(response, DATA_PATH + "PSPTest.unsupportedOperation.response.xml");
  }
}
