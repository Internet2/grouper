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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
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

public class TierUserProviderIntegrationTest {

  private static final String USERS_ENDPOINT = "http://localhost:8080/grouper-ws/scim/v2/Users";

  private static Group group1;
  private static Group group2;

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

    RegistrySubject.addOrUpdate(gs, "00001", "person", "Subject 01", "what-is-nameAttributeValue", "subject01", "This is subject01", "subject01@example.com");
    RegistrySubject.addOrUpdate(gs, "00002", "person", "Subject 02", "what-is-nameAttributeValue", "subject02", "This is subject02", "subject02@example.com");
    /* for some reason the sourceId is null when creating, but then is jdbc when re-finding it */
    subject01 = SubjectFinder.findById("00001", true);
    subject02 = SubjectFinder.findById("00002", true);

    group1.addMember(subject01, false);
    group1.addMember(subject02, false);
    group2.addMember(subject01, false);

    member01 = MemberFinder.findBySubject(gs, subject01, true);
    member02 = MemberFinder.findBySubject(gs, subject02, true);
  }

  @AfterClass
  public static void teardown() {
    String[] toDelete = {
            group1.getName(),
            group2.getName()
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
  public void getUserById() throws IOException {

    HttpUriRequest request = new HttpGet(USERS_ENDPOINT + "/" + subject01.getId());
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("id").asText(), equalTo(subject01.getId()));
    assertThat(json.get("displayName").asText(), equalTo(subject01.getName()));
    assertThat(json.get("active").asBoolean(), equalTo(Boolean.TRUE));
    assertThat(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")), containsInAnyOrder(
            "urn:ietf:params:scim:schemas:core:2.0:User",
            "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User",
            "urn:tier:params:scim:schemas:extension:TierMetaExtension"
    ));

    // this is a hash of the Scim resource; just needs to be unique. How to test?
    assertNotNull(json.get("meta").get("version").asText());

    assertThat(json.get("urn:tier:params:scim:schemas:extension:TierMetaExtension").get("resultCode").asText(), equalTo("SUCCESS"));
    assertThat(json.get("urn:tier:params:scim:schemas:extension:TierMetaExtension").get("responseDurationMillis").asLong(), greaterThan(0L));

    assertThat(json.get("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User").get("employeeNumber").asText(), equalTo(subject01.getId()));

    assertThat(json.get("groups").size(), equalTo(2));
    assertThat(json.get("groups").get(0).get("value").asText(), isOneOf(
            group1.getName(),
            group2.getName()));
    assertThat(json.get("groups").get(1).get("value").asText(), isOneOf(
            group1.getName(),
            group2.getName()));

    assertThat(json.get("groups").get(0).get("display").asText(), isOneOf(
            group1.getDisplayExtension(),
            group2.getDisplayExtension()));

    assertThat(json.get("groups").get(0).get("$ref").asText(), isOneOf(
            "../Groups/" + group1.getUuid(),
            "../Groups/" + group2.getUuid()));

  }

  @Test
  public void getUserByIdentifier() throws IOException {

    HttpUriRequest request = new HttpGet(USERS_ENDPOINT + "/" + subject01.getAttributeValue("loginid"));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("id").asText(), equalTo(subject01.getId()));
  }



  @Test
  public void searchUserByExactIdField() throws IOException {
    HttpGet request = new HttpGet(USERS_ENDPOINT + "/?filter=" + URLEncoder.encode("id eq \"" + subject01.getId() + "\"", StandardCharsets.UTF_8.toString()));
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
    assertThat(json.get("Resources").get(0).get("id").asText(), equalTo(subject01.getId()));
  }

  @Test
  public void searchUserByExactIdentifierField() throws IOException {
    HttpGet request = new HttpGet(USERS_ENDPOINT + "/?filter=" + URLEncoder.encode("identifier eq \"" + subject01.getAttributeValue("loginid") + "\"", StandardCharsets.UTF_8.toString()));
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
    assertThat(json.get("Resources").get(0).get("id").asText(), equalTo(subject01.getId()));
  }

  @Test
  public void searchUserByOtherFieldFails() throws IOException {
    HttpGet request = new HttpGet(USERS_ENDPOINT + "/?filter=" + URLEncoder.encode("identifier co \"subject\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("java.lang.IllegalArgumentException: Only eq filter supported"));
    assertThat(json.get("status").asText(), equalTo("400"));

    request = new HttpGet(USERS_ENDPOINT + "/?filter=" + URLEncoder.encode("mail eq \"" + subject01.getAttributeValue("email") + "\"", StandardCharsets.UTF_8.toString()));
    addScimHeaders(request);

    httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));
    jsonString = EntityUtils.toString(httpResponse.getEntity());
    json = objectMapper.readTree(jsonString);
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:api:messages:2.0:Error"));
    assertThat(json.get("detail").asText(), equalTo("java.lang.IllegalArgumentException: only id and identifier attribute names are allowed"));
    assertThat(json.get("status").asText(), equalTo("400"));

  }
}
