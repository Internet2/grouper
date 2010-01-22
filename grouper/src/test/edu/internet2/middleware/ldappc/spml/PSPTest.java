/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.util.resource.ResourceException;
import org.openspml.v2.msg.Marshallable;
import org.openspml.v2.msg.spml.AddRequest;
import org.openspml.v2.msg.spml.AddResponse;
import org.openspml.v2.msg.spml.ExecutionMode;
import org.openspml.v2.msg.spml.ListTargetsRequest;
import org.openspml.v2.msg.spml.ListTargetsResponse;
import org.openspml.v2.msg.spml.LookupRequest;
import org.openspml.v2.msg.spml.LookupResponse;
import org.openspml.v2.msg.spml.PSOIdentifier;
import org.openspml.v2.msg.spml.Request;
import org.openspml.v2.msg.spml.Response;
import org.openspml.v2.msg.spml.ReturnData;
import org.openspml.v2.msg.spml.Schema;
import org.openspml.v2.msg.spml.SchemaEntityRef;
import org.openspml.v2.msg.spml.Target;
import org.openspml.v2.msg.spmlsearch.Query;
import org.openspml.v2.msg.spmlsearch.SearchRequest;
import org.openspml.v2.msg.spmlsearch.SearchResponse;
import org.openspml.v2.msg.spmlupdates.UpdatesRequest;
import org.openspml.v2.profiles.spmldsml.AttributeDefinitionReference;
import org.openspml.v2.profiles.spmldsml.DSMLSchema;
import org.openspml.v2.profiles.spmldsml.ObjectClassDefinition;
import org.slf4j.Logger;
import org.springframework.context.support.GenericApplicationContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcTestHelper;
import edu.internet2.middleware.ldappc.spml.definitions.TargetDefinition;
import edu.internet2.middleware.ldappc.spml.provider.LdapTargetProvider;
import edu.internet2.middleware.ldappc.spml.provider.SpmlTargetProvider;
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
import edu.internet2.middleware.ldappc.spml.request.LdapFilterQueryClause;
import edu.internet2.middleware.ldappc.spml.request.SyncRequest;
import edu.internet2.middleware.ldappc.spml.request.SyncResponse;
import edu.internet2.middleware.ldappc.util.PSPUtil;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.pool.LdapPoolException;

public class PSPTest extends GrouperTest {

  private static final Logger LOG = GrouperUtil.getLogger(PSPTest.class);

  private PSP psp;

  protected GrouperSession grouperSession;

  protected Stem edu;

  protected Stem root;

  protected Stem groupsStem;

  protected Stem coursesStem;

  private Group groupA;

  private Group groupB;

  // private Group courseA;

  // private Group courseB;

  private GenericApplicationContext gContext;

  private List<String> targetIds = new ArrayList<String>();

  public static final String REQUESTID_TEST = "REQUESTID_TEST";

  public static final String GROUPER_BASE_DN = "dc=grouper,dc=edu";

  public void setUp() {

    super.setUp();

    try {
      gContext = PSPUtil.createSpringContext("/edu/internet2/middleware/ldappc/spml/ldappc-internal.xml",
          "/edu/internet2/middleware/ldappc/spml/ldappc-services.xml");
    } catch (ResourceException e) {
      LOG.error("Unable to load Spring application context.", e);
      fail("Unable to load Spring application context : " + e.getMessage());
    }

    // load PSP
    psp = (PSP) gContext.getBean("ldappc");

    grouperSession = SessionHelper.getRootSession();

    root = StemHelper.findRootStem(grouperSession);
    edu = StemHelper.addChildStem(root, "edu", "education");

    groupsStem = StemHelper.addChildStem(edu, "groups", "groups");
    coursesStem = StemHelper.addChildStem(edu, "coursesStem", "coursesStem");

    groupA = StemHelper.addChildGroup(this.edu, "groupA", "Group A");
    groupA.addMember(SubjectTestHelper.SUBJ0);

    groupB = StemHelper.addChildGroup(this.edu, "groupB", "Group B");
    groupB.addMember(SubjectTestHelper.SUBJ1);
    groupB.setDescription("descriptionB");
    groupB.store();

    // courseA = StemHelper.addChildGroup(coursesStem, "courseA", "Course A");
    // courseB = StemHelper.addChildGroup(coursesStem, "courseB", "Course B");

    // by default test all targets
    for (TargetDefinition targetDefinition : psp.getTargetDefinitions().values()) {
      targetIds.add(targetDefinition.getId());
    }

    prompt();
  }

  protected void prompt() {

    for (TargetDefinition targetDefinition : psp.getTargetDefinitions().values()) {

      // just test some
      if (!targetIds.contains(targetDefinition.getId())) {
        continue;
      }

      SpmlTargetProvider spmlTargetProvider = targetDefinition.getProvider();
      if (!(spmlTargetProvider instanceof LdapTargetProvider)) {
        LOG.warn("Only " + LdapTargetProvider.class + " targets are currently supported.");
        continue;
      }
      LdapTargetProvider ldapTargetProvider = (LdapTargetProvider) spmlTargetProvider;

      Ldap ldap = null;
      try {
        ldap = ldapTargetProvider.getLdapPool().checkOut();
        // prompt user
        GrouperUtil.promptUserAboutChanges("test ldap", true, "ldap", ldap.getLdapConfig().getLdapUrl(), ldap
            .getLdapConfig().getServiceUser());
      } catch (LdapPoolException e) {
        LOG.error("Error using the ldap pool.", e);
        fail("Error using the ldap pool : " + e.getMessage());
      } finally {
        ldapTargetProvider.getLdapPool().checkIn(ldap);
      }
    }
  }

  protected void setTargets(String... targetId) {
    targetIds.clear();
    for (String target : targetId) {
      targetIds.add(target);
    }
  }

  public void tearDown() {
    super.tearDown();

    for (TargetDefinition targetDefinition : psp.getTargetDefinitions().values()) {
      LdapTargetProvider ldapTargetProvider = (LdapTargetProvider) targetDefinition.getProvider();

      Ldap ldap = null;
      try {
        ldap = ldapTargetProvider.getLdapPool().checkOut();

        LOG.debug("destroy test DIT");
        // TODO deleteChildren("dc=grouper,dc=edu", ldap);

      } catch (Exception e) {
        e.printStackTrace();
        fail("An error occurred : " + e.getMessage());
      } finally {
        if (ldap != null) {
          ldap.close();
          ldapTargetProvider.getLdapPool().checkIn(ldap);
          ldapTargetProvider.getLdapPool().close();
        }
      }
    }

    gContext.close();
  }

  public void testCalculateFlat() {

    prompt();

    loadLdif("data/PSPTest.before.ldif");

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    CalcResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testCalculateFlat.response.xml");
  }

  public void testCalculateEmptyListFlat() {

    prompt();

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    loadLdif("data/PSPTest.before.ldif");

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setTargetIds(targetIds);
    CalcResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testCalculateEmptyListFlat.response.xml");
  }

  public void testCalculateSubgroupFlat() throws Exception {

    prompt();

    groupB.addMember(groupA.toSubject());

    loadLdif("data/PSPTest.before.ldif");

    CalcRequest request = new CalcRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    CalcResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testCalculateSubgroupFlat.response.xml");
  }

  public void testLookup() {

    loadLdif("data/PSPTest.testLookup.before.ldif");

    LookupRequest request;
    Response response;
    PSOIdentifier psoID;

    // test invalid name
    request = new LookupRequest();
    request.setRequestID(REQUESTID_TEST);
    response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, "data/PSPTest.testLookup.malformedRequest.xml");

    // test no target id
    request = new LookupRequest();
    request.setRequestID(REQUESTID_TEST);
    psoID = new PSOIdentifier();
    psoID.setID("foo");
    request.setPsoID(psoID);
    response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, "data/PSPTest.testLookup.noSuchIdentifier.xml");

    // test returning identifier only
    request = new LookupRequest();
    request.setReturnData(ReturnData.IDENTIFIER);
    request.setRequestID(REQUESTID_TEST);
    psoID = new PSOIdentifier();
    psoID.setID("cn=groupB,ou=groups,dc=grouper,dc=edu");
    psoID.setTargetID("openldap-prod");
    request.setPsoID(psoID);
    response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, "data/PSPTest.testLookup.groupBIdentifier.xml");

    // test returning data only
    request = new LookupRequest();
    request.setReturnData(ReturnData.DATA);
    request.setRequestID(REQUESTID_TEST);
    psoID = new PSOIdentifier();
    psoID.setID("cn=groupB,ou=groups,dc=grouper,dc=edu");
    psoID.setTargetID("openldap-prod");
    request.setPsoID(psoID);
    response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, "data/PSPTest.testLookup.groupBData.xml");

    // test returning everything
    request = new LookupRequest();
    request.setReturnData(ReturnData.EVERYTHING);
    request.setRequestID(REQUESTID_TEST);
    psoID = new PSOIdentifier();
    psoID.setID("cn=groupB,ou=groups,dc=grouper,dc=edu");
    psoID.setTargetID("openldap-prod");
    request.setPsoID(psoID);
    response = psp.execute(request);
    assertTrue(response instanceof LookupResponse);
    verifySpml(response, "data/PSPTest.testLookup.groupBEverything.xml");
  }

  public void testDiffAddFlat() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupA.getName());
    request.setTargetIds(targetIds);
    DiffResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testDiffAddFlat.response.xml");
  }

  public void testDiffDeleteFlat() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId("cn=groupC,ou=groups,dc=grouper,dc=edu");
    request.setTargetIds(targetIds);
    DiffResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testDiffDeleteFlat.response.xml");
  }

  public void testDiffModifyFlat() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setTargetIds(targetIds);
    DiffResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testDiffModifyFlat.response.xml");
  }

  public void testSyncAddFlat() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupA.getName());
    request.setTargetIds(targetIds);
    SyncResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testSyncAddFlat.response.xml");

    verifyLdif("data/PSPTest.testSyncAddFlat.after.ldif");
  }

  public void testSyncAddEmptyListFlat() {

    setTargets("openldap-prod", "openldap-test");

    groupB.deleteMember(SubjectTestHelper.SUBJ1);

    loadLdif("data/PSPTest.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setTargetIds(targetIds);
    SyncResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testSyncAddEmptyListFlat.response.xml");

    verifyLdif("data/PSPTest.testSyncAddEmptyListFlat.after.ldif");
  }

  public void testSyncDeleteFlat() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId("cn=groupC,ou=groups,dc=grouper,dc=edu");
    request.setTargetIds(targetIds);
    SyncResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testSyncDeleteFlat.response.xml");

    verifyLdif("data/PSPTest.testSyncDeleteFlat.after.ldif");
  }

  public void testSyncModifyFlat() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    SyncRequest request = new SyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setTargetIds(targetIds);
    SyncResponse response = (SyncResponse) psp.execute((Request) request);

    verifySpml(response, "data/PSPTest.testSyncModifyFlat.response.xml");

    verifyLdif("data/PSPTest.testSyncModifyFlat.after.ldif");
  }

  public void testSynchronized() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testSynchronized.ldif");

    DiffRequest request = new DiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setId(groupB.getName());
    request.setTargetIds(targetIds);
    DiffResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testSynchronized.response.xml");
  }

  public void testListTargets() {
    ListTargetsRequest request = new ListTargetsRequest();
    request.setRequestID(REQUESTID_TEST);
    ListTargetsResponse response = psp.execute(request);
    // TODO fails
    // verifySpml(response, "data/PSPTest.testListTargets.response.xml");

    // TODO test data
    for (Target target : response.getTargets()) {
      LOG.debug("target {}", target.getTargetID());
      LOG.debug("profile {}", target.getProfile());
      for (Schema schema : target.getSchemas()) {
        LOG.debug("schema ref {}", schema.getRef());

        for (Object oce : schema.getOpenContentElements(DSMLSchema.class)) {
          DSMLSchema ds = (DSMLSchema) oce;
          for (ObjectClassDefinition ocd : ds.getObjectClassDefinitions()) {
            for (AttributeDefinitionReference adr : ocd.getMemberAttributes().getAttributeDefinitionReferences()) {
              LOG.debug("schema adr {}", adr.getName());
            }
          }
        }

        for (SchemaEntityRef ser : schema.getSupportedSchemaEntities()) {
          LOG.debug("ser name {}", ser.getEntityName());
          LOG.debug("ser targetId {}", ser.getTargetID());
          for (Object oce : ser.getOpenContentElements(DSMLSchema.class)) {
            DSMLSchema ds = (DSMLSchema) oce;
            for (ObjectClassDefinition ocd : ds.getObjectClassDefinitions()) {
              for (AttributeDefinitionReference adr : ocd.getMemberAttributes().getAttributeDefinitionReferences()) {
                LOG.debug("ser adr {}", adr.getName());
              }
            }
          }
        }
      }
    }
  }

  public void testSearch() {

    loadLdif("data/PSPTest.testLookup.before.ldif");

    SearchRequest request;
    SearchResponse response;

    // TODO custom filter
    LdapFilterQueryClause filterQueryClause = new LdapFilterQueryClause();
    String filter = "objectclass=groupOfNames";
    filterQueryClause.setFilter(filter);

    Query query = new Query();
    PSOIdentifier basePsoId = new PSOIdentifier();
    basePsoId.setID("ou=groups,dc=grouper,dc=edu");
    query.setBasePsoID(basePsoId);
    query.setTargetID("openldap-prod");
    query.addQueryClause(filterQueryClause);

    // identifier
    request = new SearchRequest();
    request.setQuery(query);
    request.setRequestID(REQUESTID_TEST);
    request.setReturnData(ReturnData.IDENTIFIER);
    response = psp.execute(request);
    verifySpml(response, "data/PSPTest.testSearchIdentifier.response.xml");

    // data
    request = new SearchRequest();
    request.setQuery(query);
    request.setRequestID(REQUESTID_TEST);
    request.setReturnData(ReturnData.DATA);
    response = psp.execute(request);
    verifySpml(response, "data/PSPTest.testSearchData.response.xml");

    // everything
    request = new SearchRequest();
    request.setQuery(query);
    request.setRequestID(REQUESTID_TEST);
    request.setReturnData(ReturnData.EVERYTHING);
    response = psp.execute(request);
    verifySpml(response, "data/PSPTest.testSearchEverything.response.xml");
  }

  public void testUnsupportedOperation() {

    UpdatesRequest updatesRequest = new UpdatesRequest();
    updatesRequest.setRequestID(REQUESTID_TEST);
    Response response = psp.execute(updatesRequest);
    verifySpml(response, "data/PSPTest.unsupportedOperationResponse.xml");
  }

  public void testUnsupportedExecutionMode() {

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.setRequestID(REQUESTID_TEST);
    searchRequest.setExecutionMode(ExecutionMode.ASYNCHRONOUS);
    Response response = psp.execute((Request) searchRequest);
    verifySpml(response, "data/PSPTest.unsupportedExecutionModeResponse.xml");
  }

  public void testAddAlreadyExists() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testSynchronized.ldif");

    DiffResponse diffResponse = (DiffResponse) LdappcTestHelper.readSpml(psp.getXmlUnmarshaller(),
        getFile("data/PSPTest.testAddAlreadyExists.diffResponse.xml"));

    for (AddRequest addRequest : diffResponse.getAddRequests()) {
      AddResponse addResponse = psp.execute(addRequest);
      verifySpml(addResponse, "data/PSPTest.addResponseAlreadyExists.xml");
    }
  }

  public void testBulkCalc() {

    BulkCalcRequest request = new BulkCalcRequest();
    request.setRequestID(REQUESTID_TEST);

    BulkCalcResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testBulkCalc.response.xml");
  }

  public void testBulkDiff() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    BulkDiffRequest request = new BulkDiffRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setTargetIds(targetIds);

    BulkDiffResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testBulkDiff.response.xml");
  }

  public void testBulkSync() {

    setTargets("openldap-prod", "openldap-test");

    loadLdif("data/PSPTest.testModifyFlat.before.ldif");

    BulkSyncRequest request = new BulkSyncRequest();
    request.setRequestID(REQUESTID_TEST);
    request.setTargetIds(targetIds);

    BulkSyncResponse response = psp.execute(request);

    verifySpml(response, "data/PSPTest.testBulkSync.response.xml");

    verifyLdif("data/PSPTest.testBulkSync.after.ldif");
  }

  private File getFile(String fileName) {
    try {
      URL url = getClass().getResource(fileName);
      if (url == null) {
        fail("File not found : " + fileName);
      }
      return new File(url.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail("An error occurred : " + e.getMessage());
    }
    return null;
  }

  protected List<String> getParentNames(String name) {

    ArrayList<String> list = new ArrayList<String>();

    String parent = GrouperUtil.parentStemNameFromName(name, true);
    if (parent != null) {
      list.add(parent);
      list.addAll(this.getParentNames(parent));
    }

    return list;
  }

  protected void loadLdif(String fileName) {

    File file = getFile(fileName);

    for (TargetDefinition targetDefinition : psp.getTargetDefinitions().values()) {

      // just test some
      if (!targetIds.contains(targetDefinition.getId())) {
        continue;
      }

      LdapTargetProvider ldapTargetProvider = (LdapTargetProvider) targetDefinition.getProvider();

      Ldap ldap = null;
      try {
        ldap = ldapTargetProvider.getLdapPool().checkOut();

        LOG.debug("destroy test DIT");
        LdappcTestHelper.deleteChildren(GROUPER_BASE_DN, ldap);

        LOG.debug("load test DIT");
        LdappcTestHelper.loadLdif(file, ldap);

      } catch (Exception e) {
        e.printStackTrace();
        fail("An error occurred : " + e.getMessage());
      } finally {
        ldapTargetProvider.getLdapPool().checkIn(ldap);
      }
    }
  }

  protected void verifyLdif(String fileName) {

    File correctLdifFile = getFile(fileName);
    
    String correctLdif = LdappcTestHelper.readFile(correctLdifFile);

    for (TargetDefinition targetDefinition : psp.getTargetDefinitions().values()) {

      // just test some
      if (!targetIds.contains(targetDefinition.getId())) {
        continue;
      }

      LdapTargetProvider ldapTargetProvider = (LdapTargetProvider) targetDefinition.getProvider();

      Ldap ldap = null;
      try {
        ldap = ldapTargetProvider.getLdapPool().checkOut();

        String currentLdif = LdappcTestHelper.getCurrentLdif(GROUPER_BASE_DN, ldap);

        LdappcTestHelper.verifyLdif(correctLdif, currentLdif, true);

      } catch (Exception e) {
        e.printStackTrace();
        fail("An error occurred : " + e.getMessage());
      } finally {
        ldapTargetProvider.getLdapPool().checkIn(ldap);
      }
    }
  }

  private void verifySpml(Marshallable testObject, String correctXMLFileName) {
    LdappcTestHelper.verifySpml(psp.getXMLMarshaller(), psp.getXmlUnmarshaller(), testObject,
        getFile(correctXMLFileName));
  }

}
