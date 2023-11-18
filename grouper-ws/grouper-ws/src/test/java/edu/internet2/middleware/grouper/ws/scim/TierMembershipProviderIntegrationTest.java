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
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TierMembershipProviderIntegrationTest {

  private static final String MEMBERSHIPS_ENDPOINT = "http://localhost:8080/grouper-ws/scim/v2/Memberships";

  private static Group group1;
  private static Group group2;
  private static Group group3;

  private static Subject subject01;
  private static Subject subject02;

  private static Member member01;
  private static Member member02;

  @BeforeClass
  public static void setupClass() {
    GrouperSession gs = GrouperSession.startRootSession();
    group1 = new GroupSave()
            .assignName("test:group1")
            .assignCreateParentStemsIfNotExist(true)
            .assignDisplayName("test:Group 1 Display Name")
            .assignDescription("Lorem Ipsum 1")
            //.assignIdIndex(12345L)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
            .save();

    group2 = new GroupSave()
            .assignName("test:group2")
            .assignCreateParentStemsIfNotExist(true)
            .assignDisplayName("test:Group 2 Display Name")
            .assignDescription("Lorem Ipsum 2")
            //.assignIdIndex(12346L)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
            .save();

    group3 = new GroupSave()
            .assignName("test:group3")
            .assignCreateParentStemsIfNotExist(true)
            .assignDisplayName("test:Group 3 Display Name")
            .assignDescription("Lorem Ipsum 3")
            //.assignIdIndex(12346L)
            .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
            .save();

    RegistrySubject.addOrUpdate(gs, "00001", "person", "Subject 01", "what-is-nameAttributeValue", "subject01", "This is subject01", "subject01@example.com");
    RegistrySubject.addOrUpdate(gs, "00002", "person", "Subject 02", "what-is-nameAttributeValue", "subject02", "This is subject02", "subject02@example.com");
    /* for some reason the sourceId is null when creating, but then is jdbc when re-finding it */
    subject01 = SubjectFinder.findById("00001", true);
    subject02 = SubjectFinder.findById("00002", true);

    group1.addMember(subject01, false);
    group2.addMember(subject01, false);

    member01 = MemberFinder.findBySubject(gs, subject01, true);
    member02 = MemberFinder.findBySubject(gs, subject02, true);
  }

  @AfterClass
  public static void teardown() {
    String[] toDelete = {
            group1.getName(),
            group2.getName(),
            group3.getName(),
            "test:createMshipTest",
            "test:updateMshipTest",
            "test:deleteMshipTest",
            "test:deleteMembershipFailsWhenMembershipNotFound"
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
  public void getMembershipById() throws IOException {

    Membership mship = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group1, subject01, true);
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "/" + mship.getUuid());
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("membershipType").asText(), equalTo("immediate"));
    assertThat(json.get("enabled").asBoolean(), equalTo(Boolean.TRUE));
    assertThat(json.get("id").asText(), equalTo(mship.getUuid()));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:tier:params:scim:schemas:Membership"));
    assertNotNull(json.get("meta").get("version").asText());
    assertThat(json.get("meta").get("resourceType").asText(), equalTo("Membership"));

    JsonNode groupNode = json.get("owner");
    assertThat(groupNode.get("$ref").asText(), equalTo("../Groups/" + group1.getUuid()));
    assertThat(groupNode.get("display").asText(), equalTo(group1.getDisplayName()));
    assertThat(groupNode.get("value").asText(), equalTo(group1.getUuid()));

    JsonNode memberNode = json.get("member");
    assertThat(memberNode.get("$ref").asText(), equalTo("../Users/" + subject01.getId()));
    assertThat(memberNode.get("display").asText(), equalTo(subject01.getName()));
    assertThat(memberNode.get("value").asText(), equalTo(subject01.getId()));
  }

  @Test
  public void getMembershipByIdNotFound() throws IOException {

    Membership mship = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group1, subject01, true);
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "/bogus:bogus");
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("edu.internet2.middleware.grouper.exception.MembershipNotFoundException: could not find membership with uuid: 'bogus:bogus'"));
    assertThat(json.get("status").asText(), equalTo("404"));
  }

  @Test
  public void createMembershipSuccessfully() throws IOException {
    Group tempGroup = new GroupSave().assignName("test:createMshipTest").assignCreateParentStemsIfNotExist(true).save();

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:tier:params:scim:schemas:Membership");
    node.putObject("owner").put("value", tempGroup.getUuid());
    node.putObject("member").put("value", subject01.getId());

    HttpPost request = new HttpPost(MEMBERSHIPS_ENDPOINT);
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_CREATED));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    JsonNode json = objectMapper.readTree(jsonString);

    Membership mship = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), tempGroup, subject01, false);
    assertNotNull(mship);

    assertThat(json.get("id").asText(), equalTo(mship.getUuid()));
    assertThat(json.get("meta").get("resourceType").asText(), equalTo("Membership"));
    assertThat(json.get("membershipType").asText(), equalTo("immediate"));
    assertThat(json.get("owner").get("$ref").asText(), equalTo("../Groups/" + tempGroup.getUuid()));
    assertThat(json.get("member").get("$ref").asText(), equalTo("../Users/" + subject01.getId()));

    tempGroup.deleteMember(subject01, false);
  }

  @Test
  public void createMembershipFailsWhenGroupNotFoundInDatabase() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:tier:params:scim:schemas:Membership");
    node.putObject("owner").put("value", "bogus");
    node.putObject("member").put("value", subject01.getId());

    HttpPost request = new HttpPost(MEMBERSHIPS_ENDPOINT);
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("edu.internet2.middleware.grouper.exception.GroupNotFoundException: Cant find group by uuid: bogus"));
    assertThat(json.get("status").asText(), equalTo("400"));
  }

  @Test
  public void createMembershipFailsWhenMemberNotProvided() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:tier:params:scim:schemas:Membership");
    node.putObject("owner").put("value", group1.getUuid());

    HttpPost request = new HttpPost(MEMBERSHIPS_ENDPOINT);
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("java.lang.IllegalArgumentException: owner or member property is missing in the request body."));
    assertThat(json.get("status").asText(), equalTo("400"));
  }

  @Test
  public void createMembershipFailsWhenOwnerGroupNotProvided() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:tier:params:scim:schemas:Membership");
    node.putObject("member").put("value", subject01.getId());

    HttpPost request = new HttpPost(MEMBERSHIPS_ENDPOINT);
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("java.lang.IllegalArgumentException: owner or member property is missing in the request body."));
    assertThat(json.get("status").asText(), equalTo("400"));
  }

  @Test
  public void updateMembershipSuccessfully() throws IOException {
    Group tempGroup = new GroupSave().assignName("test:updateMshipTest").assignCreateParentStemsIfNotExist(true).save();
    tempGroup.addMember(subject01, true);
    Membership mship = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), tempGroup, subject01, false);

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:tier:params:scim:schemas:Membership");

    LocalDateTime enabledAt = LocalDateTime.now().minusDays(1);
    LocalDateTime disabledAt = LocalDateTime.now().plusDays(1);
    node.put("enabledTime", enabledAt.format(DateTimeFormatter.ISO_DATE_TIME));
    node.put("disabledTime", disabledAt.format(DateTimeFormatter.ISO_DATE_TIME));

    HttpPut request = new HttpPut(MEMBERSHIPS_ENDPOINT + "/" + mship.getUuid());
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

    // refresh from database
    mship = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), tempGroup, subject01, false);

    assertThat(mship.getEnabledTime(), equalTo(Timestamp.valueOf(enabledAt)));
    assertThat(mship.getDisabledTime(), equalTo(Timestamp.valueOf(disabledAt)));
  }

  @Test
  public void updateMembershipFailsWhenMembershipNotFound() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode node = objectMapper.createObjectNode();
    node.putArray("schemas").add("urn:tier:params:scim:schemas:Membership");

    HttpPut request = new HttpPut(MEMBERSHIPS_ENDPOINT + "/bogus:bogus");
    addScimHeaders(request);
    request.setEntity(new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));

    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("edu.internet2.middleware.grouper.exception.MembershipNotFoundException: could not find membership with uuid: 'bogus:bogus'"));
    assertThat(json.get("status").asText(), equalTo("404"));
  }

  @Test
  public void deleteMembership() throws IOException {
    Group tempGroup = new GroupSave().assignName("test:deleteMshipTest").assignCreateParentStemsIfNotExist(true).save();
    tempGroup.addMember(subject01, true);
    Membership mship = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), tempGroup, subject01, false);

    HttpDelete request = new HttpDelete(MEMBERSHIPS_ENDPOINT + "/" + mship.getUuid());
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);


    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NO_CONTENT));

    Membership mshipAfter = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), tempGroup, subject01, false);
    assertNull(mshipAfter);
  }

  @Test
  public void deleteMembershipFailsWhenMembershipNotFound() throws IOException {
    HttpDelete request = new HttpDelete(MEMBERSHIPS_ENDPOINT + "/bogus:bogus");
    addScimHeaders(request);
    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_NOT_FOUND));

    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("edu.internet2.middleware.grouper.exception.MembershipNotFoundException: could not find membership with uuid: 'bogus:bogus'"));
    assertThat(json.get("status").asText(), equalTo("404"));
  }

  @Test
  public void filterBySubjectIdWithResults() throws IOException {
    Membership mship1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group1, subject01, false);
    Membership mship2 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), group2, subject01, false);

    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "?filter="
            + URLEncoder.encode("subjectId eq \"" + subject01.getId() + "\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));

    assertThat(json.get("totalResults").asInt(), equalTo(2));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(2));

    assertThat(json.get("Resources").size(), equalTo(2));
    for (Iterator<JsonNode> it = json.get("Resources").elements(); it.hasNext(); ) {
      JsonNode node = it.next();

      assertThat(node.get("membershipType").asText(), equalTo("immediate"));
      assertThat(node.get("enabled").asBoolean(), equalTo(Boolean.TRUE));
      assertEquals(objectMapper.readerForListOf(String.class).readValue(node.get("schemas")),
              Arrays.asList("urn:tier:params:scim:schemas:Membership"));
      assertNotNull(node.get("meta").get("version").asText());

      assertThat(node.get("id").asText(), isOneOf(mship1.getUuid(), mship2.getUuid()));
      assertThat(node.get("owner").get("value").asText(), isOneOf(group1.getUuid(), group2.getUuid()));
      assertThat(node.get("owner").get("$ref").asText(), isOneOf("../Groups/" + group1.getUuid(), "../Groups/" + group2.getUuid()));
      assertThat(node.get("owner").get("display").asText(), isOneOf(group1.getDisplayName(), group2.getDisplayName()));

      assertThat(node.get("member").get("value").asText(), equalTo(subject01.getId()));
      assertThat(node.get("member").get("display").asText(), equalTo(member01.getName()));
      assertThat(node.get("member").get("$ref").asText(), equalTo("../Users/" + subject01.getId()));
    }
  }

  @Test
  public void filterBySubjectIdWithNoResults() throws IOException {
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "?filter="
            + URLEncoder.encode("subjectId eq \"" + subject02.getId() + "\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));

    assertThat(json.get("totalResults").asInt(), equalTo(0));
  }

  @Test
  public void filterBySubjectIdentifier() throws IOException {
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "?filter="
            + URLEncoder.encode("subjectIdentifier eq \"" + subject01.getAttributeValue("loginid") + "\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));

    assertThat(json.get("totalResults").asInt(), equalTo(2));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(2));

    assertThat(json.get("Resources").size(), equalTo(2));
  }

  @Test
  public void filterByGroupWithResults() throws IOException {
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "?filter="
            + URLEncoder.encode("groupName eq \"" + group1.getName() + "\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));

    assertThat(json.get("totalResults").asInt(), equalTo(1));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(1));

    assertThat(json.get("Resources").size(), equalTo(1));

    assertThat(json.get("Resources").get(0).get("member").get("display").asText(), equalTo(member01.getName()));
  }

  @Test
  public void filterByGroupWithNoResults() throws IOException {
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "?filter="
            + URLEncoder.encode("groupName eq \"" + group3.getName() + "\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));

    assertThat(json.get("totalResults").asInt(), equalTo(0));
  }

  @Test
  public void filterByGroupAndSubject() throws IOException {
    HttpUriRequest request = new HttpGet(MEMBERSHIPS_ENDPOINT + "?filter="
            + URLEncoder.encode(
                    "groupName eq \"" + group1.getName() + "\""
            + " and subjectId eq \"" + subject01.getId() + "\"", StandardCharsets.UTF_8.toString())
    );
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);

    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:ListResponse"));

    assertThat(json.get("totalResults").asInt(), equalTo(1));
    assertThat(json.get("startIndex").asInt(), equalTo(1));
    assertThat(json.get("itemsPerPage").asInt(), equalTo(1));

    assertThat(json.get("Resources").size(), equalTo(1));

    assertThat(json.get("Resources").get(0).get("member").get("display").asText(), equalTo(member01.getName()));
  }

}
