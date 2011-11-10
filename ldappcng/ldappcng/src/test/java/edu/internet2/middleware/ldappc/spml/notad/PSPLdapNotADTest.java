/*
A * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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
package edu.internet2.middleware.ldappc.spml.notad;

import junit.framework.AssertionFailedError;
import junit.textui.TestRunner;

import org.openspml.v2.msg.spml.ReturnData;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.ldappc.spml.BasePSPProvisioningTest;
import edu.internet2.middleware.ldappc.spml.request.BulkCalcRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkCalcResponse;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkDiffResponse;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncRequest;
import edu.internet2.middleware.ldappc.spml.request.BulkSyncResponse;
import edu.internet2.middleware.ldappc.spml.request.CalcRequest;
import edu.internet2.middleware.ldappc.spml.request.CalcResponse;
import edu.internet2.middleware.ldappc.spml.request.DiffRequest;
import edu.internet2.middleware.ldappc.spml.request.DiffResponse;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.subject.provider.LdapSourceAdapter;

public class PSPLdapNotADTest extends BasePSPProvisioningTest {

  public static final String CONFIG_PATH = TEST_PATH + "/spml/notad";

  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  public static void main(String[] args) {
    TestRunner.run(PSPLdapNotADTest.class);
    // TestRunner.run(new PSPLdapNotADTest("testBulkCalcBushyAdd"));
  }

  public PSPLdapNotADTest(String name) {
    super(name, CONFIG_PATH);
  }

  public void setUp() {
    super.setUp();
    setUpPSP();
    setUpEduStem();
    setUpGroupA();
    setUpGroupB();
  }

  public void testBulkCalcBushyAdd() throws Exception {

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkCalcBushyAdd.response.xml");
  }

  public void testBulkCalcBushyAddMultipleSubjects() throws Exception {

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testMultipleSubjects.before.ldif");

    ((LdapSourceAdapter) SubjectFinder.getSource("ldap")).setMultipleResults(true);

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkCalcBushyAddMultipleSubjects.response.xml");
  }

  public void testBulkCalcBushyAddMultipleSubjectsTrue() throws Exception {

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testMultipleSubjects.before.ldif");

    psp.getTargetDefinitions().get("ldap").getPSODefinition("group").getReferencesDefinition("member")
        .getReferenceDefinition("membersLdap").setMultipleResults(true);

    ((LdapSourceAdapter) SubjectFinder.getSource("ldap")).setMultipleResults(true);

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkCalcBushyAddMultipleSubjectsTrue.response.xml");
  }

  public void testBulkCalcBushyAddSubgroupPhasing() throws Exception {

    groupA.addMember(groupB.toSubject());

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkCalcBushyAddSubgroupPhasing.response.xml");
  }

  public void testBulkCalcBushyAddSubgroupPhasingTwoStep() throws Exception {

    groupA.addMember(groupB.toSubject());

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkCalcBushyAddSubgroupPhasing.response.xml");
  }

  public void testBulkDiffBushyAdd() throws Exception {

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkDiffBushyAdd.response.xml");
  }

  public void testBulkDiffBushyAddMultipleSubjects() throws Exception {

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testMultipleSubjects.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkDiffBushyAddMultipleSubjects.response.xml");
  }

  public void testBulkDiffBushyAddMultipleSubjectsTrue() throws Exception {

    psp.getTargetDefinitions().get("ldap").getPSODefinition("group").getReferencesDefinition("member")
        .getReferenceDefinition("membersLdap").setMultipleResults(true);

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testMultipleSubjects.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkDiffBushyAddMultipleSubjectsTrue.response.xml");
  }

  public void testBulkDiffBushyAddSubgroupPhasing() throws Exception {

    groupA.addMember(groupB.toSubject());

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkDiffBushyAddSubgroupPhasing.response.xml");
  }

  public void testBulkSyncBushyAdd() throws Exception {

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkSyncBushyAdd.response.xml");
    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testBulkSyncBushyAdd.after.ldif");
  }

  public void testBulkSyncBushyAddMultipleSubjectsTrue() throws Exception {

    psp.getTargetDefinitions().get("ldap").getPSODefinition("group").getReferencesDefinition("member")
        .getReferenceDefinition("membersLdap").setMultipleResults(true);

    ((LdapSourceAdapter) SubjectFinder.getSource("ldap")).setMultipleResults(true);

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testMultipleSubjects.before.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testBulkSyncBushyAddMultipleSubjectsTrue.response.xml");

    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testBulkSyncBushyAddMultipleSubjectsTrue.after.ldif");
  }

  // target ldap directory must not support referential integrity of dns
  // TODO implement
  public void testBulkSyncBushyAddSubgroupPhasing() throws Exception {

    // loadLdif(PSPLdapTest.DATA_PATH + "PSPTest.before.ldif");

    groupA.addMember(groupB.toSubject());

    // CalcRequest request = new CalcRequest();
    // SyncRequest request = new SyncRequest();
    // request.setRequestID(REQUESTID_TEST);
    // request.setReturnData(ReturnData.DATA);
    // request.setId(groupA.getName());
    // CalcResponse response = psp.execute(request);
    // Response response = psp.execute(request);
    // System.out.println(psp.toXML(response));

    // BulkSyncRequest request = new BulkSyncRequest();
    // request.setRequestID(REQUESTID_TEST);
    // request.setReturnData(ReturnData.DATA);
    // BulkSyncResponse response = psp.execute(request);
    // System.out.println(psp.toXML(response));

    // verifySpml(response, DATA_PATH +
    // "PSPLdapNotADTest.testBulkSyncBushyAddSubgroupPhasing.response.xml");
    // verifyLdif(DATA_PATH +
    // "PSPLdapNotADTest.testBulkSyncBushyAddSubgroupPhasing.after.ldif");
  }

  public void testCalcFlatAddEmptyList() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    CalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testCalcFlatAddEmptyList.response.xml");
  }

  public void testCalcFlatAddEmptyListReturnData() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setReturnData(ReturnData.DATA);
    CalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testCalcFlatAddEmptyListReturnData.response.xml");
  }

  public void testDiffFlatAddEmptyList() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testDiffFlatAddEmptyList.response.xml");
  }

  public void testDiffFlatAddEmptyListReturnData() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setReturnData(ReturnData.DATA);
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testDiffFlatAddEmptyListReturnData.response.xml");
  }

  public void testDiffFlatModifyEmptyListAddMember() throws Exception {

    this.makeGroupDNStructureFlat();

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testDiffFlatModifyEmptyListAddMember.before.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testDiffFlatModifyEmptyListAddMember.response.xml");
  }

  public void testDiffFlatModifyEmptyListDeleteMember() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testDiffFlatModifyEmptyListDeleteMember.before.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testDiffFlatModifyEmptyListDeleteMember.response.xml");
  }

  public void testSyncFlatAddEmptyList() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testSyncFlatAddEmptyList.response.xml");
    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatAddEmptyList.after.ldif");
  }

  public void testSyncFlatAddEmptyListReturnData() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setReturnData(ReturnData.DATA);
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testSyncFlatAddEmptyListReturnData.response.xml");
    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatAddEmptyList.after.ldif");
  }

  public void testSyncFlatModifyEmptyListAddMember() throws Exception {

    this.makeGroupDNStructureFlat();

    // if (this.useEmbedded()) {
    // psp.getTargetDefinitions().get("ldap").setBundleModifications(false);
    // }

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListAddMember.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    try {
      verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListAddMember.response.xml");
    } catch (AssertionFailedError e) {
      if (useEmbedded()) {
        // OK
      } else {
        throw e;
      }
    }

    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListAddMember.after.ldif");
  }

  public void testSyncFlatModifyEmptyListAddMemberUnbundled() throws Exception {

    this.makeGroupDNStructureFlat();

    psp.getTargetDefinitions().get("ldap").setBundleModifications(false);

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListAddMember.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListAddMemberUnbundled.response.xml");
    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListAddMember.after.ldif");
  }

  public void testSyncFlatModifyEmptyListDeleteMember() throws Exception {

    this.makeGroupDNStructureFlat();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testDiffFlatModifyEmptyListDeleteMember.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    try {
      verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListDeleteMember.response.xml");
    } catch (AssertionFailedError e) {
      if (useEmbedded()) {
        // OK
      } else {
        throw e;
      }
    }

    if (!psp.getTargetDefinitions().get("ldap").isBundleModifications()) {
      verifyLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatAddEmptyList.after.ldif");
    }
  }

  public void testSyncFlatModifyEmptyListDeleteMemberUnbundled() throws Exception {

    this.makeGroupDNStructureFlat();

    psp.getTargetDefinitions().get("ldap").setBundleModifications(false);

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    loadLdif(DATA_PATH + "PSPLdapNotADTest.testDiffFlatModifyEmptyListDeleteMember.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPLdapNotADTest.testSyncFlatModifyEmptyListDeleteMemberUnbundled.response.xml");
    verifyLdif(DATA_PATH + "PSPLdapNotADTest.testSyncFlatAddEmptyList.after.ldif");
  }

}
