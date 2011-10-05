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

import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.msg.spmlref.HasReference;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.Scope;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.shibboleth.util.OnNotFound;
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
import edu.internet2.middleware.ldappc.spml.request.LdapSearchRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.subject.Subject;

public class PSPLdapTest extends BasePSPProvisioningTest {

  public static final String CONFIG_PATH = TEST_PATH + "/spml";

  public static final String DATA_PATH = CONFIG_PATH + "/data/";

  public static void main(String[] args) {
    // TestRunner.run(PSPLdapTest.class);
    TestRunner.run(new PSPLdapTest("testLookupNoSuchIdentifier"));
  }

  public PSPLdapTest(String name) {
    super(name, CONFIG_PATH);
  }

  public void testBulkCalcBushyAdd() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAdd.response.xml");
  }

  public void testBulkCalcBushyAddChildStems() throws Exception {

    this.setUpCourseTest();

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddChildStems.response.xml");
  }

  public void testBulkCalcBushyAddForwardSlash() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(groupF.toSubject());

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddForwardSlash.response.xml");
  }

  public void testBulkCalcBushyAddSubgroup() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddSubgroup.response.xml");
  }

  public void testBulkCalcBushyAddSubjectNotFoundFail() throws Exception {

    psp.getTargetDefinitions().get("ldap").getPSODefinition("group").getReferencesDefinition("member")
        .getReferenceDefinition("members-jdbc").setOnNotFound(OnNotFound.fail);

    groupA.addMember(SubjectTestHelper.SUBJ2);

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddSubjectNotFoundFail.response.xml");
  }

  public void testBulkCalcBushyAddSubjectNotFoundIgnore() throws Exception {

    psp.getTargetDefinitions().get("ldap").getPSODefinition("group").getReferencesDefinition("member")
        .getReferenceDefinition("members-jdbc").setOnNotFound(OnNotFound.ignore);

    groupA.addMember(SubjectTestHelper.SUBJ2);

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddSubjectNotFound.response.xml");
  }

  public void testBulkCalcBushyAddSubjectNotFoundWarn() throws Exception {

    groupA.addMember(SubjectTestHelper.SUBJ2);

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddSubjectNotFound.response.xml");
  }

  public void testBulkCalcBushyAddSubjectWhitespace() throws Exception {

    Subject subjA = this.createSubject("test subject a", "my name is test subject a");
    groupA.addMember(subjA);

    loadLdif(DATA_PATH + "PSPTest.subjectWhitespace.before.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkCalcBushyAddSubjectWhitespace.response.xml");
  }

  public void testBulkCalcBushyDeleteForwardSlash() throws Exception {

    groupA.delete();
    groupB.delete();

    loadLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddForwardSlash.after.ldif");

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.emptyBulkCalc.response.xml");
  }

  public void testBulkDiffBushyAdd() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkDiffBushyAdd.response.xml");
  }

  public void testBulkDiffBushyAddChildStems() throws Exception {

    this.setUpCourseTest();

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkDiffBushyAddChildStems.response.xml");
  }

  public void testBulkDiffBushyAddForwardSlash() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(groupF.toSubject());

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkDiffBushyAddForwardSlash.response.xml");
  }

  public void testBulkDiffBushyAddSubgroup() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkDiffBushyAddSubgroup.response.xml");
  }

  public void testBulkDiffBushyAddSubjectWhitespace() throws Exception {

    Subject subjA = this.createSubject("test subject a", "my name is test subject a");
    groupA.addMember(subjA);

    loadLdif(DATA_PATH + "PSPTest.subjectWhitespace.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkDiffBushyAddSubjectWhitespace.response.xml");
  }

  public void testBulkDiffBushyDeleteForwardSlash() throws Exception {

    groupA.delete();
    groupB.delete();

    loadLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddForwardSlash.after.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkDiffBushyDeleteForwardSlash.response.xml");
  }

  public void testBulkSyncBushyAdd() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkSyncBushyAdd.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAdd.after.ldif");
  }

  public void testBulkSyncBushyAddChildStems() throws Exception {

    this.setUpCourseTest();

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkSyncBushyAddChildStems.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddChildStems.after.ldif");
  }

  public void testBulkSyncBushyAddForwardSlash() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ1);
    groupB.addMember(groupF.toSubject());

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkSyncBushyAddForwardSlash.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddForwardSlash.after.ldif");
  }

  public void testBulkSyncBushyAddSubgroup() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    groupB.addMember(groupA.toSubject());

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkSyncBushyAddSubgroup.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddSubgroup.after.ldif");
  }

  public void testBulkSyncBushyAddSubjectWhitespace() throws Exception {

    Subject subjA = this.createSubject("test subject a", "my name is test subject a");
    groupA.addMember(subjA);

    loadLdif(DATA_PATH + "PSPTest.subjectWhitespace.before.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkSyncBushyAddSubjectWhitespace.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddSubjectWhitespace.after.ldif");
  }

  public void testBulkSyncBushyDeleteForwardSlash() throws Exception {

    groupA.delete();
    groupB.delete();

    loadLdif(DATA_PATH + "PSPTest.testBulkSyncBushyAddForwardSlash.after.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testBulkSyncBushyDeleteForwardSlash.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testBulkSyncBushyDeleteForwardSlash.after.ldif");
  }

  public void testCalcBushyAdd() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    CalcResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testCalcBushyAdd.response.xml");
  }
  
	public void testCalcBushyAddScoped() throws Exception {

		loadLdif(DATA_PATH + "PSPTest.testCalcBushyAddScoped.before.ldif");

		CalcRequest request = new CalcRequest();
		request.setRequestID(REQUESTID_TEST);
		request.setId(groupB.getName());
		CalcResponse response = psp.execute(request);

		verifySpml(response, DATA_PATH + "PSPTest.testCalcBushyAddScoped.response.xml");
	}

  public void testCalcFlatAdd() throws Exception {

    this.makeGroupDNStructureFlat();

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    CalcResponse response = (CalcResponse) psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testCalcFlatAdd.response.xml");
  }

  public void testCalcFlatAddSchemaEntity() throws Exception {

    this.makeGroupDNStructureFlat();

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.addSchemaEntity(new SchemaEntityRef("ldap", "group"));
    CalcResponse response = (CalcResponse) psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testCalcFlatAdd.response.xml");
  }

  public void testDiffBushyModifyDescription() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.before.ldif");

    groupB.setDescription("new description");
    groupB.store();

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testDiffBushyModifyDescription.response.xml");
  }

  public void testDiffBushyModifyMember() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testDiffBushyModifyMember.response.xml");
  }

  public void testDiffBushyModifyMemberForwardSlash() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ0);

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushyForwardSlash.before.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupF.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testDiffBushyModifyMemberForwardSlash.response.xml");
  }

  public void testDiffBushyModifyMemberUnbundled() throws Exception {

    psp.getTargetDefinitions().get("ldap").setBundleModifications(false);

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    DiffResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testDiffBushyModifyMemberUnbundled.response.xml");
  }

  public void testLookupData() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testLookup.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setReturnData(ReturnData.DATA);
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=groupB,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupData.response.xml");
  }

  public void testLookupDataForwardSlash() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ1);

    loadLdif(DATA_PATH + "PSPTest.testLookupForwardSlash.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setReturnData(ReturnData.DATA);
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=group/F,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupDataForwardSlash.response.xml");
  }

  public void testLookupEverything() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testLookup.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setReturnData(ReturnData.EVERYTHING);
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=groupB,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupEverything.response.xml");
  }

  public void testLookupIdentifier() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testLookup.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setReturnData(ReturnData.IDENTIFIER);
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=groupB,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupIdentifier.response.xml");
  }

  public void testLookupIdentifierForwardSlash() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ1);

    loadLdif(DATA_PATH + "PSPTest.testLookupForwardSlash.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setReturnData(ReturnData.IDENTIFIER);
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=group/F,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupIdentifierForwardSlash.response.xml");
  }

  public void testLookupIdentifierForwardSlashEscaped() throws Exception {

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ1);

    loadLdif(DATA_PATH + "PSPTest.testLookupForwardSlash.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setReturnData(ReturnData.IDENTIFIER);
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=group\\/F,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupIdentifierForwardSlash.response.xml");
  }

  public void testLookupNoSuchIdentifier() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testLookup.before.ldif");

    LookupRequest request = new LookupRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setPsoID(new PSOIdentifier("cn=UnknownGroup,ou=edu,ou=testgroups," + base, null, "ldap"));
    Response response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, DATA_PATH + "PSPTest.testLookupNoSuchIdentifier.response.xml");
  }

  public void testSearchRequest() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testSyncFlatAdd.after.ldif");

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setBase("ou=testgroups," + base);
    ldapSearchRequest.setFilter("cn=groupB");
    ldapSearchRequest.setTargetId("ldap");
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequest.response.xml");
  }

  public void testSearchRequestData() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testSyncFlatAdd.after.ldif");

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setBase("ou=testgroups," + base);
    ldapSearchRequest.setFilter("cn=groupB");
    ldapSearchRequest.setTargetId("ldap");
    ldapSearchRequest.setReturnData(ReturnData.DATA);
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestData.response.xml");
  }

  public void testSearchRequestEverything() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testSyncFlatAdd.after.ldif");

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setBase("ou=testgroups," + base);
    ldapSearchRequest.setFilter("cn=groupB");
    ldapSearchRequest.setTargetId("ldap");
    ldapSearchRequest.setReturnData(ReturnData.EVERYTHING);
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequest.response.xml");
  }

  public void testSearchRequestIdentifier() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testSyncFlatAdd.after.ldif");

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setBase("ou=testgroups," + base);
    ldapSearchRequest.setFilter("cn=groupB");
    ldapSearchRequest.setTargetId("ldap");
    ldapSearchRequest.setReturnData(ReturnData.IDENTIFIER);
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestIdentifier.response.xml");
  }

  public void testSearchRequestNotFound() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testSyncFlatAdd.after.ldif");

    LdapSearchRequest ldapSearchRequest = new LdapSearchRequest();
    ldapSearchRequest.setBase("ou=testgroups," + base);
    ldapSearchRequest.setFilter("cn=NOT_FOUND");
    ldapSearchRequest.setTargetId("ldap");
    ldapSearchRequest.setReturnData(ReturnData.EVERYTHING);
    SearchRequest request = ldapSearchRequest.getSearchRequest();
    request.setRequestID(REQUESTID_TEST);
    SearchResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestNotFound.response.xml");
  }

  public void testSearchRequestHasReference() throws Exception {
    
    loadLdif(DATA_PATH + "PSPTest.testSearchRequestHasReference.before.ldif");
    
    PSOIdentifier memberID = new PSOIdentifier();
    memberID.setID("cn=test.subject.0,ou=testpeople," + base);
    
    HasReference hasReference = new HasReference();
    hasReference.setToPsoID(memberID);
    hasReference.setTypeOfReference("member");

    Query query = new Query();
    PSOIdentifier groupID = new PSOIdentifier();
    groupID.setID("cn=groupB,ou=testgroups," + base);
    // TODO not necessary ? groupID.setTargetID("ldap");
    query.setBasePsoID(groupID);
    query.setTargetID("ldap");
    query.addQueryClause(hasReference);
    query.setScope(Scope.PSO);

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setReturnData(ReturnData.EVERYTHING);
    searchRequest.setQuery(query);

    SearchResponse response = psp.execute(searchRequest);
    System.out.println(response.toXML(psp.getXMLMarshaller()));
    
    verifySpml(response, DATA_PATH + "PSPTest.testSearchRequestHasReference.response.xml");    
  }
  
  public void testSyncBushyModifyDescription() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.before.ldif");

    groupB.setDescription("new description");
    groupB.store();

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSyncBushyModifyDescription.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testModifyDescriptionBushy.after.ldif");
  }

  public void testSyncBushyModifyMember() throws Exception {

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSyncBushyModifyMember.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.after.ldif");
  }

  public void testSyncBushyModifyMemberForwardSlash() throws Exception {

    if (this.useEmbedded()) {
      psp.getTargetDefinitions().get("ldap").setBundleModifications(false);
    }

    Group groupF = StemHelper.addChildGroup(this.edu, "group/F", "Group/F");
    groupF.addMember(SubjectTestHelper.SUBJ0);

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushyForwardSlash.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupF.getName());
    SyncResponse response = psp.execute(request);

    if (this.useEmbedded()) {
      verifySpml(response, DATA_PATH + "PSPTest.testSyncBushyModifyMemberForwardSlashUnbundled.response.xml");
    } else {
      verifySpml(response, DATA_PATH + "PSPTest.testSyncBushyModifyMemberForwardSlash.response.xml");
    }
    verifyLdif(DATA_PATH + "PSPTest.testModifyMemberBushyForwardSlash.after.ldif");
  }

  public void testSyncBushyModifyMemberUnbundled() throws Exception {

    psp.getTargetDefinitions().get("ldap").setBundleModifications(false);

    loadLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.before.ldif");

    groupB.addMember(groupA.toSubject());

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSyncBushyModifyMemberUnbundled.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testModifyMemberBushy.after.ldif");
  }

  public void testSyncFlatAdd() throws Exception {

    this.makeGroupDNStructureFlat();

    loadLdif(DATA_PATH + "PSPTest.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    SyncResponse response = psp.execute(request);

    verifySpml(response, DATA_PATH + "PSPTest.testSyncFlatAdd.response.xml");
    verifyLdif(DATA_PATH + "PSPTest.testSyncFlatAdd.after.ldif");
  }

}
