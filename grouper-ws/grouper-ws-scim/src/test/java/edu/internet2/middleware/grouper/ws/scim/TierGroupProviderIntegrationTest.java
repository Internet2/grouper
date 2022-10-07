/****
 * Copyright 2022 Internet2
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
 ***/

package edu.internet2.middleware.grouper.ws.scim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.subject.Subject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TierGroupProviderIntegrationTest {

  private static final String GROUPS_ENDPOINT = "http://localhost:8080/grouper-ws-scim/v2/Groups";

  private static Group existingGroupWithNoMembers;
  private static Group existingGroupWithMembers;

  private static Subject subject01;
  private static Subject subject02;

  private static Member member01;
  private static Member member02;

  @BeforeClass
  public static void setupClass() {
    GrouperSession gs = GrouperSession.startRootSession();
    existingGroupWithNoMembers = new GroupSave()
            .assignName("test:existingGroupWithNoMembers")
            .assignCreateParentStemsIfNotExist(true)
            .assignDisplayName("test:Existing Display Name - No members")
            .assignDescription("Lorem Ipsum 1")
            //.assignIdIndex(12345L)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
            .save();

    existingGroupWithMembers = new GroupSave()
            .assignName("test:existingGroupWithMembers")
            .assignCreateParentStemsIfNotExist(true)
            .assignDisplayName("test:Existing Display Name - with members")
            .assignDescription("Lorem Ipsum 2")
            //.assignIdIndex(12346L)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
            .save();

    RegistrySubject.addOrUpdate(gs, "00001", "person", "Subject 01", "what-is-nameAttributeValue", "subject01", "This is subject01", "subject01@example.com");
    RegistrySubject.addOrUpdate(gs, "00002", "person", "Subject 02", "what-is-nameAttributeValue", "subject02", "This is subject02", "subject02@example.com");
    /* for some reason the sourceId is null when creating, but then is jdbc when re-finding it */
    subject01 = SubjectFinder.findById("00001", true);
    subject02 = SubjectFinder.findById("00002", true);

    existingGroupWithMembers.addMember(subject01, false);
    existingGroupWithMembers.addMember(subject02, false);

    member01 = MemberFinder.findBySubject(gs, subject01, true);
    member02 = MemberFinder.findBySubject(gs, subject02, true);
  }

  @AfterClass
  public static void teardown() {
    String[] toDelete = {
            existingGroupWithNoMembers.getName(),
            existingGroupWithMembers.getName(),
            "test:createGroup",
            "test:updateGroup"
    };
    for (String name: toDelete) {
      try {
        new GroupFinder().addGroupName(name).findGroup().delete();
      } catch (Exception e) {
        ;
      }
    }
  }

  private void addScimHeaders(HttpUriRequest request) {
    request.setHeader("Content-Type", "application/scim+json");
    request.setHeader("Accept", "application/scim+json");
    request.setHeader("Authorization", "Basic R3JvdXBlclN5c3RlbTpwYXNz");
  }

  /**
   * In addition to testing the GET endpoint, it does a full test of the json
   */
  @Test
  public void getGroupByUuidInPath() throws IOException {

    HttpUriRequest request = new HttpGet(GROUPS_ENDPOINT + "/" + existingGroupWithNoMembers.getUuid());
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("id").asText(), equalTo(existingGroupWithNoMembers.getUuid()));
    assertThat(json.get("baseUrn").asText(), equalTo("urn:ietf:params:scim:schemas:core:2.0:Group"));
    assertThat(json.get("resourceType").asText(), equalTo("Group"));
    assertThat(json.get("displayName").asText(), equalTo("test:Existing Display Name - No members"));
    assertThat(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")), containsInAnyOrder(
            "urn:ietf:params:scim:schemas:core:2.0:Group",
            "urn:grouper:params:scim:schemas:extension:TierGroupExtension",
            "urn:tier:params:scim:schemas:extension:TierMetaExtension"
    ));
    assertThat(json.get("members").size(), equalTo(0));

    /* todo from here is specific to the PSU implementation. Needs to change when changing the underlying library */

    // this is a hash of the Scim resource; just needs to be unique. How to test?
    assertNotNull(json.get("meta").get("version").asText());

    // wrapping extensions in an "extensions" node is not standard
    assertThat(json.get("extensions").get("urn:tier:params:scim:schemas:extension:TierMetaExtension").get("resultCode").asText(), equalTo("SUCCESS"));
    assertThat(json.get("extensions").get("urn:tier:params:scim:schemas:extension:TierMetaExtension").get("responseDurationMillis").asLong(), greaterThan(0L));

    assertThat(json.get("extensions").get("urn:grouper:params:scim:schemas:extension:TierGroupExtension").get("systemName").asText(), equalTo("test:existingGroupWithNoMembers"));
    assertThat(json.get("extensions").get("urn:grouper:params:scim:schemas:extension:TierGroupExtension").get("description").asText(), equalTo("Lorem Ipsum 1"));
    assertThat(json.get("extensions").get("urn:grouper:params:scim:schemas:extension:TierGroupExtension").get("idIndex").asLong(), equalTo(existingGroupWithNoMembers.getIdIndex()));


    /* Now test the member format in a group with members */
    request = new HttpGet(GROUPS_ENDPOINT + "/" + existingGroupWithMembers.getUuid());
    addScimHeaders(request);
    httpResponse = HttpClientBuilder.create().build().execute(request);
    jsonString = EntityUtils.toString(httpResponse.getEntity());
    json = objectMapper.readTree(jsonString);

    assertThat(json.get("id").asText(), equalTo(existingGroupWithMembers.getUuid()));
    assertThat(json.get("members").size(), equalTo(2));
    assertThat(json.get("members").get(0).get("ref").asText(), isOneOf("../Users/00001", "../Users/00002"));
    assertThat(json.get("members").get(0).get("type").asText(), equalTo("DIRECT"));
    assertThat(json.get("members").get(0).get("value").asText(), isOneOf(member01.getId(), member02.getId()));
  }

  @Test
  public void getGroupBySystemNameExplicitInPath() throws IOException {
    HttpUriRequest request = new HttpGet(GROUPS_ENDPOINT + "/systemName:" + existingGroupWithNoMembers.getName());
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("id").asText(), equalTo(existingGroupWithNoMembers.getUuid()));
  }

  @Test
  public void getGroupByIdIndexInPath() throws IOException {
    HttpUriRequest request = new HttpGet(GROUPS_ENDPOINT + "/idIndex:" + existingGroupWithNoMembers.getIdIndex());
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("id").asText(), equalTo(existingGroupWithNoMembers.getUuid()));
  }

  /**
   * Return 404 error for invalid group uuid; also full check of error message
   * @throws IOException
   */
  @Test
  public void getGroupFailsWhenNotFoundInPath() throws IOException {
    HttpUriRequest request = new HttpGet(GROUPS_ENDPOINT + "/bogusGroupUuid");
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("Resource bogusGroupUuid not found"));
    assertThat(json.get("status").asText(), equalTo("NOT_FOUND"));
  }

  @Test
  public void getGroupFailsNonNumericIdIndexInPath() throws IOException {
    HttpUriRequest request = new HttpGet(GROUPS_ENDPOINT + "/idIndex:bogusIdIndex");
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    // todo should this be 400 BAD REQUEST instead of 404? PSU returns 404
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("detail").asText(), equalTo("Resource idIndex:bogusIdIndex not found"));
  }

  @Test
  public void getGroupFailsWhenNameMissingColonInPath() throws IOException {
    HttpUriRequest request = new HttpGet(GROUPS_ENDPOINT + "/systemName:bogusName");
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    // todo should this be 400 BAD REQUEST instead of 404? PSU returns 404
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("detail").asText(), equalTo("Resource systemName:bogusName not found"));
  }

  @Test
  public void createGroup() throws IOException {
    Group existingGroup = new GroupFinder().addGroupName("test:createGroup").findGroup();
    if (existingGroup != null) {
      existingGroup.delete();
    }

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:ietf:params:scim:schemas:core:2.0:Group");
    node.put("displayName", "test:createGroup");
    node.putArray("members").add(objectMapper.createObjectNode().put("value", subject01.getId()));

    /** todo this fails for PSU "edu.psu.swe.scim.spec.resources.ScimExtension not instantiable" */
    //ObjectNode tierGroupNode = objectMapper.createObjectNode();
    //tierGroupNode.put("description", "createGroup - description");
    //node.putObject("extensions").putIfAbsent("urn:grouper:params:scim:schemas:extension:TierGroupExtension", tierGroupNode);

    HttpPost request = new HttpPost(GROUPS_ENDPOINT);
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_CREATED));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("extensions").get("urn:tier:params:scim:schemas:extension:TierMetaExtension").get("resultCode").asText(), equalTo("SUCCESS_CREATED"));
    assertThat(json.get("displayName").asText(), equalTo("test:createGroup"));

    /** todo this fails for PSU "edu.psu.swe.scim.spec.resources.ScimExtension not instantiable" */
    //assertThat(json.get("extensions").get("urn:grouper:params:scim:schemas:extension:TierGroupExtension").get("description").asText(), equalTo("createGroup - description"));

    /** todo for PSU, members not returned on create */
    //assertThat(json.get("members").size(), equalTo(2));
    //assertThat(json.get("members").get(0).get("ref").asText(), equalTo("../Users/00001"));
    //assertThat(json.get("members").get(0).get("type").asText(), equalTo("DIRECT"));
    //assertThat(json.get("members").get(0).get("value").asText(), equalTo(member01.getId());
  }

  @Test
  public void createGroupFailsOnRootFolder() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:ietf:params:scim:schemas:core:2.0:Group");
    node.put("displayName", "createGroupFailsOnRootFolder");

    HttpPost request = new HttpPost(GROUPS_ENDPOINT);
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("detail").asText(), equalTo("name must contain atleast one colon (:)"));

  }

  /** todo Fails with PSU: javax.ws.rs.ClientErrorException: HTTP 405 Method Not Allowed */
  @Test
  public void updateGroup() throws IOException {
    Group existingGroup = new GroupSave()
            .assignName("test:updateGroup")
            .assignCreateParentStemsIfNotExist(true)
            .assignDisplayName("updateGroup - DisplayName")
            .assignDescription("updateGroup - Description")
            //.assignIdIndex(12345L)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
            .save();

    existingGroup.addMember(subject01, false);

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas")
            .add("urn:ietf:params:scim:schemas:core:2.0:Group")
            .add("urn:grouper:params:scim:schemas:extension:TierGroupExtension");
    node.putObject("urn:grouper:params:scim:schemas:extension:TierGroupExtension")
            // todo PSU how do you set display name or display extension in an update or create?
            //.put("displayName", "DisplayName changed")
            .put("description", "description changed");
    node.putArray("members").add(objectMapper.createObjectNode().put("value", subject02.getId()));

    HttpPut request = new HttpPut(GROUPS_ENDPOINT + "/" + existingGroup.getId());
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("extensions").get("urn:tier:params:scim:schemas:extension:TierMetaExtension").get("resultCode").asText(), equalTo("SUCCESS_UPDATED"));
    assertThat(json.get("displayName").asText(), equalTo("test:updateGroup"));

    /* todo PSU updating members not working */
    //assertThat(json.get("members").size(), equalTo(2));
    //assertThat(json.get("members").get(0).get("ref").asText(), equalTo("../Users/00002"));
    //assertThat(json.get("members").get(0).get("type").asText(), equalTo("DIRECT"));
    //assertThat(json.get("members").get(0).get("value").asText(), equalTo(member02.getId()));

    /* todo PSU not updating the description, actually clears it out */
    //assertThat(json.get("extensions").get("urn:grouper:params:scim:schemas:extension:TierGroupExtension").get("description"), equalTo("description changed"));
    assertThat(json.get("extensions").get("urn:grouper:params:scim:schemas:extension:TierGroupExtension").get("description").asText(), equalTo(""));
  }

  @Test
  public void deleteGroup() throws IOException {
    Group existingGroup = new GroupSave()
            .assignName("test:deleteGroup")
            .save();

    HttpDelete request = new HttpDelete(GROUPS_ENDPOINT + "/" + existingGroup.getUuid());
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NO_CONTENT));
  }


  /** Search implementation
   *
   * These are the one PSU can handle
   *
   *     exactMatchMap.put("name", findByExactName);
   *     exactMatchMap.put("displayName", findByExactDisplayName);
   *     exactMatchMap.put("extension", findByExactExtension);
   *     exactMatchMap.put("displayExtension", findByExactDisplayExtension);
   *     exactMatchMap.put("uuid", findByExactUuid);
   *     exactMatchMap.put("idIndex", findByExactIdIndex);
   *     exactMatchMap.put("description", findByExactDescription);
   *
   *     approximateMatchMap.put("displayName", findByApproximateDisplayName);
   *     approximateMatchMap.put("extension", findByApproximateExtension);
   *     approximateMatchMap.put("displayExtension", findByApproximateDisplayExtension);
   *     approximateMatchMap.put("description", findByApproximateDescription);
   */

  @Test
  public void searchGroupByExactField() throws IOException {
    HttpGet request = new HttpGet(GROUPS_ENDPOINT + "/?filter=name%20eq%20%22" + existingGroupWithNoMembers.getName() + "%22");
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(1));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));
    assertThat(json.get("resources").get(0).get("id").asText(), equalTo(existingGroupWithNoMembers.getUuid()));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=uuid%20eq%20%22" + existingGroupWithNoMembers.getUuid() + "%22");
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=idIndex%20eq%20%22" + existingGroupWithNoMembers.getIdIndex() + "%22");
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=displayName%20eq%20%22" + URLEncoder.encode(existingGroupWithNoMembers.getDisplayName(), StandardCharsets.UTF_8.toString()) + "%22");
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=extension%20eq%20%22" + existingGroupWithNoMembers.getExtension() + "%22");
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=displayExtension%20eq%20%22" + URLEncoder.encode(existingGroupWithNoMembers.getDisplayExtension(), StandardCharsets.UTF_8.toString()) + "%22");
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=description%20eq%20%22" + URLEncoder.encode(existingGroupWithNoMembers.getDescription(), StandardCharsets.UTF_8.toString()) + "%22");
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(1));
  }

  @Test
  public void searchGroupByApproximateField() throws IOException {
    HttpGet request = new HttpGet(GROUPS_ENDPOINT + "/?filter=" + URLEncoder.encode("displayName co \"test:Existing Display Name -\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(2));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(2));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));
    assertThat(json.get("resources").get(0).get("id").asText(), isOneOf(
            existingGroupWithNoMembers.getUuid(),
            existingGroupWithMembers.getUuid()));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=" + URLEncoder.encode("extension co \"existingGroupWith\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(2));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=" + URLEncoder.encode("displayExtension co \"Existing Display Name\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(2));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=" + URLEncoder.encode("description co \"Lorem Ipsum\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(2));
  }

  @Test
  public void searchGroupWhenNotFound() throws IOException {
    HttpGet request = new HttpGet(GROUPS_ENDPOINT + "/?filter=name%20eq%20%22test:bogusGroup%22");
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(0));

    request = new HttpGet(GROUPS_ENDPOINT + "/?filter=extension%20co%20%22bogusGroup%22");
    addScimHeaders(request);

    httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    jsonString = EntityUtils.toString(httpResponse.getEntity());
    objectMapper = new ObjectMapper();
    json = objectMapper.readTree(jsonString);
    assertThat(json.get("totalResults").asInt(), equalTo(0));
  }
}
