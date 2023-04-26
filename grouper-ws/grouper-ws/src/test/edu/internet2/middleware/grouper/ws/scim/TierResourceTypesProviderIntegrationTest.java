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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class TierResourceTypesProviderIntegrationTest {

  private static final String RESOURCE_TYPES_ENDPOINT = "http://localhost:8080/grouper-ws/scim/v2/ResourceTypes";

  private void addScimHeaders(HttpUriRequest request) {
    request.setHeader("Content-Type", "application/scim+json");
    request.setHeader("Accept", "application/scim+json");
    request.setHeader("Authorization", "Basic R3JvdXBlclN5c3RlbTpwYXNz");
  }

  @Test
  public void getResourceTypesConfig() throws IOException {

    HttpUriRequest request = new HttpGet(RESOURCE_TYPES_ENDPOINT);
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();

    for (JsonNode node: objectMapper.readTree(jsonString)) {
      assertThat(node.get("meta").get("resourceType").asText(), equalTo("ResourceType"));
      assertEquals(objectMapper.readerForListOf(String.class).readValue(node.get("schemas")),
              Arrays.asList("urn:ietf:params:scim:schemas:core:2.0:ResourceType"));

      switch (node.get("schema").asText()) {
        case "urn:ietf:params:scim:schemas:core:2.0:Group":
          assertThat(node.get("endpoint").asText(), equalTo("/Groups"));
          assertThat(node.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ResourceTypes/Group"));
          assertThat(node.get("name").asText(), equalTo("Group"));
          assertThat(node.get("description").asText(), equalTo("Top level SCIM Group"));
          assertThat(node.get("schemaExtensions").get(0).get("schema").asText(), equalTo("urn:tier:params:scim:schemas:extension:TierMetaExtension"));
          assertThat(node.get("schemaExtensions").get(0).get("required").asBoolean(), equalTo(Boolean.FALSE));
          assertThat(node.get("schemaExtensions").get(1).get("schema").asText(), equalTo("urn:grouper:params:scim:schemas:extension:TierGroupExtension"));
          assertThat(node.get("schemaExtensions").get(1).get("required").asBoolean(), equalTo(Boolean.FALSE));

          assertThat(node.get("id").asText(), equalTo("Group"));
          break;
        case "urn:ietf:params:scim:schemas:core:2.0:User":
          assertThat(node.get("endpoint").asText(), equalTo("/Users"));
          assertThat(node.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ResourceTypes/User"));
          assertThat(node.get("name").asText(), equalTo("User Account"));
          assertThat(node.get("description").asText(), equalTo("Top level SCIM User"));
          assertThat(node.get("schemaExtensions").get(0).get("schema").asText(), equalTo("urn:tier:params:scim:schemas:extension:TierMetaExtension"));
          assertThat(node.get("schemaExtensions").get(0).get("required").asBoolean(), equalTo(Boolean.FALSE));
          assertThat(node.get("id").asText(), equalTo("User"));
          break;
        case "urn:tier:params:scim:schemas:Membership":
          assertThat(node.get("endpoint").asText(), equalTo("/Memberships"));
          assertThat(node.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ResourceTypes/Membership"));
          assertThat(node.get("name").asText(), equalTo("Membership"));
          assertThat(node.get("description").asText(), equalTo("Resource for representing Membership schema data"));
          assertThat(node.get("id").asText(), equalTo("Membership"));
          break;
        default:
          throw new RuntimeException("Unexpected resource type: " + node.get("id"));
      }
    }
  }

  @Test
  public void getUserResourceByName() throws IOException {
    HttpUriRequest request = new HttpGet(RESOURCE_TYPES_ENDPOINT + "/User");
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("meta").get("resourceType").asText(), equalTo("ResourceType"));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:schemas:core:2.0:ResourceType"));
    assertThat(json.get("endpoint").asText(), equalTo("/Users"));
    assertThat(json.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ResourceTypes/User"));
    assertThat(json.get("name").asText(), equalTo("User Account"));
    assertThat(json.get("description").asText(), equalTo("Top level SCIM User"));
    assertThat(json.get("schemaExtensions").get(0).get("schema").asText(), equalTo("urn:tier:params:scim:schemas:extension:TierMetaExtension"));
    assertThat(json.get("schemaExtensions").get(0).get("required").asBoolean(), equalTo(Boolean.FALSE));
    assertThat(json.get("id").asText(), equalTo("User"));
  }

  @Test
  public void getGroupResourceByName() throws IOException {
    HttpUriRequest request = new HttpGet(RESOURCE_TYPES_ENDPOINT + "/Group");
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("meta").get("resourceType").asText(), equalTo("ResourceType"));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:schemas:core:2.0:ResourceType"));
    assertThat(json.get("endpoint").asText(), equalTo("/Groups"));
    assertThat(json.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ResourceTypes/Group"));
    assertThat(json.get("name").asText(), equalTo("Group"));
    assertThat(json.get("description").asText(), equalTo("Top level SCIM Group"));
    assertThat(json.get("schemaExtensions").get(0).get("schema").asText(), equalTo("urn:tier:params:scim:schemas:extension:TierMetaExtension"));
    assertThat(json.get("schemaExtensions").get(0).get("required").asBoolean(), equalTo(Boolean.FALSE));
    assertThat(json.get("schemaExtensions").get(1).get("schema").asText(), equalTo("urn:grouper:params:scim:schemas:extension:TierGroupExtension"));
    assertThat(json.get("schemaExtensions").get(1).get("required").asBoolean(), equalTo(Boolean.FALSE));

    assertThat(json.get("id").asText(), equalTo("Group"));
  }

  @Test
  public void getMembershipResourceByName() throws IOException {
    HttpUriRequest request = new HttpGet(RESOURCE_TYPES_ENDPOINT + "/Membership");
    addScimHeaders(request);

    HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

    assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    assertThat(ContentType.getOrDefault(httpResponse.getEntity()).getMimeType(), equalTo("application/scim+json"));

    String jsonString = EntityUtils.toString(httpResponse.getEntity());
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(jsonString);
    assertThat(json.get("meta").get("resourceType").asText(), equalTo("ResourceType"));
    assertEquals(objectMapper.readerForListOf(String.class).readValue(json.get("schemas")),
            Arrays.asList("urn:ietf:params:scim:schemas:core:2.0:ResourceType"));
    assertThat(json.get("endpoint").asText(), equalTo("/Memberships"));
    assertThat(json.get("meta").get("location").asText(), equalTo("http://localhost:8080/grouper-ws/scim/v2/ResourceTypes/Membership"));
    assertThat(json.get("name").asText(), equalTo("Membership"));
    assertThat(json.get("description").asText(), equalTo("Resource for representing Membership schema data"));
    assertThat(json.get("id").asText(), equalTo("Membership"));
  }

}
